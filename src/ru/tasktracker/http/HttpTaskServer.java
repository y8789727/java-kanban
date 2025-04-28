package ru.tasktracker.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpServer;
import ru.tasktracker.Managers;
import ru.tasktracker.TaskManager;
import ru.tasktracker.tasks.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HttpTaskServer {
    public static final int DEFAULT_PORT = 8080;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
    private static final Gson gsonTasks = createTasksGson();

    private final HttpServer server;

    public HttpTaskServer(TaskManager manager, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/tasks", new TasksHandler(manager));
        server.createContext("/subtasks", new SubtasksHandler(manager));
        server.createContext("/epics", new EpicsHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    static Gson createBaseGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Duration.class, new TypeAdapter<Duration>() {
                    @Override
                    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
                        jsonWriter.value(duration.toMinutes());
                    }

                    @Override
                    public Duration read(JsonReader jsonReader) throws IOException {
                        return Duration.ofMinutes(jsonReader.nextLong());
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
                        if (localDateTime != null) {
                            jsonWriter.value(localDateTime.format(dateTimeFormatter));
                        } else {
                            jsonWriter.value("");
                        }
                    }

                    @Override
                    public LocalDateTime read(JsonReader jsonReader) throws IOException {
                        String value = jsonReader.nextString();
                        if (value.isEmpty()) {
                            return null;
                        }
                        return LocalDateTime.parse(value, dateTimeFormatter);
                    }
                })
                .create();
    }

    private static Gson createTasksGson() {
        return createBaseGson().newBuilder()
                .registerTypeHierarchyAdapter(Task.class, new TaskJsonAdapter())
                .create();
    }

    public static Gson getTasksGson() {
        return gsonTasks;
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public static void main(String[] args) {
        HttpTaskServer taskServer;
        TaskManager taskManager = Managers.getDefault();
        try {
            taskServer = new HttpTaskServer(taskManager, DEFAULT_PORT);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        taskServer.start();
    }
}
