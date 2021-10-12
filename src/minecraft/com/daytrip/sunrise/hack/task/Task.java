package com.daytrip.sunrise.hack.task;

import com.daytrip.sunrise.util.timer.TickTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Task implements ITask {
    private String name = "";

    private final List<TickTimer> timers = new ArrayList<>();

    private final List<ITask> subtasks = new ArrayList<>();

    private final List<ActionCallable> onInits = new ArrayList<>();
    private final List<Callable<Boolean>> canExecutes = new ArrayList<>();
    private final List<ActionCallable> cannotExecutes = new ArrayList<>();
    private final List<ActionCallable> tickFunctions = new ArrayList<>();

    @Override
    public ITask withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ITask addSubtask(ITask subtask) {
        subtasks.add(subtask);
        return this;
    }

    @Override
    public ITask onInit(ActionCallable callable) {
        onInits.add(callable);
        return this;
    }

    @Override
    public void init() {
        for(ActionCallable callable : onInits) {
            callable.call();
        }
    }

    @Override
    public ITask executeIf(Callable<Boolean> executeIf) {
        canExecutes.add(executeIf);
        return this;
    }

    @Override
    public boolean canExecute() {
        for(Callable<Boolean> callable : canExecutes) {
            try {
                if(!callable.call()) return false;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ITask whenCannotExecute(ActionCallable callable) {
        cannotExecutes.add(callable);
        return this;
    }

    @Override
    public void cannotExecute() {
        for(ActionCallable callable : cannotExecutes) {
            callable.call();
        }
    }

    @Override
    public ITask onTick(ActionCallable callable) {
        tickFunctions.add(callable);
        return this;
    }

    @Override
    public void tick() {
        for(TickTimer timer : timers) {
            timer.update();
        }
        for(ActionCallable callable : tickFunctions) {
            callable.call();
        }

        for(ITask task : subtasks) {
            if(task.canExecute()) {
                task.tick();
            } else {
                task.cannotExecute();
            }
        }
    }

    @Override
    public ITask callIn(Callable<Integer> ticks, ActionCallable callable) {
        int t = 0;

        try {
            t = ticks.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        timers.add(new TickTimer(callable, t, false));
        return this;
    }

    @Override
    public ITask callEvery(Callable<Integer> ticks, ActionCallable callable) {
        int t = 0;

        try {
            t = ticks.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        timers.add(new TickTimer(callable, t, true));
        return this;
    }

    @Override
    public ITask nextInterval(Callable<Integer> ticks) {
        timers.get(timers.size() - 1).setNextInterval(ticks);
        return this;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                '}';
    }
}
