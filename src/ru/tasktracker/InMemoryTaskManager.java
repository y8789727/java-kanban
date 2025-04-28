package ru.tasktracker;

import ru.tasktracker.exceptions.TaskIntersectionDetected;
import ru.tasktracker.tasks.Task;
import ru.tasktracker.tasks.Epic;
import ru.tasktracker.tasks.Subtask;
import ru.tasktracker.tasks.TaskType;

import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {
    private int idGenerator = 0;

    private final Map<Integer, Task> tasks = new LinkedHashMap<>();
    private final HistoryManager history;
    private final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.history = historyManager;
    }

    @Override
    public Task createTask(String title, String description, Duration duration, LocalDateTime startTime) {
        final var task = new Task(title, description, getNextId(), duration, startTime);
        addTask(task);

        return task;
    }

    @Override
    public Epic createEpic(String title, String description) {
        final var epic = new Epic(title, description, getNextId());
        addTask(epic);

        return epic;
    }

    @Override
    public Subtask createSubtask(String title, String description, Epic epic, Duration duration, LocalDateTime startTime) {
        final var subtask = new Subtask(title, description, getNextId(), epic, duration, startTime);
        addTask(subtask);

        return subtask;
    }

    private void addPrioritizedTask(Task task) {
        if (!TaskType.EPIC.equals(task.getType()) && task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateTask(Task task) {
        if (intersectExists(task)) {
            throw new TaskIntersectionDetected("Задачи не могу пересекаться по периоду");
        }
        tasks.put(task.getId(), task);
        addPrioritizedTask(task);
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Optional<Task> getTaskById(Integer id) {
        Task task = tasks.get(id);
        if (task != null) {
            history.add(task);
        }
        return Optional.ofNullable(task);
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
        history.clearHistory();
        prioritizedTasks.clear();
    }

    @Override
    public void removeTaskById(Integer id) {
        Task task = tasks.get(id);
        if (task != null) {
            tasks.remove(id);
            history.remove(id);
            if (TaskType.SUBTASK.equals(task.getType())) {
                ((Subtask) task).getEpic().removeSubtask((Subtask) task);
            } else if (TaskType.EPIC.equals(task.getType())) {
                ((Epic) task).getSubtasks()
                        .forEach(s -> {
                            tasks.remove(s.getId());
                            history.remove(s.getId());
                        });
            }
            prioritizedTasks.remove(task);
        }
    }

    protected void addTask(Task t) {
        if (intersectExists(t)) {
            throw new TaskIntersectionDetected("Задачи не могу пересекаться по периоду");
        }
        tasks.put(t.getId(), t);
        addPrioritizedTask(t);
    }

    @Override
    public void printAllTasks(PrintStream s) {
        tasks.values().forEach(s::println);

        s.println("История:");
        history.getHistory().forEach(s::println);
    }

    @Override
    public int getNextId() {
        return ++idGenerator;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().toList();
    }

    private boolean intersectExists(final Task task) {
        return prioritizedTasks.stream()
                .anyMatch(t -> t.getId() != task.getId() && t.intersects(task));
    }

    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }
}