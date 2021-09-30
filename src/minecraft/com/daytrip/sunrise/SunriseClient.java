package com.daytrip.sunrise;

import com.daytrip.shared.gui.GuiScreenHacks;
import com.daytrip.sunrise.hack.Hack;
import com.daytrip.sunrise.hack.HackGui;
import com.daytrip.sunrise.hack.impl.HackAimbot;
import com.daytrip.sunrise.hack.impl.HackAutoFighter;
import com.daytrip.shared.event.Event;
import com.daytrip.shared.event.EventBus;
import com.daytrip.shared.event.EventListener;
import com.daytrip.shared.event.impl.EventKeypress;
import com.daytrip.shared.event.impl.EventRegisterListeners;
import com.daytrip.sunrise.hack.impl.HackBridger;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SunriseClient implements EventListener {
    public static final List<Hack> hacks = new CopyOnWriteArrayList<>();
    public static final HackGui hackGui = new HackGui();

    @Override
    public void onEvent(Event event) {
        if(event instanceof EventRegisterListeners) {
            registerHacks();
            EventBus.registerListener(hackGui);
        }
        if(event instanceof EventKeypress) {
            for(Hack hack : hacks) {
                if(hack.getKey() == ((EventKeypress) event).getKey() && Keyboard.isKeyDown(hack.getKey())) {
                    hack.toggle();
                }
            }
            if(((EventKeypress) event).getKey() == Keyboard.KEY_RSHIFT) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiScreenHacks());
            }
        }
    }

    private void registerHacks() {
        registerHack(new HackAutoFighter());
        registerHack(new HackAimbot());
        registerHack(new HackBridger());
    }

    private void registerHack(Hack hack) {
        EventBus.registerListener(hack);
        hacks.add(hack);
    }

    @Override
    public String getListenerName() {
        return "sunrise";
    }
}
