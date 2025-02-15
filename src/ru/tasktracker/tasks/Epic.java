package ru.tasktracker.tasks;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Subtask> subtasks = new ArrayList<>();

    public Epic(String title, String description, int id) {
        super(title, description, id);
    }

    public void renewStatus() {
        TaskStatus newEpicStatus;
        if (!subtasks.isEmpty()) {
            newEpicStatus = subtasks.getFirst().getStatus();
            for (Subtask subtask : subtasks) {
                if (!newEpicStatus.equals(subtask.getStatus())) {
                    newEpicStatus = TaskStatus.IN_PROGRESS;
                    break;
                }
            }
        } else {
            newEpicStatus = TaskStatus.NEW;
        }

        super.setStatus(newEpicStatus);
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        renewStatus();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        renewStatus();
    }

    @Override
    public void setStatus(TaskStatus status) {
        // Status cannot be directly changed for epic
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        for (Subtask subtask : subtasks) {
            result.append("\n    ").append(subtask.toString());
        }
        return result.toString();
    }
}
