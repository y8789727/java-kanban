package ru.tasktracker.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.tasktracker.TaskManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected TaskManager manager;

    public BaseHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    public void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.length);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.getResponseBody().write(resp);
        h.close();
    }

    public void sendNotFound(HttpExchange h) throws IOException {
        h.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
        h.close();
    }

    public void sendHasInteractions(HttpExchange h) throws IOException {
        h.sendResponseHeaders(HttpURLConnection.HTTP_NOT_ACCEPTABLE, 0);
        h.close();
    }

    public void sendCreated(HttpExchange h) throws IOException {
        h.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, 0);
        h.close();
    }

    public void sendOK(HttpExchange h) throws IOException {
        h.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        h.close();
    }
}
