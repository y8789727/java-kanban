package ru.tasktracker;

import ru.tasktracker.tasks.Task;
import java.util.List;

public interface HistoryManager {
    int MAX_ELEMENTS_IN_HISTORY = 10;

    void add(Task task);
    List<Task> getHistory();
}
