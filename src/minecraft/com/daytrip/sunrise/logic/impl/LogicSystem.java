package com.daytrip.sunrise.logic.impl;

import com.daytrip.sunrise.hack.task.ActionCallable;
import com.daytrip.sunrise.logic.ILogic;
import com.daytrip.sunrise.logic.ILogicSystem;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class LogicSystem {
    private final List<ILogic> logics;

    private LogicSystem(ILogic[] logics) {
        this.logics = Arrays.asList(logics);
    }

    public List<ILogic> getLogics() {
        return logics;
    }

    public static LogicSystem create(ILogic... logics) {
        return new LogicSystem(logics);
    }
}
