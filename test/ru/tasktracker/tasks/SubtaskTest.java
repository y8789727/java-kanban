package ru.tasktracker.tasks;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {
    private static final LocalDateTime startTime = LocalDateTime.of(2025,1, 1,10,0); // 01-01-2025 10:00
    private static final Duration duration = Duration.ofMinutes(30);

    @Test
    public void checkBasicClassOperations() {
        Epic epic = new Epic("Epic title", "Epic desc", 10);
        String subtaskTitle = "Test title";
        String subtaskDesc = "Test desc";
        int taskId = 1;
        Subtask subtask = new Subtask(subtaskTitle, subtaskDesc, taskId, epic, duration, startTime);

        assertEquals(subtaskTitle, subtask.getTitle(), "Название задачи не совпадает с заданным");
        assertEquals(subtaskDesc, subtask.getDescription(), "Описание задачи не совпадает с заданным");
        assertEquals(TaskStatus.NEW, subtask.getStatus(), "Начальный статус задачи не NEW");
        assertEquals(taskId, subtask.getId(), "ID задачи не совпадает с заданным");
        assertEquals(epic, subtask.getEpic(), "Эпик установлен некорректно");
    }

    @Test
    public void afterSubtaskCreatedShouldAddToEpicSubtasks() {
        Epic epic = new Epic("Epic title", "Epic desc", 10);
        Subtask subtask = new Subtask("Test title", "Test desc", 1, epic, duration, startTime);

        assertEquals(subtask, epic.getSubtasks().getFirst(), "Подзадача не добавлена в эпик");
    }

    @Test
    public void whenStatusUpdatedToDoneEpicDone() {
        Epic epic = new Epic("Epic title", "Epic desc", 10);
        new Subtask("Test title", "Test desc", 1, epic, duration, startTime);

        epic.getSubtasks().forEach(s -> s.setStatus(TaskStatus.DONE));
        assertEquals(TaskStatus.DONE, epic.getStatus(), "Статус эпика не изменился на DONE");
    }

    @Test
    public void checkTaskTypeValid() {
        Epic epic = new Epic("Epic title", "Epic desc", 10);
        Task task = new Subtask("Task", "Task desc", -1, epic, duration, startTime);
        assertEquals(TaskType.SUBTASK, task.getType(), "Тип задачи определен некорректно");
    }
}