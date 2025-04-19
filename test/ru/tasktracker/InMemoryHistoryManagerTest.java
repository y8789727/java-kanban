package ru.tasktracker;

import org.junit.jupiter.api.Test;
import ru.tasktracker.tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private static final Duration duration20 = Duration.ofMinutes(20);

    @Test
    public void taskInHistoryAppearsOnlyOnce() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task t1 = new Task("t1", "desc", -1, duration20, LocalDateTime.of(2025,1,1,10,0));
        Task t2 = new Task("t2", "desc", -2, duration20, LocalDateTime.of(2025,1,1,11,0));
        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t1);
        assertEquals(2, historyManager.getHistory().size(), "Некорректное количество после добавления одной задачи дважды");

        List<Task> tasks = new ArrayList<>();
        tasks.add(t1);
        tasks.add(t2);
        assertArrayEquals(tasks.toArray(),historyManager.getHistory().toArray(), "История записана некорректно");
    }

    @Test
    public void checkRemoveTasksFromHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task t1 = new Task("t1", "desc", -1, duration20, LocalDateTime.of(2025,1,1,10,0));
        Task t2 = new Task("t2", "desc", -2, duration20, LocalDateTime.of(2025,1,1,11,0));
        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.remove(t1.getId());
        List<Task> tasks = new ArrayList<>();
        tasks.add(t2);
        assertArrayEquals(tasks.toArray(),historyManager.getHistory().toArray(), "После удаления задачи история записана некорректно");

        historyManager.remove(t2.getId());
        assertTrue(historyManager.getHistory().isEmpty(), "После удаления всех задачи из истории список не пуст");
    }

    @Test
    public void deleteNotExistentTaskFromHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task t1 = new Task("t1", "desc", -1, duration20, LocalDateTime.of(2025,1,1,10,0));
        Task t2 = new Task("t2", "desc", -2, duration20, LocalDateTime.of(2025,1,1,11,0));
        historyManager.add(t1);
        historyManager.add(t2);
        List<Task> tasks = new ArrayList<>();
        tasks.add(t2);
        tasks.add(t1);

        historyManager.remove(-99);
        assertArrayEquals(tasks.toArray(),historyManager.getHistory().toArray(), "После удаления несуществующей задачи история записана некорректно");
    }

    @Test
    public void checkClearHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task t1 = new Task("t1", "desc", -1, duration20, LocalDateTime.of(2025,1,1,10,0));
        Task t2 = new Task("t2", "desc", -2, duration20, LocalDateTime.of(2025,1,1,11,0));
        historyManager.add(t1);
        historyManager.add(t2);

        historyManager.clearHistory();
        assertTrue(historyManager.getHistory().isEmpty(), "После очистки истории список не пуст");
    }
}