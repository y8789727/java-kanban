package ru.tasktracker.tasks;

public class Subtask extends Task {

    private final Epic epic;

    public Subtask(String title, String description, Epic epic) {
        super(title, description);
        this.epic = epic;
        epic.addSubtask(this);
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public void actionsAfterUpdate() {
        super.actionsAfterUpdate();
        epic.actionsAfterUpdate();
    }
}
