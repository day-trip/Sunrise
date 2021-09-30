package com.daytrip.shared.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Event {
    private boolean cancelled;
    private final List<String> excludes = new CopyOnWriteArrayList<>();
    private String customFromTarget = "";

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void addExcludes(String name) {
        excludes.add(name);
    }

    public boolean isExcluded(String name) {
        return excludes.contains(name);
    }

    public void post() throws Exception {
        EventBus.post(this);
    }

    public void setCustomFromTarget(String customFromTarget) {
        this.customFromTarget = customFromTarget;
    }

    public String getCustomFromTarget() {
        return customFromTarget;
    }
}
