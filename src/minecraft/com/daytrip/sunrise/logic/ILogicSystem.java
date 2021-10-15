package com.daytrip.sunrise.logic;

import com.daytrip.sunrise.hack.task.ActionCallable;

import java.util.concurrent.Callable;

public interface ILogicSystem {
    void runWhen(Callable<Boolean> when, ActionCallable run);
}
