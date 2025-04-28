package ru.tasktracker.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.tasktracker.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final String method = exchange.getRequestMethod();

        if ("GET".equals(method)) {
            final Gson gson = HttpTaskServer.getTasksGson();
            sendText(exchange, gson.toJson(manager.getHistory()));
        }

    }
}
