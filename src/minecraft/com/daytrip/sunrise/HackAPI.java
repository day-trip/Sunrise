package com.daytrip.sunrise;

import com.daytrip.sunrise.event.impl.input.EventClickMouse;
import com.daytrip.sunrise.util.Wrappers;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class HackAPI {
    public static Minecraft minecraft;

    public static void leftClick(String id) {
        EventClickMouse eventClickMouse = new EventClickMouse();
        eventClickMouse.setButton(0);
        eventClickMouse.setCustomFromTarget(id);
        Wrappers.post(eventClickMouse);
    }

    public static void rightClick(String id) {
        EventClickMouse eventClickMouse = new EventClickMouse();
        eventClickMouse.setButton(1);
        eventClickMouse.setCustomFromTarget(id);
        Wrappers.post(eventClickMouse);
    }

    public static void stopMovingAndSprinting() {
        stopSprinting();
        minecraft.thePlayer.movementInput.moveForward = 0;
    }

    public static void startMovingAndSprinting() {
        startSprinting();
        minecraft.thePlayer.movementInput.moveForward = 1;
    }

    public static void jump() {
        if(minecraft.thePlayer.onGround) {
            minecraft.thePlayer.jump();
        }
    }

    public static void startSprinting() {
        if(!minecraft.thePlayer.isSprinting()) {
            minecraft.thePlayer.setSprinting(true); // Will update network and get caught by anti-cheat; don't call unless necessary
        }
    }

    public static void stopSprinting() {
        if(minecraft.thePlayer.isSprinting()) {
            minecraft.thePlayer.setSprinting(false); // Will update network and get caught by anti-cheat; don't call unless necessary
        }
    }

    public static void changeInventorySlotAndUpdate(int slot) {
        minecraft.thePlayer.inventory.currentItem = slot;
        minecraft.playerController.syncCurrentPlayItem();
    }

    public static int getPingForPlayer(EntityPlayer player) {
        return minecraft.getNetHandler().getPlayerInfo(player.getUniqueID()).getResponseTime();
    }

    public static void moveTo(int x, int z) {

    }
}
