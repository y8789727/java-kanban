package ru.tasktracker;

import ru.tasktracker.tasks.*;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest {

    private static final Duration duration20 = Duration.ofMinutes(20);

    @Test
    public void whenGetTaskByIdHistoryAdded() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager taskManager = new InMemoryTaskManager(historyManager);

        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,10,0));
        Task t = taskManager.getTaskById(t1.getId()).get();
        assertEquals(t1, t, "Ошибка поиска задачи по Id");

        assertEquals(1,  historyManager.getHistory().size(), "История получения не записалась");
    }

    @Test
    public void whenRemoveTaskHistoryChanged() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager taskManager = new InMemoryTaskManager(historyManager);

        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,10,0));
        Task t2 = taskManager.createTask("Task2", "desc", duration20, LocalDateTime.of(2025,1,1,11,0));
        taskManager.getTaskById(t1.getId());
        taskManager.getTaskById(t2.getId());
        taskManager.removeTaskById(t1.getId());

        List<Task> taskHistory = new ArrayList<>();
        taskHistory.add(t2);

        assertArrayEquals(taskHistory.toArray(),historyManager.getHistory().toArray(), "После удаления задачи запись в истории не удалена");
    }

    @Test
    public void whenRemoveAllTaskHistoryEmpty() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager taskManager = new InMemoryTaskManager(historyManager);

        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,10,0));
        Task t2 = taskManager.createTask("Task2", "desc", duration20, LocalDateTime.of(2025,1,1,11,0));
        taskManager.getTaskById(t1.getId());
        taskManager.getTaskById(t2.getId());
        assertEquals(2,  historyManager.getHistory().size(), "История получения не записалась");

        taskManager.removeAllTasks();
        assertTrue(historyManager.getHistory().isEmpty(), "После очистки всех задач список истории не пуст");
    }

}