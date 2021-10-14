package com.daytrip.sunrise.hack;

import com.daytrip.sunrise.event.EventBus;
import com.daytrip.sunrise.event.impl.input.EventKeypress;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HackManager {
    private static final Map<String, Hack> hacks = new HashMap<>();

    public static void addHack(Hack hack) {
        hacks.put(hack.id, hack);
        EventBus.registerListener(hack);
    }

    public static Hack getHack(String id) {
        return hacks.get(id);
    }

    public static Hack getHack(int id) {
        Set<Map.Entry<String, Hack>> entry = hacks.entrySet();
        return new ArrayList<>(entry).get(id).getValue();
    }

    public static Map<String, Hack> getHacks() {
        return hacks;
    }

    public static int count() {
        return hacks.size();
    }

    public static void keyPress(EventKeypress event) {
        if(Keyboard.isKeyDown(event.getKey())) {
            for(Hack hack : hacks.values()) {
                if(hack.getKey() == event.getKey()) {
                    hack.toggle();
                    event.setCancelled(true);
                }
            }
        }
    }

    public static boolean enabled(String id) {
        return hacks.get(id).isEnabled();
    }

    public static boolean enabled(int id) {
        Set<Map.Entry<String, Hack>> entry = hacks.entrySet();
        return new ArrayList<>(entry).get(id).getValue().isEnabled() ;
    }
}
