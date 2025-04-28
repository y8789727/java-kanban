package ru.tasktracker.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.tasktracker.TaskManager;
import ru.tasktracker.exceptions.TaskIntersectionDetected;
import ru.tasktracker.tasks.Task;
import ru.tasktracker.tasks.TaskType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler {
    public TasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final String method = exchange.getRequestMethod();
        final String[] pathParts = exchange.getRequestURI().getPath().split("/");

        final Gson gson = HttpTaskServer.getTasksGson();
        if ("GET".equals(method) && pathParts.length == 2) {
            sendText(exchange, gson.toJson(manager.getAllTasks().stream()
                    .filter(t -> TaskType.TASK.equals(t.getType()))
                    .toList()));
        } else if ("GET".equals(method) && pathParts.length == 3) {
            final var taskOpt = manager.getTaskById(Integer.parseInt(pathParts[2]));
            if (taskOpt.isPresent()) {
                sendText(exchange, gson.toJson(taskOpt.get()));
            } else {
                sendNotFound(exchange);
            }
        } else if ("POST".equals(method)) {
            final var task = gson.fromJson(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), Task.class);
            try {
                if (task.getId() == 0) {
                    manager.createTask(task.getTitle(), task.getDescription(), task.getDuration(), task.getStartTime());
                } else {
                    manager.updateTask(task);
                }
                sendCreated(exchange);
            } catch (TaskIntersectionDetected e) {
                sendHasInteractions(exchange);
            }
        } else if ("DELETE".equals(method) && pathParts.length == 3) {
            manager.removeTaskById(Integer.parseInt(pathParts[2]));
            sendOK(exchange);
        } else {
            throw new IOException("Unsupported tasks request");
        }
    }

}
