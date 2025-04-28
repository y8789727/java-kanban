package ru.tasktracker.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tasktracker.HistoryManager;
import ru.tasktracker.InMemoryHistoryManager;
import ru.tasktracker.InMemoryTaskManager;
import ru.tasktracker.TaskManager;
import ru.tasktracker.tasks.Epic;
import ru.tasktracker.tasks.Subtask;
import ru.tasktracker.tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {

    private static final String HOST = "http://localhost:" + HttpTaskServer.DEFAULT_PORT;
    private static final Duration duration20 = Duration.ofMinutes(20);
    private static HistoryManager history;
    private static TaskManager taskManager;
    private static HttpTaskServer taskServer;
    private static final Gson gson = HttpTaskServer.getTasksGson();

    private static class TaskListTypeToken extends TypeToken<List<? extends Task>> {
    }

    @BeforeAll
    public static void initTaskServer() {
        history = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager(history);
        try {
            taskServer = new HttpTaskServer(taskManager, HttpTaskServer.DEFAULT_PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        taskServer.start();
    }

    @BeforeEach
    public void prepareServer() {
        taskManager.removeAllTasks();
        history.clearHistory();
    }

    @AfterAll
    public static void stopServer() {
        taskServer.stop();
    }

    @Test
    public void checkHistoryAfterTasksAdded() throws IOException, InterruptedException {

        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,10,0));
        Task t2 = taskManager.createTask("Task2", "desc", duration20, LocalDateTime.of(2025,1,1,11,0));
        taskManager.getTaskById(t1.getId());
        taskManager.getTaskById(t2.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/history"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        List<Task> historyResponse = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(2, historyResponse.size(), "Некорректное количество задач в истории");
    }

    @Test
    public void checkPrioritizedTasks() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));

        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        Task t2 = taskManager.createTask("Task1", "desc", duration20, null);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/prioritized"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        List<Task> prioritizedResponse = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        Task[] expected = new Task[2];
        expected[0] = s1;
        expected[1] = t1;

        assertArrayEquals(expected, prioritizedResponse.toArray(), "Неверное значение списка задач с приоритетом");
    }

    @Test
    public void whenTasksAddedTasksReturned() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));
        Task t2 = taskManager.createTask("Task1", "desc", duration20, null);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/tasks"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        List<Task> tasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        Task[] expected = new Task[2];
        expected[0] = t1;
        expected[1] = t2;

        assertArrayEquals(expected, tasks.toArray(), "Неверное значение списка задач");
    }

    @Test
    public void whenFindTaskByIdTaskReturned() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/tasks/" + t1.getId()))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        Task resultTask = gson.fromJson(response.body(), Task.class);
        assertEquals(t1, resultTask, "Задачи не совпадают");
    }

    @Test
    public void whenFindTaskByIdNotExists() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/tasks/-999"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(404, response.statusCode(), "Неверный код ответа");
    }

    @Test
    public void whenCreateTaskThenTaskCreated() throws IOException, InterruptedException {
        var body = HttpRequest.BodyPublishers.ofString("{\"title\":\"SomeTask\",\"description\":\"desc\",\"status\":\"NEW\",\"duration\":20,\"startTime\":\"01012025100000\",\"taskType\":\"TASK\"}", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/tasks"))
                .header("Accept", "application/json")
                .POST(body)
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(201, response.statusCode(), "Неверный код ответа");
        assertEquals(1, taskManager.getAllTasks().size(), "Неверное количество задач");
    }

    @Test
    public void whenUpdateTaskThenTaskUpdated() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));

        var body = HttpRequest.BodyPublishers.ofString("{\"id\":" + t1.getId() + ",\"title\":\"SomeTask\",\"description\":\"desc\",\"status\":\"NEW\",\"duration\":20,\"startTime\":\"01012025100000\",\"taskType\":\"TASK\"}", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/tasks"))
                .header("Accept", "application/json")
                .POST(body)
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(201, response.statusCode(), "Неверный код ответа");
        assertEquals("SomeTask", taskManager.getTaskById(t1.getId()).get().getTitle(), "Задача не обновилась");
        assertEquals(1, taskManager.getAllTasks().size(), "Неверное количество задач");
    }

    @Test
    public void whenTasksIntersectShouldReturnErrorCode() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));

        var body = HttpRequest.BodyPublishers.ofString("{\"title\":\"SomeTask\",\"description\":\"desc\",\"status\":\"NEW\",\"duration\":20,\"startTime\":\"01012025121000\",\"taskType\":\"TASK\"}", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/tasks"))
                .header("Accept", "application/json")
                .POST(body)
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(406, response.statusCode(), "Неверный код ответа");
    }

    @Test
    public void whenDeleteTaskThenTaskDeleted() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/tasks/" + t1.getId()))
                .header("Accept", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(0, taskManager.getAllTasks().size(), "Неверное количество задач");
    }

    @Test
    public void whenEpicAddedEpicsReturned() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));
        Task t2 = taskManager.createTask("Task1", "desc", duration20, null);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/epics"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        List<Epic> tasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        Epic[] expected = new Epic[1];
        expected[0] = e1;

        assertArrayEquals(expected, tasks.toArray(), "Неверное значение списка эпиков");
    }

    @Test
    public void whenFindEpicByIdEpicReturned() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/epics/" + e1.getId()))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        Epic resultTask = gson.fromJson(response.body(), Epic.class);
        assertEquals(e1, resultTask, "Задачи не совпадают");
    }

    @Test
    public void whenFindEpicByIdNotExists() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/epics/-999"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(404, response.statusCode(), "Неверный код ответа");
    }

    @Test
    public void whenFindEpicSubtasks() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/epics/" + e1.getId() + "/subtasks"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        List<Subtask> tasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        Subtask[] expected = new Subtask[1];
        expected[0] = s1;

        assertArrayEquals(expected, tasks.toArray(), "Неверное значение списка эпиков");
    }

    @Test
    public void whenCreateEpicThenEpicCreated() throws IOException, InterruptedException {
        var body = HttpRequest.BodyPublishers.ofString("{\n" +
                "    \"title\": \"Epic\",\n" +
                "    \"description\": \"desc\",\n" +
                "    \"taskType\": \"EPIC\"\n" +
                "  }", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/epics"))
                .header("Accept", "application/json")
                .POST(body)
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(201, response.statusCode(), "Неверный код ответа");
        assertEquals(1, taskManager.getAllTasks().size(), "Неверное количество задач");
    }

    @Test
    public void whenUpdateEpicThenEpicUpdated() throws IOException, InterruptedException {
        Epic e1 = taskManager.createEpic("Epic", "desc");

        var body = HttpRequest.BodyPublishers.ofString("{\n" +
                "    \"id\": " + e1.getId() + ",\n" +
                "    \"title\": \"New Title\",\n" +
                "    \"description\": \"desc\",\n" +
                "    \"taskType\": \"EPIC\"\n" +
                "  }", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/tasks/" + e1.getId()))
                .header("Accept", "application/json")
                .POST(body)
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(201, response.statusCode(), "Неверный код ответа");
        assertEquals("New Title", taskManager.getTaskById(e1.getId()).get().getTitle(), "Задача не обновилась");
        assertEquals(1, taskManager.getAllTasks().size(), "Неверное количество задач");
    }
    @Test
    public void whenDeleteEpicThenEpicDeleted() throws IOException, InterruptedException {
        Epic e1 = taskManager.createEpic("Epic", "desc");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/epics/" + e1.getId()))
                .header("Accept", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(0, taskManager.getAllTasks().size(), "Неверное количество задач");
    }

    @Test
    public void whenSubtasksAddedSubtasksReturned() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));
        Task t2 = taskManager.createTask("Task1", "desc", duration20, null);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/subtasks"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        List<Subtask> tasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        Subtask[] expected = new Subtask[1];
        expected[0] = s1;

        assertArrayEquals(expected, tasks.toArray(), "Неверное значение списка задач");
    }

    @Test
    public void whenFindSubtaskByIdSubtaskReturned() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/subtasks/" + s1.getId()))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        Subtask resultTask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(s1, resultTask, "Задачи не совпадают");
    }

    @Test
    public void whenFindSubtaskByIdNotExists() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOST + "/subtasks/-999"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(404, response.statusCode(), "Неверный код ответа");
    }

    @Test
    public void whenCreateSubtaskThenSubtaskCreated() throws IOException, InterruptedException {
        Epic e1 = taskManager.createEpic("Epic", "desc");

        var body = HttpRequest.BodyPublishers.ofString("{\n" +
                "    \"title\": \"Subtask 1\",\n" +
                "    \"description\": \"desc\",\n" +
                "    \"status\": \"NEW\",\n" +
                "    \"duration\": 20,\n" +
                "    \"startTime\": \"02012025100000\",\n" +
                "    \"taskType\": \"SUBTASK\",\n" +
                "    \"epicId\": " + e1.getId() + "\n" +
                "  }", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/subtasks"))
                .header("Accept", "application/json")
                .POST(body)
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(201, response.statusCode(), "Неверный код ответа");
        assertEquals(1, e1.getSubtasks().size(), "Подзадача не создана в эпике");
    }

    @Test
    public void whenUpdateSubtaskThenSubtaskUpdated() throws IOException, InterruptedException {
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        var body = HttpRequest.BodyPublishers.ofString("{\n" +
                "    \"id\": " + s1.getId() + ",\n" +
                "    \"title\": \"New title\",\n" +
                "    \"description\": \"desc\",\n" +
                "    \"status\": \"NEW\",\n" +
                "    \"duration\": 20,\n" +
                "    \"startTime\": \"02012025100000\",\n" +
                "    \"taskType\": \"SUBTASK\",\n" +
                "    \"epicId\": " + s1.getEpic().getId() + "\n" +
                "  }", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/subtasks"))
                .header("Accept", "application/json")
                .POST(body)
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(201, response.statusCode(), "Неверный код ответа");
        assertEquals("New title", taskManager.getTaskById(s1.getId()).get().getTitle(), "Задача не обновилась");
        assertEquals(2, taskManager.getAllTasks().size(), "Неверное количество задач");
    }

    @Test
    public void whenSubtasksIntersectShouldReturnErrorCode() throws IOException, InterruptedException {
        Task t1 = taskManager.createTask("Task1", "desc", duration20, LocalDateTime.of(2025,1,1,12,0));
        Epic e1 = taskManager.createEpic("Epic", "desc");

        var body = HttpRequest.BodyPublishers.ofString("{\n" +
                "    \"title\": \"Subtask title\",\n" +
                "    \"description\": \"desc\",\n" +
                "    \"status\": \"NEW\",\n" +
                "    \"duration\": 20,\n" +
                "    \"startTime\": \"01012025121000\",\n" +
                "    \"taskType\": \"SUBTASK\",\n" +
                "    \"epicId\": " + e1.getId() + "\n" +
                "  }", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/subtasks"))
                .header("Accept", "application/json")
                .POST(body)
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(406, response.statusCode(), "Неверный код ответа");
    }

    @Test
    public void whenDeleteSubtaskThenTaskDeleted() throws IOException, InterruptedException {
        Epic e1 = taskManager.createEpic("Epic", "desc");
        Subtask s1 = taskManager.createSubtask("Subtask 1", "desc", e1, duration20, LocalDateTime.of(2025,1,1,10,0));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/subtasks/" + s1.getId()))
                .header("Accept", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(0, e1.getSubtasks().size(), "Неверное количество задач в эпике");
        assertEquals(1, taskManager.getAllTasks().size(), "Неверное количество задач");
    }
}