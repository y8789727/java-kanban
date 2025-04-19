package ru.tasktracker.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Epic extends Task {

    private final List<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime = null;

    public Epic(String title, String description, int id) {
        super(title, description, id, null, null);
    }

    public void renewStatus() {
        TaskStatus newEpicStatus;
        if (!subtasks.isEmpty()) {
            final TaskStatus firstStatus = subtasks.getFirst().getStatus();

            if (subtasks.stream()
                    .filter(s -> !firstStatus.equals(s.getStatus()))
                    .findAny()
                    .isEmpty()) {
                newEpicStatus = firstStatus;
            } else {
                newEpicStatus = TaskStatus.IN_PROGRESS;
            }
        } else {
            newEpicStatus = TaskStatus.NEW;
        }

        super.setStatus(newEpicStatus);
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        renewStatus();
        updateSubtasksDuration(subtask, Duration.ZERO);
        updateSubtasksDates(subtask);
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void updateSubtasksDuration(Subtask subtask, Duration oldDuration) {
        if (this.getDuration() == null) {
            super.setDuration(subtask.getDuration());
        } else {
            super.setDuration(this.getDuration().minus(oldDuration).plus(subtask.getDuration()));
        }

        updateSubtasksEndDate(subtask);
    }

    private void updateSubtasksStartDate(Subtask subtask) {
        if (this.getStartTime() == null || this.getStartTime().isAfter(subtask.getStartTime())) {
            super.setStartTime(subtask.getStartTime());
        }
    }

    private void updateSubtasksEndDate(Subtask subtask) {
        if (endTime == null) {
            endTime = subtask.getEndTime();
        } else if (endTime.isBefore(subtask.getEndTime()))  {
            endTime = subtask.getEndTime();
        }
    }

    public void updateSubtasksDates(Subtask subtask) {
        updateSubtasksStartDate(subtask);
        updateSubtasksEndDate(subtask);
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        renewStatus();
        super.setDuration(this.getDuration().minus(subtask.getDuration()));

        if (this.getStartTime().equals(subtask.getStartTime())) {
            super.setStartTime(subtasks.stream()
                    .map(Subtask::getStartTime)
                    .min(Comparator.naturalOrder())
                    .orElse(null));
        }

        if (endTime.equals(subtask.getEndTime())) {
            endTime = subtasks.stream()
                    .map(Subtask::getEndTime)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
        }
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public void setStatus(TaskStatus status) {
        // Status cannot be directly changed for epic
    }

    @Override
    public void setDuration(Duration duration) {
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder(super.toString());
        subtasks.forEach(s -> result.append("\n    ").append(s.toString()));
        return result.toString();
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}
