package ru.tasktracker.http;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import ru.tasktracker.tasks.Epic;
import ru.tasktracker.tasks.Subtask;
import ru.tasktracker.tasks.Task;
import ru.tasktracker.tasks.TaskType;

import java.lang.reflect.Type;

public class TaskJsonAdapter implements JsonSerializer<Task>, JsonDeserializer<Task> {

    private static final Gson baseGson = HttpTaskServer.createBaseGson();

    @Override
    public JsonElement serialize(Task task, Type type, JsonSerializationContext context) {
        JsonElement element = baseGson.toJsonTree(task, type);
        element.getAsJsonObject().addProperty("taskType", task.getType().name());
        if (TaskType.SUBTASK.equals(task.getType())) {
            element.getAsJsonObject().addProperty("epicId", ((Subtask) task).getEpic().getId());
        }
        return element;
    }

    @Override
    public Task deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        TaskType taskType = TaskType.valueOf(jsonElement.getAsJsonObject().get("taskType").getAsString());

        Type classType = switch (taskType) {
            case TASK -> Task.class;
            case EPIC -> Epic.class;
            case SUBTASK -> Subtask.class;
        };

        return baseGson.fromJson(jsonElement, classType);
    }
}
