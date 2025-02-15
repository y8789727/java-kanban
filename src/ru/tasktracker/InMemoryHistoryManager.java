package ru.tasktracker;

import ru.tasktracker.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        history.addFirst(task);

        while (history.size() > HistoryManager.MAX_ELEMENTS_IN_HISTORY) {
            history.removeLast();
        }
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
