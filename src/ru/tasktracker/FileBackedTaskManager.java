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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    final Path file;

    public static final String CSV_FILE_HEADER = "id,type,name,status,description,epic";

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
                writer.write(taskToCSVLine(t));
            }

            writer.flush();
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    private static String taskToCSVLine(Task t) {
        String epicId = "";
        if (TaskType.SUBTASK.equals(t.getType())) {
            epicId = Integer.toString(((Subtask) t).getEpic().getId());
        }

        return String.format("%d,%s,%s,%s,%s,%s", t.getId(), t.getType(), t.getTitle(), t.getStatus(), t.getDescription(), epicId);
    }

    private static Task CSVLineToTask(String line, Map<Integer, Epic> epics) {
        final Task task;

        String[] columns = line.split(",", 6);
        int i = 0;
        final int taskId = Integer.parseInt(columns[i++]);
        final TaskType taskType = TaskType.valueOf(columns[i++]);
        final String taskTitle = columns[i++];
        final TaskStatus taskStatus = TaskStatus.valueOf(columns[i++]);
        final String taskDescription = columns[i++];
        Integer epicId = null;
        if (!columns[i].isEmpty()) {
            epicId = Integer.parseInt(columns[i]);
        }

        if (TaskType.TASK.equals(taskType)) {
            task = new Task(taskTitle, taskDescription, taskId);
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

            task = new Subtask(taskTitle, taskDescription, taskId, epic);
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
                    fileTaskManager.addTask(CSVLineToTask(reader.readLine(), epics));
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

            FileBackedTaskManager tm1 = new FileBackedTaskManager(Managers.getDefaultHistory(), src);
            tm1.createTask("t1", "t1d");
            Epic e = tm1.createEpic("e1", "e1d");
            tm1.createSubtask("e1s1", "e1s1d", e);
            Subtask s2 = tm1.createSubtask("e1s2", "e1s2d", e);
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
