package com.daytrip.sunrise.event.impl;

import com.daytrip.sunrise.event.Event;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

public class EventEntityDeath extends Event {
    private EntityLivingBase entity;
    private DamageSource source;

    public DamageSource getSource() {
        return source;
    }

    public void setSource(DamageSource source) {
        this.source = source;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public void setEntity(EntityLivingBase entity) {
        this.entity = entity;
    }
}
