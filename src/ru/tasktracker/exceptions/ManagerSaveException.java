package ru.tasktracker.exceptions;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message) {
        super(message);
    }
}
