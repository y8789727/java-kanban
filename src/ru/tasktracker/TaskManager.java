package ru.tasktracker;

import ru.tasktracker.tasks.Epic;
import ru.tasktracker.tasks.Subtask;
import ru.tasktracker.tasks.Task;

import java.io.PrintStream;
import java.util.List;

public interface TaskManager {
    Task createTask(String title, String description);

    Epic createEpic(String title, String description);

    Subtask createSubtask(String title, String description, Epic epic);

    void updateTask(Task task);

    int getNextId();

    List<Task> getAllTasks();

    Task getTaskById(Integer id);

    void removeAllTasks();

    void removeTaskById(Integer id);

    void printAllTasks(PrintStream s);
}
