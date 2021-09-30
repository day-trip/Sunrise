package com.daytrip.sunrise.hack.impl;

import com.daytrip.shared.CommonMath;
import com.daytrip.shared.ExtendedReach;
import com.daytrip.shared.event.Event;
import com.daytrip.shared.event.impl.*;
import com.daytrip.sunrise.hack.Hack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

public class HackAutoFighter extends Hack {
    private EntityLivingBase target;

    private int inventorySwordSlot;
    private int inventoryRodSlot;

    private int attackTicks;
    private int blockHitCounter;

    private int rodTicks;

    private int lerpTicks;
    private int targetLerpTicks;

    private int jumpTicks;
    private float targetJumpTicks;

    private String strafe = "left";
    private int strafeTicks;
    private int targetStrafeTicks;

    public HackAutoFighter() {
        super(Keyboard.KEY_P, "Auto Fighter", "auto_fighter");
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventProcessMouse && target != null && minecraft.inWorld()) {
            event.setCancelled(true);
        }
        if(event instanceof EventClickMouse && minecraft.inWorld()) {
            if(event.getCustomFromTarget().equals(id)) return;

            if(((EventClickMouse) event).getButton() == 2) {
                ExtendedReach reach = new ExtendedReach(null, 25, minecraft, minecraft.thePlayer);
                reach.getMouseOver();
                if(reach.mcObjectMouseOver.entityHit instanceof EntityLivingBase) {
                    target = (EntityLivingBase) reach.mcObjectMouseOver.entityHit;
                }
            }

            event.setCancelled(true);
        }
        if(event instanceof EventRenderBrightnessBuffer && target != null && minecraft.inWorld()) {
            EventRenderBrightnessBuffer eventRBB = (EventRenderBrightnessBuffer) event;
            if(eventRBB.getEntityLivingBase() == target) {
                if(eventRBB.getContext() == 0) {
                    eventRBB.setR(0.0F);
                    eventRBB.setG(1.0F);
                    eventRBB.setB(0.0F);
                    eventRBB.setA(0.3F);
                } else {
                    eventRBB.setR(0.0F);
                    eventRBB.setG(0.0F);
                    eventRBB.setB(1.0F);
                    eventRBB.setA(0.3F);
                }
            }
        }
        if(event instanceof EventUpdateMovementInput && target != null && minecraft.inWorld()) {
            event.setCancelled(true);
        }
        if(event instanceof EventTick && target != null && minecraft.inWorld()) {
            double fDist = Math.floor(target.getDistanceToEntity(minecraft.thePlayer));

            BlockPos blockInFrontPos = minecraft.thePlayer.getPosition().offset(minecraft.thePlayer.getHorizontalFacing());
            IBlockState blockInFront = minecraft.theWorld.getBlockState(blockInFrontPos);

            if(fDist < 4) {
                rodTicks = 0;

                minecraft.thePlayer.inventory.currentItem = inventorySwordSlot;
                minecraft.playerController.syncCurrentPlayItem();

                startMoving();

                if(fDist > 2) {
                    if(strafeTicks > targetStrafeTicks) {
                        strafeTicks = 0;
                        targetStrafeTicks = 50 + minecraft.theWorld.rand.nextInt(30);
                        minecraft.thePlayer.movementInput.moveStrafe = 0;
                        if(strafe.equals("left")) {
                            strafe = "right";
                        } else {
                            strafe = "left";
                        }
                    } else {
                        strafeTicks++;
                        if(strafe.equals("left")) {
                            minecraft.thePlayer.movementInput.moveStrafe = -1;
                        } else {
                            minecraft.thePlayer.movementInput.moveStrafe = 1;
                        }
                    }
                } else {
                    minecraft.thePlayer.movementInput.moveStrafe = 0;
                }

                if(blockInFront.getBlock() != Blocks.air) {
                    if(minecraft.thePlayer.onGround) {
                        minecraft.thePlayer.jump();
                    }
                } else {
                    minecraft.thePlayer.setJumping(false);
                }

                if(attackTicks > 2) {
                    if(blockHitCounter > 3) {
                        blockHitCounter = 0;

                        EventClickMouse eventClickMouse = new EventClickMouse();
                        eventClickMouse.setButton(1);
                        eventClickMouse.setCustomFromTarget(id);
                        eventClickMouse.post();

                        minecraft.thePlayer.setSprinting(false);
                        minecraft.thePlayer.movementInput.moveForward = 0;
                        minecraft.thePlayer.setSprinting(true);
                        minecraft.thePlayer.movementInput.moveForward = 1;
                    } else {
                        blockHitCounter++;

                        EventClickMouse eventClickMouse = new EventClickMouse();
                        eventClickMouse.setButton(0);
                        eventClickMouse.setCustomFromTarget(id);
                        eventClickMouse.post();
                    }
                    attackTicks = 0;
                } else {
                    attackTicks++;
                }
            } else {
                startMoving();

                if(minecraft.theWorld.getBlockState(minecraft.thePlayer.getPosition().offset(minecraft.thePlayer.getHorizontalFacing())).getBlock() != Blocks.air) {
                    if(minecraft.thePlayer.onGround) {
                        minecraft.thePlayer.jump();
                    }
                }

                minecraft.thePlayer.inventory.currentItem = inventoryRodSlot;
                minecraft.playerController.syncCurrentPlayItem();

                if(rodTicks > 10) {
                    rodTicks = 0;
                    EventClickMouse eventClickMouse = new EventClickMouse();
                    eventClickMouse.setButton(1);
                    eventClickMouse.setCustomFromTarget(id);
                    eventClickMouse.post();
                } else {
                    rodTicks++;
                }

                if(jumpTicks > targetJumpTicks) {
                    jumpTicks = 0;
                    targetJumpTicks = 20 + minecraft.theWorld.rand.nextInt(10);
                    if(fDist > 6) {
                        minecraft.thePlayer.jump();
                    }
                } else {
                    jumpTicks++;
                }
            }

            if(lerpTicks > targetLerpTicks) {
                lerpTicks = 0;
                targetLerpTicks = 18 + minecraft.theWorld.rand.nextInt(4);
            } else {
                lerpTicks++;
            }

            CommonMath.updateValues(minecraft.thePlayer, target);

            minecraft.thePlayer.rotationYaw = (float) Math.toDegrees(CommonMath.lerpAngle((float) Math.toRadians(minecraft.thePlayer.rotationYaw), (float) Math.toRadians(CommonMath.yawToFaceEntity()), targetLerpTicks / 20f));
            minecraft.thePlayer.rotationPitch = (float) Math.toDegrees(CommonMath.lerpAngle((float) Math.toRadians(minecraft.thePlayer.rotationPitch), (float) Math.toRadians(CommonMath.pitchToFaceEntity()), targetLerpTicks / 20f));

            if(target.getHealth() <= 0) {
                loseTarget();
            }

            navigation();
        }
        if(event instanceof EventKeypress) {
            if(minecraft.inWorld()) {
                if(target != null) {
                    if(((EventKeypress) event).getKey() == Keyboard.KEY_ESCAPE) {
                        loseTarget();

                    }
                    if(((EventKeypress) event).getKey() != Keyboard.KEY_F5) {
                        event.setCancelled(true);
                    }
                } else {
                    if(((EventKeypress) event).getKey() == Keyboard.KEY_G) {
                        EntityPlayer player = minecraft.theWorld.getClosestPlayerExcluding(minecraft.thePlayer.posX, minecraft.thePlayer.posY, minecraft.thePlayer.posZ, 25);
                        if(player != null) {
                            target = player;
                        }
                    }
                }
            }
        }
    }

    private void loseTarget() {
        target = null;
        lerpTicks = 0;
        attackTicks = 0;
        jumpTicks = 0;
        rodTicks = 0;
        strafeTicks = 0;
        blockHitCounter = 0;
        stopMoving();
    }

    private void learnInventoryLayout() {
        for(int i = 0; i < 9; i++) {
            ItemStack stack = minecraft.thePlayer.inventory.mainInventory[i];
            if(stack.getItem() == Items.iron_sword || stack.getItem() == Items.wooden_sword || stack.getItem() == Items.stone_sword || stack.getItem() == Items.golden_sword || stack.getItem() == Items.diamond_sword) {
                inventorySwordSlot = i;
            }
            if(stack.getItem() == Items.fishing_rod) {
                inventoryRodSlot = i;
            }
        }
    }

    private void navigation() {

    }

    private void stopMoving() {
        minecraft.thePlayer.setSprinting(false);
        minecraft.thePlayer.movementInput.moveForward = 0;
    }

    private void startMoving() {
        minecraft.thePlayer.setSprinting(true);
        minecraft.thePlayer.movementInput.moveForward = 1;
    }

    public EntityLivingBase getTarget() {
        return target;
    }

    /**
     * @return whether the bot is playing offense or defense
     */
    private boolean isOffense() {
        if(minecraft.thePlayer.getHealth() >= target.getHealth()) {
            return true;
        }
        return minecraft.thePlayer.getHealth() > minecraft.thePlayer.getMaxHealth() / 2;
    }

    @Override
    protected void disable() {
        super.disable();
        loseTarget();
    }

    @Override
    protected void enable() {
        super.enable();
        learnInventoryLayout();
        targetStrafeTicks = 50 + minecraft.theWorld.rand.nextInt(30);
        targetLerpTicks = 18 + minecraft.theWorld.rand.nextInt(4);
        targetJumpTicks = 20 + minecraft.theWorld.rand.nextInt(10);
    }
}
