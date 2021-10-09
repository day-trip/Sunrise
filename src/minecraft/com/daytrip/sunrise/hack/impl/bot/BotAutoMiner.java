package com.daytrip.sunrise.hack.impl.bot;

import com.daytrip.shared.event.Event;
import com.daytrip.sunrise.hack.Hack;
import org.lwjgl.input.Keyboard;

import java.lang.annotation.Annotation;

public class BotAutoMiner extends Hack {
    public BotAutoMiner() {
        super(Keyboard.KEY_M, "Auto Miner", "auto_miner");
    }

    @Override
    public void onEvent(Event event) throws Exception {

    }
}
