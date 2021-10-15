package com.daytrip.sunrise;

import com.daytrip.sunrise.util.LoginManager;
import com.daytrip.sunrise.event.Event;
import com.daytrip.sunrise.event.EventBus;
import com.daytrip.sunrise.event.EventListener;
import com.daytrip.sunrise.event.impl.init.EventGamePreInit;
import com.daytrip.sunrise.event.impl.input.EventKeypress;
import com.daytrip.sunrise.event.impl.EventRegisterListeners;
import com.daytrip.sunrise.util.timer.TimerManager;
import com.daytrip.sunrise.gui.GuiScreenMenu;
import com.daytrip.sunrise.hack.HackManager;
import com.daytrip.sunrise.hack.SunriseGUI;
import com.daytrip.sunrise.hack.impl.*;
import com.daytrip.sunrise.hack.impl.bot.BotAutoFighter;
import com.daytrip.sunrise.hack.impl.bot.BotAutoMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

public class SunriseClient implements EventListener {
    @Override
    public void onEvent(Event event) {
        Minecraft minecraft = Minecraft.getMinecraft();

        if(event instanceof EventGamePreInit) {
            LoginManager.applyUser(LoginManager.login("malaworld@gmail.com", "JaiAvi10:14"));
        }
        if(event instanceof EventRegisterListeners) {
            HackAPI.minecraft = minecraft;
            EventBus.registerListener(new TimerManager());
            registerHacks();
            EventBus.registerListener(new SunriseGUI());
        }
        if(event instanceof EventKeypress) {
            HackManager.keyPress((EventKeypress) event);
            if(((EventKeypress) event).getKey() == Keyboard.KEY_RSHIFT) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiScreenMenu());
            }
            if(((EventKeypress) event).getKey() == Keyboard.KEY_NUMPAD9) {
                Vec3 nextPos = minecraft.thePlayer.getPositionVector().addVector(minecraft.thePlayer.getMotionVector().multiplyVector(new Vec3(5)).divideVector(new Vec3(minecraft.timer.elapsedPartialTicks)));
                System.out.println(nextPos.toString());
                System.out.println(minecraft.theWorld.getBlockState(new BlockPos(nextPos.xCoord, nextPos.yCoord, nextPos.zCoord).down()).getBlock() == Blocks.lava);
                System.out.println(minecraft.theWorld.getBlockState(new BlockPos(nextPos.xCoord, nextPos.yCoord, nextPos.zCoord).down()).getBlock() == Blocks.flowing_lava);
            }
        }
    }

    private void registerHacks() {
        // Bots
        HackManager.addHack(new BotAutoFighter());
        HackManager.addHack(new BotAutoMiner());

        // Visual difference
        HackManager.addHack(new HackXRay());
        HackManager.addHack(new HackEntityInfo());

        // Physical difference
        HackManager.addHack(new HackAlwaysSprint());
        HackManager.addHack(new HackAimbot());
        HackManager.addHack(new HackBridger());
    }

    public static void main(String[] args) {
        System.out.println(Math.sqrt((5-10)*(5-10)));
    }
}
