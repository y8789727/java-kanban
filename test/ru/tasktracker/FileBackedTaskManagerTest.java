package ru.tasktracker;

import org.junit.jupiter.api.Test;
import ru.tasktracker.tasks.Epic;
import ru.tasktracker.tasks.Subtask;
import ru.tasktracker.tasks.Task;
import ru.tasktracker.tasks.TaskStatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @Test
    public void checkEmptyFile() throws IOException {
        Path expected = File.createTempFile("emptyExpected", ".csv").toPath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(expected.toFile()))) {
            writer.write(FileBackedTaskManager.CSV_FILE_HEADER);
            writer.flush();
        }

        Path working = File.createTempFile("emptyWorking", ".csv").toPath();
        new FileBackedTaskManager(Managers.getDefaultHistory(), working);

        assertEquals(-1L, Files.mismatch(expected, working), "Некорректный пустой файл");

        Files.delete(working);
        Files.delete(expected);
    }

    @Test
    public void checkUnloadToFileCorrect() throws IOException {
        Path expected = File.createTempFile("expected", ".csv").toPath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(expected.toFile()))) {
            writer.write(FileBackedTaskManager.CSV_FILE_HEADER);
            writer.newLine();
            writer.write("1,TASK,t1,NEW,t1d,");
            writer.newLine();
            writer.write("2,EPIC,e1,IN_PROGRESS,e1d,");
            writer.newLine();
            writer.write("3,SUBTASK,e1s1,NEW,e1s1d,2");
            writer.newLine();
            writer.write("4,SUBTASK,e1s2,DONE,e1s2d,2");
            writer.newLine();
            writer.write("99,TASK,t99,NEW,t99d,");

            writer.flush();
        }

        Path working = File.createTempFile("working", ".csv").toPath();
        FileBackedTaskManager tm = new FileBackedTaskManager(Managers.getDefaultHistory(), working);
        tm.createTask("t1", "t1d");
        Epic e = tm.createEpic("e1", "e1d");
        tm.createSubtask("e1s1", "e1s1d", e);
        Subtask s2 = tm.createSubtask("e1s2", "e1s2d", e);
        s2.setStatus(TaskStatus.DONE);
        tm.updateTask(s2);
        Task t = new Task("t99","t99d",99);
        tm.addTask(t);

        assertEquals(-1L, Files.mismatch(expected, working), "Некорректный результирующий файл");

        Files.delete(expected);
        Files.delete(working);
    }

    @Test
    public void checkUploadFromFileCorrect() throws IOException {
        Path src = File.createTempFile("src2_", ".csv").toPath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(src.toFile()))) {
            writer.write(FileBackedTaskManager.CSV_FILE_HEADER);
            writer.newLine();
            writer.write("1,TASK,t1,NEW,t1d,");
            writer.newLine();
            writer.write("2,EPIC,e1,IN_PROGRESS,e1d,");
            writer.newLine();
            writer.write("3,SUBTASK,e1s1,NEW,e1s1d,2");
            writer.newLine();
            writer.write("4,SUBTASK,e1s2,DONE,e1s2d,2");

            writer.flush();
        }

        FileBackedTaskManager tm = FileBackedTaskManager.loadFromFile(src);

        assertEquals(4, tm.getAllTasks().size(), "Неверное количество задач");

        Epic e = (Epic) tm.getTaskById(2);
        assertEquals(TaskStatus.IN_PROGRESS, e.getStatus(), "Некорректный статус эпика");
        assertEquals(2, e.getSubtasks().size(), "Неверное количество подзадач в эпике");

        Files.delete(src);
    }

    @Test
    public void checkRemoveOperations() throws IOException {
        Path src = File.createTempFile("src3", ".csv").toPath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(src.toFile()))) {
            writer.write(FileBackedTaskManager.CSV_FILE_HEADER);
            writer.newLine();
            writer.write("1,TASK,t1,NEW,t1d,");
            writer.newLine();
            writer.write("2,EPIC,e1,IN_PROGRESS,e1d,");
            writer.newLine();
            writer.write("3,SUBTASK,e1s1,NEW,e1s1d,2");
            writer.newLine();
            writer.write("4,SUBTASK,e1s2,DONE,e1s2d,2");

            writer.flush();
        }

        FileBackedTaskManager tm = FileBackedTaskManager.loadFromFile(src);

        tm.removeTaskById(1);
        Path expected1 = File.createTempFile("expected1", ".csv").toPath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(expected1.toFile()))) {
            writer.write(FileBackedTaskManager.CSV_FILE_HEADER);
            writer.newLine();
            writer.write("2,EPIC,e1,IN_PROGRESS,e1d,");
            writer.newLine();
            writer.write("3,SUBTASK,e1s1,NEW,e1s1d,2");
            writer.newLine();
            writer.write("4,SUBTASK,e1s2,DONE,e1s2d,2");

            writer.flush();
        }
        assertEquals(-1L, Files.mismatch(expected1, src), "Некорректный результат в файле после удаления задачи");

        tm.removeAllTasks();
        Path empty = File.createTempFile("empty2", ".csv").toPath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(empty.toFile()))) {
            writer.write(FileBackedTaskManager.CSV_FILE_HEADER);
            writer.flush();
        }
        assertEquals(-1L, Files.mismatch(empty, src), "Некорректный результат в файле после удаления всех задач");

        Files.delete(expected1);
        Files.delete(src);
        Files.delete(empty);
    }
}