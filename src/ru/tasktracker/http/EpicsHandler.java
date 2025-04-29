package ru.tasktracker.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.tasktracker.TaskManager;
import ru.tasktracker.tasks.Epic;
import ru.tasktracker.tasks.TaskType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicsHandler extends BaseHttpHandler {
    public EpicsHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final String method = exchange.getRequestMethod();
        final String[] pathParts = exchange.getRequestURI().getPath().split("/");

        final Gson gson = HttpTaskServer.getTasksGson();

        if ("GET".equals(method) && pathParts.length == 2) {
            sendText(exchange, gson.toJson(manager.getAllTasks().stream()
                    .filter(t -> TaskType.EPIC.equals(t.getType()))
                    .toList()));
        } else if ("GET".equals(method) && pathParts.length == 3) {
            final var taskOpt = manager.getTaskById(Integer.parseInt(pathParts[2]));
            if (taskOpt.isPresent()) {
                sendText(exchange, gson.toJson(taskOpt.get()));
            } else {
                sendNotFound(exchange);
            }
        } else if ("GET".equals(method) && pathParts.length == 4 && "subtasks".equals(pathParts[3])) {
            final var taskOpt = manager.getTaskById(Integer.parseInt(pathParts[2]));
            if (taskOpt.isPresent()) {
                sendText(exchange, gson.toJson(((Epic)taskOpt.get()).getSubtasks()));
            } else {
                sendNotFound(exchange);
            }
        } else if ("POST".equals(method)) {
            final var epic = gson.fromJson(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), Epic.class);
            if (epic.getId() == 0) {
                manager.createEpic(epic.getTitle(), epic.getDescription());
            } else {
                manager.updateTask(epic);
            }
            sendCreated(exchange);
        } else if ("DELETE".equals(method) && pathParts.length == 3) {
            manager.removeTaskById(Integer.parseInt(pathParts[2]));
            sendOK(exchange);
        } else {
            throw new UnsupportedOperationException("Unsupported tasks request");
        }
    }
}
