package com.daytrip.sunrise.hack;

import com.daytrip.shared.event.EventBus;
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

    public static void keyPress(int key) {
        if(Keyboard.isKeyDown(key)) {
            for(Hack hack : hacks.values()) {
                if(hack.getKey() == key) {
                    hack.toggle();
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
