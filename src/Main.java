import ru.tasktracker.HistoryManager;
import ru.tasktracker.InMemoryTaskManager;
import ru.tasktracker.Managers;
import ru.tasktracker.TaskManager;
import ru.tasktracker.tasks.*;

public class Main {

    public static void main(String[] args) {
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager taskManager = new InMemoryTaskManager(historyManager);

        Task t1 = taskManager.createTask("Task 1", "Description of task 1");
        Task t2 = taskManager.createTask("Task 2", "Description of task 2");

        Epic e1 = taskManager.createEpic("Epic 1", "Description of epic 1");
        Subtask e1s1 = taskManager.createSubtask("Subtask 1 of Epic 1", "Some description", e1);
        Subtask e1s2 = taskManager.createSubtask("Subtask 2 of Epic 1", "Some description", e1);
        Subtask e1s3 = taskManager.createSubtask("Subtask 3 of Epic 1", "Some description", e1);

        Epic e2 = taskManager.createEpic("Epic 2", "Description of epic 2");

        taskManager.getTaskById(t2.getId());
        taskManager.getTaskById(t1.getId());
        taskManager.getTaskById(t2.getId());
        System.out.println("История запросов 1:\n" + historyManager.getHistory() + "\n");

        taskManager.getTaskById(e2.getId());
        taskManager.getTaskById(e1s2.getId());
        taskManager.getTaskById(e1s3.getId());
        taskManager.getTaskById(e1.getId());
        taskManager.getTaskById(e2.getId());
        System.out.println("История запросов 2:\n" + historyManager.getHistory() + "\n");

        taskManager.removeTaskById(t2.getId());
        System.out.println("История запросов 3:\n" + historyManager.getHistory() + "\n");

        taskManager.removeTaskById(e1.getId());
        System.out.println("История запросов 4:\n" + historyManager.getHistory() + "\n");
    }
}
