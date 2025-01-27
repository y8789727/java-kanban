package ru.tasktracker.tasks;

import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    private void renewStatus() {
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

        this.setStatus(newEpicStatus);
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        renewStatus();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        renewStatus();
    }

    @Override
    public void actionsAfterUpdate() {
        super.actionsAfterUpdate();
        renewStatus();
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
