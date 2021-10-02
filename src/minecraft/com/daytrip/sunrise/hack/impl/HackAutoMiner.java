package com.daytrip.sunrise.hack.impl;

import com.daytrip.shared.event.Event;
import com.daytrip.sunrise.hack.Hack;
import org.lwjgl.input.Keyboard;

import java.lang.annotation.Annotation;

public class HackAutoMiner extends Hack {
    public HackAutoMiner() {
        super(Keyboard.KEY_M, "Auto Miner", "auto_miner");
    }

    @Override
    public void onEvent(Event event) throws Exception {

    }
}
