package com.daytrip.sunrise.event.impl;

import com.daytrip.sunrise.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class EventEntityAttackedByPlayer extends Event {
    private Entity target;
    private EntityPlayer attacker;

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public EntityPlayer getAttacker() {
        return attacker;
    }

    public void setAttacker(EntityPlayer attacker) {
        this.attacker = attacker;
    }
}
