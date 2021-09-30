package com.daytrip.shared.event.impl;

import com.daytrip.shared.event.Event;
import net.minecraft.entity.EntityLivingBase;

public class EventRenderBrightnessBuffer extends Event {
    private EntityLivingBase entityLivingBase;
    private float r;
    private float g;
    private float b;
    private float a;
    private int context;

    public int getContext() {
        return context;
    }

    public void setContext(int context) {
        this.context = context;
    }

    public EntityLivingBase getEntityLivingBase() {
        return entityLivingBase;
    }

    public void setEntityLivingBase(EntityLivingBase entityLivingBase) {
        this.entityLivingBase = entityLivingBase;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }
}
