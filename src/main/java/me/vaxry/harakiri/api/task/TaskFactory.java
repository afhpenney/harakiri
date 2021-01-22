package me.vaxry.harakiri.api.task;

import me.vaxry.harakiri.api.task.basic.BasicTask;

import java.util.List;

public interface TaskFactory<T extends BasicTask> {

    void removeTask(String taskName);

    void removeTask(T task);

    List<T> getTasks();
}
