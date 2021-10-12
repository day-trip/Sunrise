package com.daytrip.sunrise.util.timer;

public class TickTimer {
    private final TimerAction action;

    private int targetTicks;
    private int currentTicks;

    private boolean canRun;

    private final boolean repeating;

    public TickTimer(TimerAction action, int targetTicks, boolean repeating) {
        this.action = action;
        this.targetTicks = targetTicks;
        this.repeating = repeating;
        canRun = true;
    }

    public void update() {
        if(canRun) {
            if(currentTicks > targetTicks) {
                action.onTimerFinish(this);
                if(repeating) {
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

    public TimerAction getAction() {
        return action;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public static TickTimer createNoAction(int targetTicks, boolean repeating) {
        return new TickTimer(tickTimer -> doNothing(), targetTicks, repeating);
    }

    private static void doNothing() {
        ;
    }
}
