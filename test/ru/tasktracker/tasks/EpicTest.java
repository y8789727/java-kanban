package ru.tasktracker.tasks;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpicTest {
    private static final LocalDateTime startTime = LocalDateTime.of(2025,1, 1,10,0); // 01-01-2025 10:00
    private static final Duration duration = Duration.ofMinutes(30);

    @Test
    public void checkSubtaskAddAndRemove() {
        Epic epic = new Epic("Title", "Description", 1);
        Subtask subtask = new Subtask("Subtask title", "Subtask description", 2, epic, duration, startTime);

        assertEquals(1, epic.getSubtasks().size(),"Неверное количество подзадач");
        assertEquals(subtask, epic.getSubtasks().getFirst(),"Подзадача не совпадает");

        epic.removeSubtask(subtask);
        assertTrue(epic.getSubtasks().isEmpty(), "Подзадача не удалена");
    }

    @Test
    public void statusCannotBeChangedDirectly() {
        Epic epic = new Epic("Title", "Description", 1);
        epic.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.NEW, epic.getStatus(),"Статус не может быть изменен напрямую");
    }

    @Test
    public void checkStatusWorkflow() {
        int id = 0;
        Epic epic = new Epic("Title", "Description", ++id);

        Subtask s1 = new Subtask("1", "1", ++id, epic, duration, startTime);
        Subtask s2 = new Subtask("2", "2", ++id, epic, duration, startTime);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Начальный статус должен быть NEW");

        s1.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Для различных статусов подзадач статус эпика должен быть IN_PROGRESS");

        epic.renewStatus();
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Переопределение статуса без изменения статусов подзадач не должно менять статус эпика");

        s2.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, epic.getStatus(), "Все подзадачи DONE, статус эпика должен быть DONE");

        Subtask s3 = new Subtask("3", "3", ++id, epic, duration, startTime);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Добавили подзадачу, статус эпика должен стать IN_PROGRESS");

        epic.removeSubtask(s1);
        epic.removeSubtask(s2);
        epic.removeSubtask(s3);
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Удаление всех подзадач должно перевести статус эпика в NEW");
    }

    @Test
    public void checkTaskTypeValid() {
        Task task = new Epic("Task", "Task desc", -1);
        assertEquals(TaskType.EPIC, task.getType(), "Тип задачи определен некорректно");
    }

    @Test
    public void whenChangeSubtasksEpicTimeIsCorrect() {
        Epic epic = new Epic("Task", "Task desc", -1);

        Subtask s1 = new Subtask("1", "1", -2, epic, Duration.ofMinutes(20), LocalDateTime.of(2025,1,2,10,0));
        Subtask s2 = new Subtask("2", "2", -3, epic, Duration.ofMinutes(30), LocalDateTime.of(2025,1,1,12,0));

        assertEquals(Duration.ofMinutes(50),epic.getDuration(),"Неверная продолжительность эпика");
        assertEquals(LocalDateTime.of(2025,1,1,12,0),epic.getStartTime(),"Неверная дата начала эпика");
        assertEquals(LocalDateTime.of(2025,1,2,10,20),epic.getEndTime(),"Неверная дата завершения эпика");

        epic.removeSubtask(s2);
        assertEquals(Duration.ofMinutes(20),epic.getDuration(),"Неверная продолжительность эпика после удаления подзадачи");
        assertEquals(LocalDateTime.of(2025,1,2,10,0),epic.getStartTime(),"Неверная дата начала эпика после удаления подзадачи");
        assertEquals(LocalDateTime.of(2025,1,2,10,20),epic.getEndTime(),"Неверная дата завершения эпика после удаления подзадачи");
    }
}