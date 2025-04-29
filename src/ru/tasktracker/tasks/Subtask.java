package ru.tasktracker.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private final transient Epic epic;
    private final int epicId;

    public Subtask(String title, String description, int id, Epic epic, Duration duration, LocalDateTime startTime) {
        super(title, description, id, duration, startTime);
        this.epic = epic;
        this.epicId = epic.getId();
        epic.addSubtask(this);
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public void setStatus(TaskStatus status) {
        super.setStatus(status);
        epic.renewStatus();
    }

    @Override
    public void setDuration(Duration duration) {
        Duration oldDuration = this.getDuration();
        super.setDuration(duration);
        epic.updateSubtasksDuration(this, oldDuration);
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        super.setStartTime(startTime);
        epic.updateSubtasksDates(this);
    }

    public int getEpicId() {
        return epicId;
    }
}
