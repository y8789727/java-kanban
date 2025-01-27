import ru.tasktracker.TaskManager;
import ru.tasktracker.tasks.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = new TaskManager();
        Task t1 = taskManager.createTask("Task 1", "Description of task 1");
        Task t2 = taskManager.createTask("Task 2", "Description of task 2");
        Epic e1 = taskManager.createEpic("Epic 1", "Description of epic 1");
        Subtask e1s1 = taskManager.createSubtask("Subtask 1 of Epic 1", "Some description", e1);
        Subtask e1s2 = taskManager.createSubtask("Subtask 2 of Epic 1", "Some description", e1);
        Epic e2 = taskManager.createEpic("Epic 2", "Description of epic 2");
        Subtask e2s1 = taskManager.createSubtask("Subtask 1 of Epic 2", "Some description", e2);
        System.out.println("Исходные задачи:");
        taskManager.printAllTasks(System.out);
        System.out.println();

        t2.setStatus(TaskStatus.DONE);
        taskManager.updateTask(t2);
        e1s2.setTitle("Updated subtask");
        e1s2.setStatus(TaskStatus.DONE);
        taskManager.updateTask(e1s2);
        System.out.println("Задачи после изменения:");
        taskManager.printAllTasks(System.out);
        System.out.println();

        taskManager.removeTaskById(t1.getId());
        taskManager.removeTaskById(e2.getId());
        taskManager.removeTaskById(e1s1.getId());
        System.out.println("Задачи после удаления некоторых:");
        taskManager.printAllTasks(System.out);
        System.out.println();
    }
}
