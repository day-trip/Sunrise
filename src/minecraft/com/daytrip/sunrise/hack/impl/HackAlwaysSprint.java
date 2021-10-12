package com.daytrip.sunrise.hack.impl;

import com.daytrip.sunrise.event.Event;
import com.daytrip.sunrise.event.impl.EventTick;
import com.daytrip.sunrise.hack.Hack;
import org.lwjgl.input.Keyboard;

public class HackAlwaysSprint extends Hack {
    public HackAlwaysSprint() {
        super(Keyboard.KEY_T, "Always Sprint", "always_sprint");
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventTick) {
            if(!minecraft.thePlayer.isSprinting()) {
                minecraft.thePlayer.setSprinting(true);
            }
        }
    }

    @Override
    protected void enable() {
        super.enable();
        minecraft.thePlayer.setSprinting(true);
    }

    @Override
    protected void disable() {
        super.disable();
        minecraft.thePlayer.setSprinting(false);
    }
}
