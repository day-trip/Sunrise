package com.daytrip.sunrise.hack.task;

import net.minecraft.client.Minecraft;

import javax.tools.JavaCompiler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TaskManager {
    private final Map<Integer, List<ITask>> tasksByPriority = new TreeMap<>();

    private boolean canContinue = true;

    private boolean a;

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
        task.init();
    }

    public void tick() {
        for(List<ITask> tasks : tasksByPriority.values()) {
            for(ITask task : tasks) {
                if(!canContinue)  {
                    return;
                }
                if(!a) {
                    Minecraft.logger.info(task.toString());
                }
                if(task.canExecute()) {
                    task.tick();
                } else {
                    task.cannotExecute();
                }
            }
        }
        if(!a) a = true;
    }

    public void endChain() {
        canContinue = false;
    }
}
