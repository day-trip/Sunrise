package com.daytrip.sunrise.event.impl;

import com.daytrip.sunrise.event.Event;
import net.minecraft.entity.EntityLivingBase;

import java.nio.FloatBuffer;

public class EventRenderBrightnessBuffer extends Event {
    public EntityLivingBase onEntity;
    public FloatBuffer buffer;
    public boolean combineTextures;
}
