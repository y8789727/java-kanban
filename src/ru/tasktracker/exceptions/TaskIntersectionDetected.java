package ru.tasktracker.exceptions;

public class TaskIntersectionDetected extends RuntimeException {
    public TaskIntersectionDetected(String message) {
        super(message);
    }
}
