package ru.tasktracker;

import ru.tasktracker.tasks.Epic;
import ru.tasktracker.tasks.Subtask;
import ru.tasktracker.tasks.Task;

import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskManager {
    Task createTask(String title, String description, Duration duration, LocalDateTime startTime);

    Epic createEpic(String title, String description);

    Subtask createSubtask(String title, String description, Epic epic, Duration duration, LocalDateTime startTime);

    void updateTask(Task task);

    int getNextId();

    List<Task> getAllTasks();

    Optional<Task> getTaskById(Integer id);

    void removeAllTasks();

    void removeTaskById(Integer id);

    void printAllTasks(PrintStream s);

    List<Task> getPrioritizedTasks();

    List<Task> getHistory();
}
