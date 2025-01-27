package ru.tasktracker;

import ru.tasktracker.tasks.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private static int idGenerator = 0;

    private final HashMap<Integer, Task> tasks = new HashMap<>();

    public Task createTask(String title, String description) {
        final var task = new Task(title, description);
        addTask(task);

        return task;
    }

    public Epic createEpic(String title, String description) {
        final var epic = new Epic(title, description);
        addTask(epic);

        return epic;
    }

    public Subtask createSubtask(String title, String description, Epic epic) {
        final var subtask = new Subtask(title, description, epic);
        addTask(subtask);

        return subtask;
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
        task.actionsAfterUpdate();
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Task getTaskById(Integer id) {
        return tasks.get(id);
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public void removeTaskById(Integer id) {
        Task task = tasks.get(id);
        if (task != null) {
            tasks.remove(id);
            if (task instanceof Subtask) {
                ((Subtask) task).getEpic().removeSubtask((Subtask) task);
            } else if (task instanceof Epic) {
                for (Subtask subtask : ((Epic) task).getSubtasks()) {
                    tasks.remove(subtask.getId());
                }
            }
        }
    }

    private void addTask(Task t) {
        tasks.put(t.getId(), t);
    }

    public static int getNextId() {
        return ++idGenerator;
    }

    public void printAllTasks(PrintStream s) {
        for (Task task : tasks.values()) {
            s.println(task);
        }
    }
}
