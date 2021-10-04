package com.daytrip.sunrise;

import com.daytrip.shared.LoginManager;
import com.daytrip.shared.event.Event;
import com.daytrip.shared.event.EventBus;
import com.daytrip.shared.event.EventListener;
import com.daytrip.shared.event.impl.EventGamePreInit;
import com.daytrip.shared.event.impl.EventKeypress;
import com.daytrip.shared.event.impl.EventRegisterListeners;
import com.daytrip.sunrise.gui.GuiScreenHacks;
import com.daytrip.sunrise.gui.GuiScreenMenu;
import com.daytrip.sunrise.hack.HackManager;
import com.daytrip.sunrise.hack.SunriseGUI;
import com.daytrip.sunrise.hack.impl.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class SunriseClient implements EventListener {
    @Override
    public void onEvent(Event event) {
        if(event instanceof EventGamePreInit) {
            LoginManager.setData(LoginManager.login("malaworld@gmail.com", "JaiAvi10:14"));
        }
        if(event instanceof EventRegisterListeners) {
            registerHacks();
            EventBus.registerListener(new SunriseGUI());
        }
        if(event instanceof EventKeypress) {
            HackManager.keyPress(((EventKeypress) event).getKey());
            if(((EventKeypress) event).getKey() == Keyboard.KEY_RSHIFT) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiScreenMenu());
            }
        }
    }

    private void registerHacks() {
        HackManager.addHack(new HackAutoFighter());
        HackManager.addHack(new HackAimbot());
        HackManager.addHack(new HackBridger());
        HackManager.addHack(new HackXRay());
        HackManager.addHack(new HackEntityInfo());
    }

    @Override
    public String getListenerName() {
        return "sunrise";
    }
}
