package com.daytrip.sunrise.hack.impl.bot;

import com.daytrip.shared.ExtendedReach;
import com.daytrip.shared.event.Event;
import com.daytrip.shared.event.impl.*;
import com.daytrip.shared.math.ArrayGrid;
import com.daytrip.shared.math.CommonMath;
import com.daytrip.shared.math.Vec2;
import com.daytrip.shared.timer.TickTimer;
import com.daytrip.shared.timer.TimerManager;
import com.daytrip.sunrise.hack.Hack;
import com.daytrip.sunrise.hack.pathfinding.PathFinder;
import com.daytrip.sunrise.hack.pathfinding.Point;
import com.daytrip.sunrise.hack.setting.impl.SettingBoolean;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotAutoFighter extends Hack {
    // Dangerous blocks to avoid while navigating
    private final Block[] DANGER_BLOCKS = { Blocks.fire, Blocks.lava, Blocks.flowing_lava, Blocks.water, Blocks.flowing_water, Blocks.soul_sand, Blocks.web };

    // Hotbar slots TODO: make customizable
    private static final int inventorySwordSlot = 0;
    private static final int inventoryRodSlot = 3;

    // The target (rip them lol)
    private EntityLivingBase target;
    // The distance to the target
    private double distanceToTarget;

    // Whether or not to automatically navigate
    private boolean autoNavigate;

    // The timers
    private TickTimer attackTimer;
    private TickTimer rodTimer;
    private TickTimer strafeTimer;
    private TickTimer cameraInterpolationTimer;

    // Whether or not the bot can melee
    private boolean canSword = true;
    // The amount of hits left until the bot has to rod the target
    private int mustRodCounter;
    // The amount of times the bot hit the target
    private int hitCounter;

    // Whether or not the bot can approach the target
    private boolean canMove = true;

    private String strafe = "left";
    private int strafeTicks;
    private int targetStrafeTicks;

    private boolean lockAim;
    private BlockPos aimLockPos;

    public BotAutoFighter() {
        super(Keyboard.KEY_P, "Auto Fighter", "auto_fighter");
        settingManager.addSetting(new SettingBoolean("Strafe", "strafe", true));
        settingManager.addSetting(new SettingBoolean("Move Towards Target", "approach_target", true));
        settingManager.addSetting(new SettingBoolean("Aim Lock", "aim_lock", true));
        settingManager.addSetting(new SettingBoolean("Interpolate Aim", "interpolate", true));
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventProcessMouse && target != null && minecraft.inWorld()) {
            // Only the bot should be able to move the mouse
            if(settingManager.<SettingBoolean>getSetting("aim_lock").getValue()) {
                event.setCancelled(true);
            }
        }
        if(event instanceof EventClickMouse && minecraft.inWorld()) {
            // Don't do anything if the bot is clicking
            if(event.getCustomFromTarget().equals(id)) return;

            // If a target is set, only bot should have control
            if(target != null) {
                event.setCancelled(true);
            }

            // Pick the selected entity as the (new) target
            if(((EventClickMouse) event).getButton() == 2) {
                ExtendedReach reach = new ExtendedReach(null, 50, minecraft.thePlayer);
                reach.calculateMouseOver();
                if(reach.objectMouseOver.entityHit instanceof EntityLivingBase) {
                    pickTarget((EntityLivingBase) reach.objectMouseOver.entityHit);
                    event.setCancelled(true);
                }
            }
        }
        if(event instanceof EventRenderBrightnessBuffer && target != null && minecraft.inWorld()) {
            EventRenderBrightnessBuffer eventRBB = (EventRenderBrightnessBuffer) event;
            if(eventRBB.getEntityLivingBase() == target) {
                // Custom damage color for the target
                if(eventRBB.getContext() == 0) {
                    eventRBB.setR(0.0F);
                    eventRBB.setG(1.0F);
                    eventRBB.setB(0.0F);
                    eventRBB.setA(0.3F);
                }
            }
        }
        if(event instanceof EventUpdateMovementInput && target != null && minecraft.inWorld()) {
            // Only the bot should be able to use the controls
            event.setCancelled(true);
        }
        if(event instanceof EventKeypress) {
            if(minecraft.inWorld() && target != null) {
                if(((EventKeypress) event).getKey() == Keyboard.KEY_ESCAPE) {
                    loseTarget();

                }
                if(((EventKeypress) event).getKey() != Keyboard.KEY_F5) {
                    // Only the bot should have keyboard control, allows F5 as a utility to the user (AKA to flex)
                    event.setCancelled(true);
                }
                if(((EventKeypress) event).getKey() == Keyboard.KEY_SPACE) {
                    // Allow user to override jumping
                    jump();
                }
            }
        }
        if(event instanceof EventPlayerDamaged) {
            if(((EventPlayerDamaged) event).player == minecraft.thePlayer) {
                // Some sources say this reduces KB, others don't
                //TimerManager.registerTimer(new TickTimer(tickTimer -> minecraft.thePlayer.jump(), 3, false));
            }
        }
        if(event instanceof EventEntityAttackedByPlayer) {
            if(minecraft.inWorld() && target != null) {
                if(((EventEntityAttackedByPlayer) event).getAttacker() == minecraft.thePlayer && ((EventEntityAttackedByPlayer) event).getTarget() == target) {
                    System.out.println("Da hit counter has incremented!");
                    hitCounter++;
                }
            }
        }
        if(event instanceof EventTick && target != null && minecraft.inWorld()) {
            if(!target.isEntityAlive()) {
                // Don't do anything if the target died
                loseTarget();
                return;
            }

            // Get the distance to the target
            distanceToTarget = target.getDistanceToEntity(minecraft.thePlayer);

            //navigation();
            //pathFinderNavigation();

            autoNavigate = true;

            if(autoNavigate) {
                if(!lockAim) {
                    aimOnTarget();
                } else {
                    CommonMath.updateValues(minecraft.thePlayer, new Vec3(aimLockPos.getX(), aimLockPos.getY(), aimLockPos.getZ()));
                    minecraft.thePlayer.rotationYaw = CommonMath.yawToFaceEntity();
                    minecraft.thePlayer.rotationPitch = CommonMath.pitchToFaceEntity();
                }

                //minecraft.thePlayer.movementInput.jump = false;
                //minecraft.thePlayer.setJumping(false);
                minecraft.thePlayer.movementInput.sneak = false;
                minecraft.thePlayer.setSneaking(false);

                approachTarget();
                strafeTarget();

                if(distanceToTarget < 6) {
                    rodTimer.reset();
                    attackTimer.update();
                } else {
                    rodTimer.update();
                }
            }
        }
    }

    @Override
    protected void disable() {
        super.disable();
        loseTarget();
    }

    private void loseTarget() {
        target = null;

        attackTimer = null;
        rodTimer = null;
        strafeTimer = null;
        cameraInterpolationTimer = null;

        strafeTicks = 0;
        hitCounter = 0;
        mustRodCounter = 0;

        stopMoving();
    }

    private void pickTarget(EntityLivingBase target) {
        this.target = target;
        CommonMath.updateValues(minecraft.thePlayer, target);
        System.out.println("i-was-here#1");
        attackTimer = new TickTimer(tickTimer -> {
            if(canSword) {
                minecraft.thePlayer.inventory.currentItem = inventorySwordSlot;
                minecraft.playerController.syncCurrentPlayItem();

                if(!minecraft.thePlayer.isSprinting() && canMove) {
                    minecraft.thePlayer.setSprinting(true);
                }

                EventClickMouse eventClickMouse = new EventClickMouse();
                eventClickMouse.setButton(0);
                eventClickMouse.setCustomFromTarget(id);
                try {
                    eventClickMouse.post();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(hitCounter == 1) {
                    // Is block hitting worth it?

                    EventClickMouse eventRightClickMouse = new EventClickMouse();
                    eventRightClickMouse.setButton(1);
                    eventRightClickMouse.setCustomFromTarget(id);
                    try {
                        eventRightClickMouse.post();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if(hitCounter > 3) {
                    System.out.println("Da hit counter was greater than 3!");
                    hitCounter = 0;

                    stopMoving();
                    canMove = false;
                    //canSword = false;
                    //TimerManager.registerTimer(new TickTimer(tickTimer1 -> canSword = true, 3, false));
                    TimerManager.registerTimer(new TickTimer(tickTimer1 -> {
                        canMove = true;
                        startMoving();
                    }, 6, false));
                }

                /*
                if(mustRodCounter > 30 && target.isSprinting() & distanceToTarget > 4) {
                    mustRodCounter = 0;
                    rod(7);
                } else {
                    mustRodCounter++;
                }

                 */
            }
        }, 1, true);

        rodTimer = new TickTimer(tickTimer -> rod(10), 15, true);

        cameraInterpolationTimer = TickTimer.createNoAction(5 - 1, true); // 5 - 1 because of > instead of >=
    }

    @Override
    protected void enable() {
        super.enable();
        targetStrafeTicks = 25 + minecraft.theWorld.rand.nextInt(5);
    }

    private void rod(int castTimeTicks) {
        canSword = false;

        minecraft.thePlayer.inventory.currentItem = inventoryRodSlot;
        minecraft.playerController.syncCurrentPlayItem();

        EventClickMouse eventRightClickMouse = new EventClickMouse();
        eventRightClickMouse.setButton(1);
        eventRightClickMouse.setCustomFromTarget(id);
        try {
            eventRightClickMouse.post();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TimerManager.registerTimer(new TickTimer(tickTimer -> {
            minecraft.thePlayer.inventory.currentItem = inventoryRodSlot;
            minecraft.playerController.syncCurrentPlayItem();

            EventClickMouse eventRightClickMouse1 = new EventClickMouse();
            eventRightClickMouse1.setButton(1);
            eventRightClickMouse1.setCustomFromTarget(id);
            try {
                eventRightClickMouse1.post();
            } catch (Exception e) {
                e.printStackTrace();
            }

            canSword = true;
        }, castTimeTicks, false));
    }

    private void approachTarget() {
        if(settingManager.<SettingBoolean>getSetting("approach_target").getValue()) {
            if(canMove) {
                if(distanceToTarget < 0.5) {
                    minecraft.thePlayer.movementInput.moveForward = -1;
                } else {
                    if(distanceToTarget > 2.5) {
                        startMoving();
                    } else {
                        stopMoving();
                    }
                }
            }
        }
    }

    private void strafeTarget() {
        if(settingManager.<SettingBoolean>getSetting("strafe").getValue()) {
            if(distanceToTarget > 2.5 && distanceToTarget < 10) {
                if(strafeTicks > targetStrafeTicks) {
                    strafeTicks = 0;
                    targetStrafeTicks = 25 + minecraft.theWorld.rand.nextInt(5);
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

    private void aimOnTarget() {
        if(settingManager.<SettingBoolean>getSetting("aim_lock").getValue()) {
            CommonMath.updateValues(minecraft.thePlayer, target);

            if(settingManager.<SettingBoolean>getSetting("interpolate").getValue()) {
                cameraInterpolationTimer.update();

                float progress = (cameraInterpolationTimer.getCurrentTicks() + minecraft.timer.elapsedPartialTicks) / cameraInterpolationTimer.getTargetTicks();
                minecraft.thePlayer.rotationYaw = (float) Math.toDegrees(CommonMath.lerpAngle((float) Math.toRadians(minecraft.thePlayer.rotationYaw), (float) Math.toRadians(CommonMath.yawToFaceEntity()), progress));
                minecraft.thePlayer.rotationPitch = (float) Math.toDegrees(CommonMath.lerpAngle((float) Math.toRadians(minecraft.thePlayer.rotationPitch), (float) Math.toRadians(CommonMath.pitchToFaceEntity()), progress));
            } else {
                minecraft.thePlayer.rotationYaw = CommonMath.yawToFaceEntity();
                minecraft.thePlayer.rotationPitch = CommonMath.pitchToFaceEntity();
            }
        }
    }

    private final Map<Vec2, int[][]> pathDataMap = new HashMap<>();

    private void generatePathData(Vec2 chunkCoords) {
        Chunk chunk = minecraft.theWorld.getChunkFromChunkCoords(chunkCoords.getX(), chunkCoords.getZ());

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
            CommonMath.printNeat(pathData);
            return pathData;
        });
    }

    private void pathFinderNavigation() {
        Vec2 chunkCoords = new Vec2(minecraft.thePlayer.getPosition().getX() >> 4, minecraft.thePlayer.getPosition().getZ() >> 4);
        Chunk chunk = minecraft.theWorld.getChunkFromChunkCoords(chunkCoords.getX(), chunkCoords.getZ());

        int x = chunk.xPosition << 4;
        int z = chunk.zPosition << 4;

        // Middle middle
        generatePathData(chunkCoords);
        // Middle right
        generatePathData(chunkCoords.clone().add(1, 0));
        // Middle left
        generatePathData(chunkCoords.clone().add(-1, 0));
        // Top middle
        generatePathData(chunkCoords.clone().add(0, 1));
        // Bottom middle
        generatePathData(chunkCoords.clone().add(0, -1));
        // Top right
        generatePathData(chunkCoords.clone().add(1, 1));
        // Bottom left
        generatePathData(chunkCoords.clone().add(-1, -1));
        // Bottom right
        generatePathData(chunkCoords.clone().add(1, -1));
        // Top left
        generatePathData(chunkCoords.clone().add(-1, 1));

        int[][] finalArray = ArrayGrid.create()
                .add(pathDataMap.get(chunkCoords), 1, 1)

                .add(pathDataMap.get(chunkCoords.clone().add(1, 0)), 2, 1)
                .add(pathDataMap.get(chunkCoords.clone().add(-1, 0)), 0, 1)
                .add(pathDataMap.get(chunkCoords.clone().add(0, 1)), 1, 2)
                .add(pathDataMap.get(chunkCoords.clone().add(0, -1)), 1, 0)

                .add(pathDataMap.get(chunkCoords.clone().add(1, 1)), 2, 2)
                .add(pathDataMap.get(chunkCoords.clone().add(-1, -1)), 0, 0)
                .add(pathDataMap.get(chunkCoords.clone().add(1, -1)), 2, 0)
                .add(pathDataMap.get(chunkCoords.clone().add(-1, 1)), 0, 2)

                .build();

        List<Point> path =  PathFinder.FindPath(
                finalArray,
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
            int i = 0;
            for(Point point : path) {
                Point p = new Point(point.x + x, point.y + z);
                System.out.println(p);
                lockAim = true;
                if(i == 0) {
                    aimLockPos = new BlockPos(p.x, minecraft.thePlayer.getPosition().getY() + 1, p.y);
                }
                i++;
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
            if(autoNavigate) {
                stopMoving(true);
                minecraft.thePlayer.movementInput.moveStrafe = 0;
                minecraft.thePlayer.setJumping(false);
            }
            autoNavigate = false;
        } else {
            autoNavigate = true;
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
        if(!autoNavigate) return;
        minecraft.thePlayer.setSprinting(false);
        minecraft.thePlayer.movementInput.moveForward = 0;
    }

    private void startMoving() {
        if(!autoNavigate) return;
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
        if(!autoNavigate) return;
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
