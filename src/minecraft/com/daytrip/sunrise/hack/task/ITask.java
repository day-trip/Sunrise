package com.daytrip.sunrise.hack.task;

import java.util.concurrent.Callable;

public interface ITask {
    ITask withName(String name);
    String name();

    ITask onInit(ActionCallable callable);
    void init();

    ITask addSubtask(ITask subtask);

    ITask executeIf(Callable<Boolean> executeIf);
    boolean canExecute();

    ITask whenCannotExecute(ActionCallable callable);
    void cannotExecute();

    ITask onTick(ActionCallable callable);
    void tick();

    ITask callIn(Callable<Integer> ticks, ActionCallable callable);

    ITask callEvery(Callable<Integer> ticks, ActionCallable callable);
    ITask nextInterval(Callable<Integer> ticks);
}
