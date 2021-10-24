package com.daytrip.sunrise.hack.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TaskManager {
    private final Map<Integer, List<ITask>> tasksByPriority = new TreeMap<>();
    private final List<ActionCallable> ends = new ArrayList<>();

    private boolean canContinue;

    public TaskManager() {

    }

    /**
     * Register a new task to the system
     *
     * @param task     the task to be register
     * @param priority the priority of the task (0 is the greatest; the larger the number, the less important the task. Also, there can be as many tasks as needed per priority)
     */
    public void registerTask(int priority, ITask task) {
        tasksByPriority.computeIfAbsent(priority, integer -> new ArrayList<>());
        tasksByPriority.get(priority).add(task);
    }

    /**
     * Register a action to happen when the task manager stops
     * @param onEnd an action
     */
    public void registerOnEnd(ActionCallable onEnd) {
        ends.add(onEnd);
    }

    public void tick() {
        for(List<ITask> tasks : tasksByPriority.values()) {
            for(ITask task : tasks) {
                if(!canContinue)  {
                    return;
                }
                if(task.canExecute()) {
                    task.tick();
                } else {
                    task.cannotExecute();
                }
            }
        }
    }

    public void stopAndEndLine() {
        canContinue = false;
        for(ActionCallable end : ends) {
            end.call();
        }
    }

    public void start() {
        if(!canContinue) {
            canContinue = true;
            for(List<ITask> tasks : tasksByPriority.values()) {
                for (ITask task : tasks) {
                    task.init();
                }
            }
        }
    }
}
