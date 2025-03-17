package ru.tasktracker;

import ru.tasktracker.tasks.Task;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    void remove(int id);
    void clearHistory();
    List<Task> getHistory();
}
