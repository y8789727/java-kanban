package ru.tasktracker;

import org.junit.jupiter.api.Test;
import ru.tasktracker.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {
    @Test
    public void onlyMAXTaskCanBeInHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task t = new Task("t1", "desc", -1);
        historyManager.add(t);
        historyManager.add(t);
        assertEquals(2, historyManager.getHistory().size(), "Некорректное количество после добавления одной задачи дважды");

        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < HistoryManager.MAX_ELEMENTS_IN_HISTORY; i++) {
            Task newTask = new Task( "task " + i, "desc", i);
            tasks.addFirst(newTask);
            historyManager.add(newTask);
        }
        assertArrayEquals(tasks.toArray(),historyManager.getHistory().toArray(), "История записана некорректно");
    }
}