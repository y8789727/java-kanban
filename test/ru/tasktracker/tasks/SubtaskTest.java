package ru.tasktracker.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {

    @Test
    public void checkBasicClassOperations() {
        Epic epic = new Epic("Epic title", "Epic desc", 10);
        String subtaskTitle = "Test title";
        String subtaskDesc = "Test desc";
        int taskId = 1;
        Subtask subtask = new Subtask(subtaskTitle, subtaskDesc, taskId, epic);

        assertEquals(subtaskTitle, subtask.getTitle(), "Название задачи не совпадает с заданным");
        assertEquals(subtaskDesc, subtask.getDescription(), "Описание задачи не совпадает с заданным");
        assertEquals(TaskStatus.NEW, subtask.getStatus(), "Начальный статус задачи не NEW");
        assertEquals(taskId, subtask.getId(), "ID задачи не совпадает с заданным");
        assertEquals(epic, subtask.getEpic(), "Эпик установлен некорректно");
    }

    @Test
    public void afterSubtaskCreatedShouldAddToEpicSubtasks() {
        Epic epic = new Epic("Epic title", "Epic desc", 10);
        Subtask subtask = new Subtask("Test title", "Test desc", 1, epic);

        assertEquals(subtask, epic.getSubtasks().getFirst(), "Подзадача не добавлена в эпик");
    }

    @Test
    public void whenStatusUpdatedToDoneEpicDone() {
        Epic epic = new Epic("Epic title", "Epic desc", 10);
        new Subtask("Test title", "Test desc", 1, epic);

        for (Subtask s : epic.getSubtasks()) {
            s.setStatus(TaskStatus.DONE);
        }
        assertEquals(TaskStatus.DONE, epic.getStatus(), "Статус эпика не изменился на DONE");
    }

    @Test
    public void checkTaskTypeValid() {
        Epic epic = new Epic("Epic title", "Epic desc", 10);
        Task task = new Subtask("Task", "Task desc", -1, epic);
        assertEquals(TaskType.SUBTASK, task.getType(), "Тип задачи определен некорректно");
    }
}