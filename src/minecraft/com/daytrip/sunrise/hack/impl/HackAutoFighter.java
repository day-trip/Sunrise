package com.daytrip.sunrise.hack.impl;

import com.daytrip.shared.CommonMath;
import com.daytrip.shared.ExtendedReach;
import com.daytrip.shared.event.Event;
import com.daytrip.shared.event.impl.*;
import com.daytrip.sunrise.hack.Hack;
import com.daytrip.sunrise.hack.pathfinding.PathFinder;
import com.daytrip.sunrise.hack.pathfinding.Point;
import com.daytrip.sunrise.hack.setting.impl.SettingBoolean;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class HackAutoFighter extends Hack {
    private EntityLivingBase target;

    private static final int inventorySwordSlot = 0;
    private static final int inventoryRodSlot = 3;

    private double distanceToTarget;

    private int attackTicks;

    private int hitCounter;

    private int rodTicks;

    private int lerpTicks;

    private String strafe = "left";
    private int strafeTicks;
    private int targetStrafeTicks;

    private boolean wantsToJump;

    private boolean autoNav;

    private final Block[] DANGER_BLOCKS = {Blocks.fire, Blocks.lava, Blocks.flowing_lava, Blocks.water, Blocks.flowing_water, Blocks.soul_sand, Blocks.web};

    public HackAutoFighter() {
        super(Keyboard.KEY_P, "Auto Fighter", "auto_fighter");
        settingManager.addSetting(new SettingBoolean("Strafe", "strafe", true));
        settingManager.addSetting(new SettingBoolean("Move Towards Target", "approach_target", true));
        settingManager.addSetting(new SettingBoolean("Aim Lock", "aim_lock", true));
        settingManager.addSetting(new SettingBoolean("Interpolate Aim", "interpolate", true));
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventProcessMouse && target != null && minecraft.inWorld()) {
            if(settingManager.<SettingBoolean>getSetting("aim_lock").getValue()) {
                event.setCancelled(true);
            }
        }
        if(event instanceof EventClickMouse && minecraft.inWorld()) {
            if(event.getCustomFromTarget().equals(id)) return;

            if(target != null) {
                event.setCancelled(true);
            }

            if(((EventClickMouse) event).getButton() == 2) {
                ExtendedReach reach = new ExtendedReach(null, 25, minecraft.thePlayer);
                reach.calculateMouseOver();
                if(reach.objectMouseOver.entityHit instanceof EntityLivingBase) {
                    target = (EntityLivingBase) reach.objectMouseOver.entityHit;
                    event.setCancelled(true);
                }
            }
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
        if(event instanceof EventTick && target != null && minecraft.inWorld()) {
            distanceToTarget = target.getDistanceToEntity(minecraft.thePlayer);

            //navigation();
            pathFinderNavigation();

            autoNav = true;

            if(autoNav) {
                if(distanceToTarget < 4) {
                    rodTicks = 0;

                    minecraft.thePlayer.inventory.currentItem = inventorySwordSlot;
                    minecraft.playerController.syncCurrentPlayItem();

                    approachTarget();
                    strafeTarget();

                    if(minecraft.thePlayer.isCollidedHorizontally) {
                        minecraft.thePlayer.jump();
                    } else {
                        minecraft.thePlayer.setJumping(false);
                    }

                    attackTarget();
                } else {
                    startMoving();

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
                }
            }

            aimOnTarget();

            if(!target.isEntityAlive()) {
                loseTarget();
            }
        }
    }

    @Override
    protected void disable() {
        super.disable();
        loseTarget();
    }

    @Override
    protected void enable() {
        super.enable();
        targetStrafeTicks = 50 + minecraft.theWorld.rand.nextInt(30);
    }

    private void approachTarget() {
        if(settingManager.<SettingBoolean>getSetting("approach_target").getValue()) {
            if(distanceToTarget < 1) {
                minecraft.thePlayer.movementInput.moveForward = -1;
            } else {
                if(distanceToTarget > 2.5) {
                    startMoving();
                } else {
                    stopMoving();
                }
            }
        } else {
            stopMoving();
        }
    }

    private void strafeTarget() {
        if(settingManager.<SettingBoolean>getSetting("strafe").getValue()) {
            System.out.println(new Vec3(minecraft.thePlayer.motionX, minecraft.thePlayer.motionY, minecraft.thePlayer.motionZ).lengthVector());
            if(distanceToTarget > 2.5) {
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
        }
    }

    private void attackTarget() throws Exception {
        if(attackTicks > 2) {
            attackTicks = 0;
            EventClickMouse eventClickMouse = new EventClickMouse();
            eventClickMouse.setButton(0);
            eventClickMouse.setCustomFromTarget(id);
            eventClickMouse.post();

            if(hitCounter > 5) {
                hitCounter = 0;
                stopMoving();
                EventClickMouse eventRightClickMouse = new EventClickMouse();
                eventRightClickMouse.setButton(1);
                eventRightClickMouse.setCustomFromTarget(id);
                eventRightClickMouse.post();
                startMoving();
            } else {
                hitCounter++;
            }
        } else {
            attackTicks++;
        }
    }

    private void aimOnTarget() {
        if(settingManager.<SettingBoolean>getSetting("aim_lock").getValue()) {
            if(lerpTicks > 7) {
                lerpTicks = 0;
            } else {
                lerpTicks++;
            }

            CommonMath.updateValues(minecraft.thePlayer, target);

            if(settingManager.<SettingBoolean>getSetting("interpolate").getValue()) {
                minecraft.thePlayer.rotationYaw = (float) Math.toDegrees(CommonMath.lerpAngle((float) Math.toRadians(minecraft.thePlayer.rotationYaw), (float) Math.toRadians(CommonMath.yawToFaceEntity()), (lerpTicks + minecraft.timer.elapsedPartialTicks - 0) / 7f));
                minecraft.thePlayer.rotationPitch = (float) Math.toDegrees(CommonMath.lerpAngle((float) Math.toRadians(minecraft.thePlayer.rotationPitch), (float) Math.toRadians(CommonMath.pitchToFaceEntity()), (lerpTicks + minecraft.timer.elapsedPartialTicks - 0) / 7f));
            } else {
                minecraft.thePlayer.rotationYaw = CommonMath.yawToFaceEntity();
                minecraft.thePlayer.rotationPitch = CommonMath.pitchToFaceEntity();
            }
        }
    }

    private void rodTarget() {

    }

    private void loseTarget() {
        target = null;
        lerpTicks = 0;
        attackTicks = 0;
        rodTicks = 0;
        strafeTicks = 0;
        hitCounter = 0;
        stopMoving();
    }

    private final Map<Vector2f, int[][]> pathDataMap = new HashMap<>();
    private List<Point> finalPath;

    private void pathFinderNavigation() {
        Vector2f chunkCoords = new Vector2f(minecraft.thePlayer.getPosition().getX() >> 4, minecraft.thePlayer.getPosition().getZ() >> 4);
        Chunk chunk = minecraft.theWorld.getChunkFromChunkCoords((int) chunkCoords.x, (int) chunkCoords.y);

        int x = chunk.xPosition << 4;
        int z = chunk.zPosition << 4;

        pathDataMap.computeIfAbsent(chunkCoords, vector2f -> {
            int[][] pathData = new int[16][16];

            for(int xx = x; xx < x + 16; xx++) {
                for(int zz = z; zz < z + 16; zz++) {
                    List<Block> dangerBlocks = Arrays.asList(DANGER_BLOCKS);
                    int xxx = xx - x;
                    int zzz = zz - z;
                    IBlockState state = minecraft.theWorld.getBlockState(new BlockPos(xx, minecraft.thePlayer.getPosition().getY(), zz));
                    IBlockState stateDown = minecraft.theWorld.getBlockState(new BlockPos(xx, minecraft.thePlayer.getPosition().getY() - 1, zz));
                    if(dangerBlocks.contains(state.getBlock()) || dangerBlocks.contains(stateDown.getBlock())) {
                        pathData[xxx][zzz] = 1;
                    } else {
                        pathData[xxx][zzz] = 0;
                    }
                }
            }
            System.out.println(Arrays.deepToString(pathData));
            return pathData;
        });

        List<Point> path =  PathFinder.FindPath(
                pathDataMap.get(chunkCoords),
                new Point(
                        minecraft.thePlayer.getPosition().getX() - x,
                        minecraft.thePlayer.getPosition().getZ() - z
                ),
                new Point(
                        target.getPosition().getX() - x,
                        target.getPosition().getZ() - z
                )
        );

        if(path != null) {
            finalPath = new ArrayList<>();
            for(Point point : path) {
                System.out.println(new Point(point.x + x, point.y + z));
                finalPath.add(new Point(point.x + x, point.y + z));
            }
        }


    }

    private void legacyNavigation() {
        double predX = minecraft.thePlayer.posX + (minecraft.thePlayer.motionX * getBPSState()) * minecraft.timer.renderPartialTicks;
        double predY = minecraft.thePlayer.posY + (minecraft.thePlayer.motionY * getBPSState()) * minecraft.timer.renderPartialTicks;
        double predZ = minecraft.thePlayer.posZ + (minecraft.thePlayer.motionZ * getBPSState()) * minecraft.timer.renderPartialTicks;

        BlockPos predictedLocation = new BlockPos(Math.round(predX), Math.round(predY), Math.round(predZ));

        System.out.println("a");
        System.out.println(minecraft.thePlayer.getPosition());
        System.out.println("b");
        System.out.println(predictedLocation);

        IBlockState predictedBlock = minecraft.theWorld.getBlockState(predictedLocation);
        IBlockState predictedBlockDown = minecraft.theWorld.getBlockState(predictedLocation.down());

        System.out.println(predictedBlock.getBlock().toString());
        System.out.println(predictedBlockDown.getBlock().toString());

        List<Block> dangerBlocks = Arrays.asList(DANGER_BLOCKS);

        if(predictedBlock.getBlock() != Blocks.air && !dangerBlocks.contains(predictedBlock.getBlock())) {
            minecraft.thePlayer.jump();
        }

        BlockPos groundBlock = findBlock(predictedLocation.down());
        IBlockState predictedBlockGround = minecraft.theWorld.getBlockState(groundBlock);

        if(dangerBlocks.contains(predictedBlock.getBlock()) || dangerBlocks.contains(predictedBlockDown.getBlock()) || (predictedBlockGround.getBlock() != Blocks.water && predictedLocation.down().getY() - groundBlock.getY() > 13)) {
            if(autoNav) {
                stopMoving(true);
                minecraft.thePlayer.movementInput.moveStrafe = 0;
                minecraft.thePlayer.setJumping(false);
            }
            autoNav = false;
        } else {
            autoNav = true;
        }
    }

    private BlockPos findBlock(BlockPos start) {
        int airBlocks = 0;
        while(true) {
            if(minecraft.theWorld.getBlockState(start.down(airBlocks + 1)).getBlock() == Blocks.air) {
                airBlocks++;
            } else {
                break;
            }
        }
        return start.down(airBlocks);
    }

    private double getBPSState() {
        return 6;
    }

    private double getBPSSState() {
        if(minecraft.thePlayer.isSprinting()) {
            return 5.612;
        }
        if(minecraft.thePlayer.isSneaking()) {
            return 1.3;
        }
        return 4.317;
    }

    private void stopMoving() {
        if(!autoNav) return;
        minecraft.thePlayer.setSprinting(false);
        minecraft.thePlayer.movementInput.moveForward = 0;
    }

    private void startMoving() {
        if(!autoNav) return;
        minecraft.thePlayer.setSprinting(true);
        minecraft.thePlayer.movementInput.moveForward = 1;
    }

    private void stopMoving(boolean b) {
        if(!b) return;
        minecraft.thePlayer.setSprinting(false);
        minecraft.thePlayer.movementInput.moveForward = 0;
    }

    private void startMoving(boolean b) {
        if(!b) return;
        minecraft.thePlayer.setSprinting(true);
        minecraft.thePlayer.movementInput.moveForward = 1;
    }

    private void jump() {
        if(!autoNav) return;
        if(minecraft.thePlayer.onGround) {
            minecraft.thePlayer.jump();
        }
    }

    private void jump(boolean b) {
        if(!b) return;
        if(minecraft.thePlayer.onGround) {
            minecraft.thePlayer.jump();
        }
    }

    public EntityLivingBase getTarget() {
        return target;
    }
}
