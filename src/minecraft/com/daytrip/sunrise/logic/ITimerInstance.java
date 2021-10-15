package com.daytrip.sunrise.logic;

import com.daytrip.sunrise.hack.task.ActionCallable;

import java.util.concurrent.Callable;

public interface ITimerInstance {
    ITimerInstance ticksIf(Callable<Boolean> ticksIf);
    ITimerInstance onDone(ActionCallable onDone);
}
