package ru.tasktracker;

import ru.tasktracker.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    Node<Task> lastTask;
    final Map<Integer, Node<Task>> historyIndex = new HashMap<>();

    @Override
    public void add(Task task) {
        remove(task.getId());
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        if (historyIndex.containsKey(id)) {
            removeNode(historyIndex.get(id));
            historyIndex.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void clearHistory() {
        lastTask = null;
        historyIndex.clear();
    }

    private void linkLast(Task t) {
        if (lastTask == null) {
            lastTask = new Node<>(t, null, null);
        } else {
            Node<Task> newNode = new Node<>(t, lastTask, null);
            lastTask.next = newNode;
            lastTask = newNode;
        }
        historyIndex.put(t.getId(),lastTask);
    }

    private List<Task> getTasks() {
        Node<Task> currentNode = lastTask;
        List<Task> tasks = new ArrayList<>();
        while (currentNode != null) {
            tasks.add(currentNode.data);
            currentNode = currentNode.prev;
        }
        return tasks;
    }

    private void removeNode(Node<Task> n) {
        if (n.prev != null) {
            n.prev.next = n.next;
        }
        if (n.next != null) {
            n.next.prev = n.prev;
        }
        if (n == lastTask) {
            lastTask = n.prev;
        }
    }

    private class Node<T> {
        Node<T> prev;
        Node<T> next;
        T data;

        Node(T data, Node<T> prev, Node<T> next) {
            this.data = data;
            this.next = next;
            this.prev = prev;
        }
    }
}