package ru.tasktracker;

import ru.tasktracker.tasks.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryTaskManagerTest {

    @Test
    public void checkTasksCreationAndRemove() {
        TaskManager taskManager = Managers.getDefault();

        Task t1 = taskManager.createTask("Task1", "desc");
        assertNotNull(t1,"Задача не создана");

        Task t2 = taskManager.createTask("Task2", "desc");
        assertNotEquals(t1.getId(), t2.getId(), "Задачи должны иметь разные ID");

        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1);
        assertEquals(1, e1.getSubtasks().size(), "Подзадача не добавлена в эпик");

        assertEquals(4, taskManager.getAllTasks().size(), "Неверное количество задач в менеджере");

        taskManager.removeAllTasks();
        assertEquals(0, taskManager.getAllTasks().size(), "Неверное количество задач в менеджере после удаления всех задач");
    }

    @Test
    public void checkTaskUpdate() {
        TaskManager taskManager = Managers.getDefault();
        Task t1 = taskManager.createTask("Task1", "desc");
        int taskId = t1.getId();
        String t2Title = "Task 2";
        Task t2 = new Task(t2Title, "desc", taskId);
        taskManager.updateTask(t2);

        assertEquals(t2Title, taskManager.getTaskById(taskId).getTitle(), "Задача не обновилась");
    }

    @Test
    public void whenGetTaskByIdHistoryAdded() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager taskManager = new InMemoryTaskManager(historyManager);

        Task t1 = taskManager.createTask("Task1", "desc");
        Task t = taskManager.getTaskById(t1.getId());
        assertEquals(t1, t, "Ошибка поиска задачи по Id");

        assertEquals(1,  historyManager.getHistory().size(), "История получения не записалась");
    }

    @Test
    public void whenEpicRemovedSubtaskRemovedToo() {
        TaskManager taskManager = Managers.getDefault();

        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1);

        assertEquals(2, taskManager.getAllTasks().size(), "Неверное количество задач в менеджере после создания задач");

        taskManager.removeTaskById(e1.getId());
        assertEquals(0, taskManager.getAllTasks().size(), "Неверное количество задач в менеджере после удаления эпика");
    }

    @Test
    public void whenSubtaskRemovedRemoveItFromEpicToo() {
        TaskManager taskManager = Managers.getDefault();

        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1);

        taskManager.removeTaskById(s1.getId());
        assertEquals(0, e1.getSubtasks().size(), "Подзадача не удалилась из эпика после удаления из менеджера");
    }

}