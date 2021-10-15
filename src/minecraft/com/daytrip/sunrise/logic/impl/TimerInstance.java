package com.daytrip.sunrise.logic.impl;

import com.daytrip.sunrise.hack.task.ActionCallable;
import com.daytrip.sunrise.logic.ITimerInstance;
import com.daytrip.sunrise.util.Wrappers;

import java.util.concurrent.Callable;

public class TimerInstance extends Logic implements ITimerInstance {
    private final int targetTicks;
    private int ticks;

    private Callable<Boolean> ticksIf;
    private ActionCallable onDone;

    private TimerInstance(int targetTicks) {
        this.targetTicks = targetTicks;
    }

    @Override
    public TimerInstance ticksIf(Callable<Boolean> ticksIf) {
        this.ticksIf = ticksIf;
        return this;
    }

    @Override
    public TimerInstance onDone(ActionCallable onDone) {
        this.onDone = onDone;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        if(Wrappers.notNull(Wrappers.<Boolean>call(ticksIf))) {
            if(ticks >= targetTicks) {
                ticks = 0;
                onDone.call();
            } else {
                ticks++;
            }
        }
    }

    public static TimerInstance create(int targetTicks) {
        return new TimerInstance(targetTicks);
    }
}
