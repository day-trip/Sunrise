package com.daytrip.sunrise.event;

public class Event {
    private boolean cancelled;
    private String customFromTarget = "";

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
