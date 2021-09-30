package com.daytrip.sunrise.hack.impl;

import com.daytrip.shared.event.Event;
import com.daytrip.shared.event.impl.EventProcessMouse;
import com.daytrip.shared.event.impl.EventTick;
import com.daytrip.shared.event.impl.EventUpdateMovementInput;
import com.daytrip.sunrise.hack.Hack;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;

public class HackBridger extends Hack {
    private static final int[] possibleRotations = {-135, 135, -45, 45};

    private int jumpTicks;
    private int onAirTicks;

    public HackBridger() {
        super(Keyboard.KEY_B, "Bridger", "bridger");
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventProcessMouse && minecraft.inWorld()) {
            event.setCancelled(true);
        }
        if(event instanceof EventUpdateMovementInput && minecraft.inWorld()) {
            event.setCancelled(true);
        }
        if(event instanceof EventTick && minecraft.inWorld()) {
            if(minecraft.theWorld.getBlockState(minecraft.thePlayer.getPosition().subtract(new Vec3i(0, 1, 0))).getBlock() == Blocks.air) {
                if(onAirTicks > 2) {
                    onAirTicks = 0;
                    placeBlocks();
                } else {
                    onAirTicks++;
                }
            }

            if(jumpTicks > 25) {
                jumpTicks = 0;
                if(minecraft.thePlayer.onGround) {
                    minecraft.thePlayer.jump();
                    placeBlocks();
                }
            } else {
                jumpTicks++;
            }
        }
    }

    private void placeBlocks() {
        if(minecraft.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos hitPos = minecraft.objectMouseOver.getBlockPos();
            Vec3 hitVec = minecraft.objectMouseOver.hitVec;

            minecraft.playerController.syncCurrentPlayItem();

            float f = (float)(hitVec.xCoord - (double)hitPos.getX());
            float f1 = (float)(hitVec.yCoord - (double)hitPos.getY());
            float f2 = (float)(hitVec.zCoord - (double)hitPos.getZ());

            minecraft.playerController.netClientHandler.addToSendQueue(new C08PacketPlayerBlockPlacement(hitPos, minecraft.objectMouseOver.sideHit.getIndex(), minecraft.thePlayer.inventory.getCurrentItem(), f, f1, f2));
        }
    }

    private int getClosest(int[] array, int number) {
        int distance = Math.abs(array[0] - number);
        int idx = 0;
        for(int c = 1; c < array.length; c++){
            int cdistance = Math.abs(array[c] - number);
            if(cdistance < distance){
                idx = c;
                distance = cdistance;
            }
        }
        return array[idx];
    }

    @Override
    protected void disable() {
        super.disable();
        jumpTicks = 0;
        onAirTicks = 0;
        minecraft.thePlayer.rotationPitch = 0;
        stopMoving();
    }

    @Override
    protected void enable() {
        super.enable();
        Minecraft.logger.info("Pure Yaw" + MathHelper.wrapAngleTo180_float(minecraft.thePlayer.rotationYaw));
        Minecraft.logger.info("Yaw" + Math.round(MathHelper.wrapAngleTo180_float(minecraft.thePlayer.rotationYaw)));
        int closest = getClosest(possibleRotations, Math.round(MathHelper.wrapAngleTo180_float(minecraft.thePlayer.rotationYaw)));
        Minecraft.logger.info("Closest" + closest);
        minecraft.thePlayer.rotationYaw = closest;
        minecraft.thePlayer.rotationPitch = 78;
        placeBlocks();
        startMoving();
    }

    private void stopMoving() {
        minecraft.thePlayer.movementInput.moveForward = 0;
        minecraft.thePlayer.movementInput.moveStrafe = 0;
    }

    private void startMoving() {
        minecraft.thePlayer.movementInput.moveForward = -1;
        minecraft.thePlayer.movementInput.moveStrafe = -1;
    }
}
