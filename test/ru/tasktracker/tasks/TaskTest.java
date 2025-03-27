package ru.tasktracker.tasks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {
    @Test
    public void checkBasicClassOperations() {
        String taskTitle1 = "Test title";
        String taskDesc1 = "Test desc";
        int taskId = -1;
        Task t = new Task(taskTitle1, taskDesc1, taskId);

        assertEquals(taskTitle1, t.getTitle(), "Название задачи не совпадает с заданным");

        String newTaskTitle = "New title";
        t.setTitle(newTaskTitle);
        assertEquals(newTaskTitle, t.getTitle(), "Название задачи после изменения не совпадает с заданным");

        assertEquals(taskDesc1, t.getDescription(), "Описание задачи не совпадает с заданным");

        String newTaskDesc = "News desc";
        t.setDescription(newTaskDesc);
        assertEquals(newTaskDesc, t.getDescription(), "Описание задачи после изменения не совпадает с заданным");

        assertEquals(TaskStatus.NEW, t.getStatus(), "Начальный статус задачи не NEW");

        t.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, t.getStatus(), "Статус изменился некорректно");

        assertEquals(taskId, t.getId(), "ID задачи не совпадает с заданным");
    }

    @Test
    public void tasksWithSameIdShouldBeEqual() {
        int taskId = -1;
        Task task = new Task("First task", "First task desc", taskId);
        Task taskSameId = new Task("Second task", "Second task desc", taskId);

        assertEquals(task, taskSameId, "Задачи с одинаковым ID не считаются одной и той же задачей");
    }

    @Test
    public void checkTaskTypeValid() {
        Task task = new Task("Task", "Task desc", -1);
        assertEquals(TaskType.TASK, task.getType(), "Тип задачи определен некорректно");
    }
}