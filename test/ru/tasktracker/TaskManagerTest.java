package ru.tasktracker;

import org.junit.jupiter.api.Test;

import ru.tasktracker.exceptions.TaskIntersectionDetected;
import ru.tasktracker.tasks.*;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {

    private static final Duration duration20 = Duration.ofMinutes(20);

    @Test
    public void checkTasksCreationAndRemove() {
        TaskManager taskManager = Managers.getDefault();

        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,10,0));
        assertNotNull(t1,"Задача не создана");

        Task t2 = taskManager.createTask("Task2", "desc", duration20, LocalDateTime.of(2025,1,1,11,0));
        assertNotEquals(t1.getId(), t2.getId(), "Задачи должны иметь разные ID");

        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,12,0));
        assertEquals(1, e1.getSubtasks().size(), "Подзадача не добавлена в эпик");

        assertEquals(4, taskManager.getAllTasks().size(), "Неверное количество задач в менеджере");

        taskManager.removeAllTasks();
        assertEquals(0, taskManager.getAllTasks().size(), "Неверное количество задач в менеджере после удаления всех задач");

    }

    @Test
    public void checkTaskUpdate() {
        TaskManager taskManager = Managers.getDefault();
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,10,0));
        int taskId = t1.getId();
        String t2Title = "Task 2";
        Task t2 = new Task(t2Title, "desc", taskId, duration20, LocalDateTime.of(2025,1,1,11,0));
        taskManager.updateTask(t2);

        assertEquals(t2Title, taskManager.getTaskById(taskId).get().getTitle(), "Задача не обновилась");
    }

    @Test
    public void whenEpicRemovedSubtaskRemovedToo() {
        TaskManager taskManager = Managers.getDefault();

        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        assertEquals(2, taskManager.getAllTasks().size(), "Неверное количество задач в менеджере после создания задач");

        taskManager.removeTaskById(e1.getId());
        assertEquals(0, taskManager.getAllTasks().size(), "Неверное количество задач в менеджере после удаления эпика");
    }

    @Test
    public void whenSubtaskRemovedRemoveItFromEpicToo() {
        TaskManager taskManager = Managers.getDefault();

        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        taskManager.removeTaskById(s1.getId());
        assertEquals(0, e1.getSubtasks().size(), "Подзадача не удалилась из эпика после удаления из менеджера");
    }

    @Test
    public void checkPrioritizedTasks() {
        TaskManager taskManager = Managers.getDefault();
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));

        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        Task t2 = taskManager.createTask("Task1", "desc", duration20, null);

        Task[] expected = new Task[2];
        expected[0] = s1;
        expected[1] = t1;

        assertArrayEquals(expected, taskManager.getPrioritizedTasks().toArray(), "Неверное значение списка задач с приоритетом");
    }

    @Test
    public void whenTasksIntersectExceptionThrown() {
        TaskManager taskManager = Managers.getDefault();
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));

        Epic e1 = taskManager.createEpic("Epic", "desc");

        assertThrows(TaskIntersectionDetected.class, () -> {
                Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,12,10));
        });
    }
}