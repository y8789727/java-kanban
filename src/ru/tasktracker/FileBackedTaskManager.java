package ru.tasktracker;

import ru.tasktracker.exceptions.ManagerLoadException;
import ru.tasktracker.exceptions.ManagerSaveException;
import ru.tasktracker.tasks.Epic;
import ru.tasktracker.tasks.Subtask;
import ru.tasktracker.tasks.Task;
import ru.tasktracker.tasks.TaskStatus;
import ru.tasktracker.tasks.TaskType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    final Path file;

    public static final String CSV_FILE_HEADER = "id,type,name,status,description,epic,duration,startTime";
    private static final DateTimeFormatter csvDateFormatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmm");

    private FileBackedTaskManager(HistoryManager historyManager, Path file, boolean isFileSaveNeeded) {
        super(historyManager);
        this.file = file;

        if (isFileSaveNeeded) {
            save();
        }
    }

    public FileBackedTaskManager(HistoryManager historyManager, Path file) {
        this(historyManager, file, true);
    }

    private void save() {
        List<Task> tasks = super.getAllTasks();

        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()))) {
            writer.write(CSV_FILE_HEADER);

            for (Task t : tasks) {
                writer.newLine();
                writer.write(taskToString(t));
            }

            writer.flush();
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    private static String taskToString(Task t) {
        String epicId = "";
        if (TaskType.SUBTASK.equals(t.getType())) {
            epicId = Integer.toString(((Subtask) t).getEpic().getId());
        }

        String duration = "";
        String startTime = "";
        if (!TaskType.EPIC.equals(t.getType())) {
            if (t.getDuration() != null) {
                duration = Long.toString(t.getDuration().toMinutes());
            }

            if (t.getStartTime() != null) {
                startTime = t.getStartTime().format(csvDateFormatter);
            }
        }

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s", t.getId(), t.getType(), t.getTitle(), t.getStatus(), t.getDescription(), epicId, duration, startTime);
    }

    private static Task stringToTask(String line, Map<Integer, Epic> epics) {
        final Task task;

        String[] columns = line.split(",", 8);
        int i = 0;
        final int taskId = Integer.parseInt(columns[i++]);
        final TaskType taskType = TaskType.valueOf(columns[i++]);
        final String taskTitle = columns[i++];
        final TaskStatus taskStatus = TaskStatus.valueOf(columns[i++]);
        final String taskDescription = columns[i++];
        Integer epicId = null;
        String epicIdStr = columns[i++];
        if (!epicIdStr.isEmpty()) {
            epicId = Integer.parseInt(epicIdStr);
        }
        Duration duration = null;
        String durationStr = columns[i++];
        if (!durationStr.isEmpty()) {
            duration = Duration.ofMinutes(Integer.parseInt(durationStr));
        }
        LocalDateTime startTime = null;
        String startTimeStr = columns[i++];
        if (!startTimeStr.isEmpty()) {
            startTime = LocalDateTime.parse(startTimeStr, csvDateFormatter);
        }

        if (TaskType.TASK.equals(taskType)) {
            task = new Task(taskTitle, taskDescription, taskId, duration, startTime);
            task.setStatus(taskStatus);
        } else if (TaskType.EPIC.equals(taskType)) {
            task = new Epic(taskTitle, taskDescription, taskId);
            epics.put(task.getId(), (Epic) task);
        } else if (TaskType.SUBTASK.equals(taskType)) {
            if (epicId == null) {
                throw new ManagerLoadException(String.format("Для подзадачи id=%d не определен эпик", taskId));
            }

            final Epic epic = epics.get(epicId);
            if (epic == null) {
                throw new ManagerLoadException(String.format("Для подзадачи id=%d эпик с id=%d не найден", taskId, epicId));
            }

            task = new Subtask(taskTitle, taskDescription, taskId, epic, duration, startTime);
            task.setStatus(taskStatus);
        } else {
            throw new ManagerLoadException("Неподдерживаемый тип задач: " + taskType);
        }

        return task;
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        final FileBackedTaskManager fileTaskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file, false);

        try (final BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
            final Map<Integer, Epic> epics = new HashMap<>();

            boolean firstLineRead = false;
            while (reader.ready()) {
                if (!firstLineRead) {
                    reader.readLine();
                    firstLineRead = true;
                } else {
                    fileTaskManager.addTask(stringToTask(reader.readLine(), epics));
                }
            }
        } catch (IOException e) {
            throw new ManagerLoadException(e.getMessage());
        }

        return fileTaskManager;
    }

    @Override
    protected void addTask(Task t) {
        super.addTask(t);
        save();
    }

    @Override
    public void updateTask(Task t) {
        super.updateTask(t);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeTaskById(Integer id) {
        super.removeTaskById(id);
        save();
    }

    public static void main(String[] args) {
        try {
            Path src = File.createTempFile("srcMain", ".csv").toPath();
            Duration duration = Duration.ofMinutes(30);
            LocalDateTime startTime = LocalDateTime.of(2025,1,1,10,0);

            FileBackedTaskManager tm1 = new FileBackedTaskManager(Managers.getDefaultHistory(), src);
            tm1.createTask("t1", "t1d", duration, startTime);
            Epic e = tm1.createEpic("e1", "e1d");
            tm1.createSubtask("e1s1", "e1s1d", e, duration, startTime);
            Subtask s2 = tm1.createSubtask("e1s2", "e1s2d", e, duration, startTime);
            s2.setStatus(TaskStatus.DONE);
            tm1.updateTask(s2);

            FileBackedTaskManager tm2 = FileBackedTaskManager.loadFromFile(src);

            if (tm1.getAllTasks().size() == tm2.getAllTasks().size()) {
                System.out.println("Количество задач в менеджерах корректно");
            } else {
                System.out.println("Количество задач в менеджерах не совпадает");
            }

            Files.delete(src);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
