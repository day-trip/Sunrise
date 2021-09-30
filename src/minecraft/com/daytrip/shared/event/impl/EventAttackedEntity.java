package com.daytrip.shared.event.impl;

import com.daytrip.shared.event.Event;
import net.minecraft.entity.Entity;

public class EventAttackedEntity extends Event {
    private Entity target;

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }
}
