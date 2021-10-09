package com.daytrip.shared.event.impl;

import com.daytrip.shared.event.Event;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class EventPlayerDamaged extends Event {
    public EntityPlayer player;
    public float amount;
    public DamageSource damageSource;
}
