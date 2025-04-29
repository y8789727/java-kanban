package ru.tasktracker.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.tasktracker.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final String method = exchange.getRequestMethod();

        if ("GET".equals(method)) {
            final Gson gson = HttpTaskServer.getTasksGson();
            sendText(exchange, gson.toJson(manager.getPrioritizedTasks()));
        }
    }
}
