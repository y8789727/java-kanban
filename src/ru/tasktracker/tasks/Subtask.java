package ru.tasktracker.tasks;

public class Subtask extends Task {

    private final Epic epic;

    public Subtask(String title, String description, int id, Epic epic) {
        super(title, description, id);
        this.epic = epic;
        epic.addSubtask(this);
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public void setStatus(TaskStatus status) {
        super.setStatus(status);
        epic.renewStatus();
    }
}
