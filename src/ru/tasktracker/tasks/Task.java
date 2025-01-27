package ru.tasktracker.tasks;
import ru.tasktracker.TaskManager;

import java.util.Objects;

public class Task {

    private final int id;
    private String title;
    private String description;
    private TaskStatus status = TaskStatus.NEW;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.id = TaskManager.getNextId();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", status=" + status +
               "}";
    }

    public void actionsAfterUpdate() {
    }
}
