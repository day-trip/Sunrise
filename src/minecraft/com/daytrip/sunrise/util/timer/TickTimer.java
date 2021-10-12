package com.daytrip.sunrise.util.timer;

import com.daytrip.sunrise.hack.task.ActionCallable;

import java.util.concurrent.Callable;

public class TickTimer {
    private final ActionCallable action;

    private int targetTicks;
    private int currentTicks;

    private boolean canRun;

    private final boolean repeating;

    private Callable<Integer> nextInterval;

    public TickTimer(ActionCallable action, int targetTicks, boolean repeating) {
        this.action = action;
        this.targetTicks = targetTicks;
        this.repeating = repeating;
        canRun = true;
    }

    public void update() {
        if(canRun) {
            if(currentTicks > targetTicks) {
                action.call();
                if(repeating) {
                    if(nextInterval != null) {
                        try {
                            targetTicks = nextInterval.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    reset();
                } else {
                    stop();
                    TimerManager.finished(this);
                }
            } else {
                currentTicks++;
            }
        }
    }

    public void setNextInterval(Callable<Integer> nextInterval) {
        this.nextInterval = nextInterval;
    }

    public void reset() {
        currentTicks = 0;
    }

    public void stop() {
        canRun = false;
    }

    public void resume() {
        canRun = true;
    }

    public interface TimerAction {
        void onTimerFinish(TickTimer tickTimer);
    }

    public int getCurrentTicks() {
        return currentTicks;
    }

    public int getTargetTicks() {
        return targetTicks;
    }

    public ActionCallable getAction() {
        return action;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public static TickTimer createNoAction(int targetTicks, boolean repeating) {
        return new TickTimer(TickTimer::doNothing, targetTicks, repeating);
    }

    private static void doNothing() {
        ;
    }
}
