package ru.tasktracker;

import ru.tasktracker.tasks.*;

import java.io.PrintStream;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int idGenerator = 0;

    private final Map<Integer, Task> tasks = new LinkedHashMap<>();
    private final HistoryManager history;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.history = historyManager;
    }

    @Override
    public Task createTask(String title, String description) {
        final var task = new Task(title, description, getNextId());
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
    public Subtask createSubtask(String title, String description, Epic epic) {
        final var subtask = new Subtask(title, description, getNextId(), epic);
        addTask(subtask);

        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = tasks.get(id);
        history.add(task);
        return task;
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
        history.clearHistory();
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
                for (Subtask subtask : ((Epic) task).getSubtasks()) {
                    tasks.remove(subtask.getId());
                    history.remove(subtask.getId());
                }
            }
        }
    }

    protected void addTask(Task t) {
        tasks.put(t.getId(), t);
    }

    @Override
    public void printAllTasks(PrintStream s) {
        for (Task task : tasks.values()) {
            s.println(task);
        }

        s.println("История:");
        for (Task hist : history.getHistory()) {
            s.println(hist);
        }
    }

    @Override
    public int getNextId() {
        return ++idGenerator;
    }
}