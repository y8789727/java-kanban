package ru.tasktracker.tasks;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {
    private static final LocalDateTime startTime = LocalDateTime.of(2025,1, 1,10,0); // 01-01-2025 10:00
    private static final Duration duration = Duration.ofMinutes(30);

    @Test
    public void checkBasicClassOperations() {
        String taskTitle1 = "Test title";
        String taskDesc1 = "Test desc";
        int taskId = -1;
        Task t = new Task(taskTitle1, taskDesc1, taskId, duration, startTime);

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

        LocalDateTime endTime = LocalDateTime.of(2025,1,1,10,30);
        assertEquals(endTime, t.getEndTime(), "Время окончании задачи некорректно");
    }

    @Test
    public void tasksWithSameIdShouldBeEqual() {
        int taskId = -1;
        Task task = new Task("First task", "First task desc", taskId, duration, startTime);
        Task taskSameId = new Task("Second task", "Second task desc", taskId, duration, startTime);

        assertEquals(task, taskSameId, "Задачи с одинаковым ID не считаются одной и той же задачей");
    }

    @Test
    public void checkTaskTypeValid() {
        Task task = new Task("Task", "Task desc", -1, duration, startTime);
        assertEquals(TaskType.TASK, task.getType(), "Тип задачи определен некорректно");
    }
}