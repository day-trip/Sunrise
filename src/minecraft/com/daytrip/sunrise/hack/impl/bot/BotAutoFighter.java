package com.daytrip.sunrise.hack.impl.bot;

import com.daytrip.sunrise.HackAPI;
import com.daytrip.sunrise.event.Event;
import com.daytrip.sunrise.event.EventHandler;
import com.daytrip.sunrise.event.EventIgnores;
import com.daytrip.sunrise.event.impl.EventEntityAttackedByPlayer;
import com.daytrip.sunrise.event.impl.EventPlayerDamaged;
import com.daytrip.sunrise.event.impl.EventRenderBrightnessBuffer;
import com.daytrip.sunrise.event.impl.EventTick;
import com.daytrip.sunrise.event.impl.input.EventClickMouse;
import com.daytrip.sunrise.event.impl.input.EventKeypress;
import com.daytrip.sunrise.event.impl.input.EventProcessMouse;
import com.daytrip.sunrise.event.impl.input.EventUpdateMovementInput;
import com.daytrip.sunrise.hack.Hack;
import com.daytrip.sunrise.hack.pathfinding.PathFinder;
import com.daytrip.sunrise.hack.pathfinding.Point;
import com.daytrip.sunrise.hack.setting.impl.SettingBoolean;
import com.daytrip.sunrise.hack.task.Task;
import com.daytrip.sunrise.util.math.ArrayGrid;
import com.daytrip.sunrise.util.math.ArrayMath;
import com.daytrip.sunrise.util.math.Interpolation;
import com.daytrip.sunrise.util.math.Vec2;
import com.daytrip.sunrise.util.minecraft.ExtendedReach;
import com.daytrip.sunrise.util.timer.TickTimer;
import com.daytrip.sunrise.util.timer.TimerManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.input.Keyboard;

import java.util.*;

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

    private float yaw;
    private float pitch;

    // Whether or not to automatically navigate
    private boolean autoNavigate = true;

    // The timers
    private TickTimer rodTimer;
    private TickTimer cameraInterpolationTimer;

    // Whether or not the bot can melee
    private boolean canSword = true;
    // The amount of hits left until the bot has to rod the target
    private int mustRodCounter;

    private boolean isRodding;

    // Whether or not the bot can approach the target
    private boolean canMove = true;

    private String strafe = "left";
    private int targetStrafeTicks;

    private boolean lockAim;
    private BlockPos aimLockPos;

    public BotAutoFighter() {
        super(Keyboard.KEY_P, "Auto Fighter", "auto_fighter");
    }

    @EventHandler
    public void onTick(EventTick eventTick) {
        System.out.println(eventTick.getClass().getSimpleName());
    }

    @EventIgnores
    public boolean ignores(Event event) {
        return false;
    }

    @Override
    protected void registerSettings() {
        settingManager.addSetting(new SettingBoolean("Strafe", "strafe", true));
        settingManager.addSetting(new SettingBoolean("Move Towards Target", "approach_target", true));
        settingManager.addSetting(new SettingBoolean("Aim Lock", "aim_lock", true));
        settingManager.addSetting(new SettingBoolean("Interpolate Aim", "interpolate", true));
    }

    @Override
    protected void registerTasks() {
        taskManager.registerTask(0, new Task()
                .withName("PVP Module: Dead Check")
                .executeIf(() -> true)
                .onTick(() -> {
                    if(target.isDead || target.getHealth() <= 0.0F) {
                        loseTarget();
                    }
                })
        );

        taskManager.registerTask(0, new Task()
                .withName("PVP Module: Basic Math Update")
                .executeIf(() -> true)
                .onInit(() -> {
                    distanceToTarget = target.getDistanceToEntity(minecraft.thePlayer);
                    yaw = math.yawToFaceEntity(minecraft.thePlayer.getPositionVector(), target.getPositionVector(), (float) ((target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY) / 2));
                    pitch = math.pitchToFaceEntity(minecraft.thePlayer.getPositionVector(), target.getPositionVector(), (float) ((target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY) / 2));
                })
                .onTick(() -> {
                    Vec3 vec3 = target.getPositionVector();
                    distanceToTarget = target.getDistanceToEntity(minecraft.thePlayer);
                    yaw = math.yawToFaceEntity(minecraft.thePlayer.getPositionVector(), vec3, (float) (((target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY) / 2)));
                    pitch = math.pitchToFaceEntity(minecraft.thePlayer.getPositionVector(), vec3, (float) (((target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY) / 2)));
                })
        );

        taskManager.registerTask(0, new Task()
                .withName("PVP Module: No crouch")
                .onTick(() -> {
                    minecraft.thePlayer.movementInput.sneak = false;
                    minecraft.thePlayer.setSneaking(false);
                })
        );

        taskManager.registerTask(1, new Task()
                .withName("PVP Module: Aim")
                .executeIf(() -> true)
                .onTick(() -> {
                    if(!lockAim) {
                        if(settingManager.<SettingBoolean>getSetting("aim_lock").getValue()) {
                            if(settingManager.<SettingBoolean>getSetting("interpolate").getValue()) {
                                cameraInterpolationTimer.update();

                                float progress = (cameraInterpolationTimer.getCurrentTicks() + minecraft.timer.elapsedPartialTicks) / cameraInterpolationTimer.getTargetTicks();
                                minecraft.thePlayer.rotationYaw = (float) Math.toDegrees(Interpolation.angleLinearInterpolate((float) Math.toRadians(minecraft.thePlayer.rotationYaw), (float) Math.toRadians(yaw), progress));
                                minecraft.thePlayer.rotationPitch = (float) Math.toDegrees(Interpolation.angleLinearInterpolate((float) Math.toRadians(minecraft.thePlayer.rotationPitch), (float) Math.toRadians(pitch), progress));
                            } else {
                                minecraft.thePlayer.rotationYaw = yaw;
                                minecraft.thePlayer.rotationPitch = pitch;
                            }
                        }
                    } else {
                        float yaw = math.yawToFaceEntity(minecraft.thePlayer.getPositionVector(), new Vec3(aimLockPos.getX(), aimLockPos.getY(), aimLockPos.getZ()), 0);
                        float pitch = math.pitchToFaceEntity(minecraft.thePlayer.getPositionVector(), new Vec3(aimLockPos.getX(), aimLockPos.getY(), aimLockPos.getZ()), 0);

                        minecraft.thePlayer.rotationYaw = yaw;
                        minecraft.thePlayer.rotationPitch = pitch;
                    }
                })
        );

        taskManager.registerTask(1, new Task()
                .withName("PVP Module: Sword")
                .executeIf(() -> canSword && distanceToTarget < 6)
                .whenCannotExecute(() -> isRodding = true)
                .callEvery(() -> 2, () -> {
                    isRodding = false;
                    rodTimer.reset();

                    minecraft.thePlayer.inventory.currentItem = inventorySwordSlot;
                    minecraft.playerController.syncCurrentPlayItem();

                    if(!minecraft.thePlayer.isSprinting() && canMove) {
                        minecraft.thePlayer.setSprinting(true);
                    }

                    float yawDist = (float) Math.atan2(Math.sin(yaw - minecraft.thePlayer.rotationYaw), Math.cos(yaw - minecraft.thePlayer.rotationYaw));
                    System.out.println(yawDist);
                    if(yawDist < 15) {
                        HackAPI.leftClick(id);
                        if(new Random().nextInt(100) > 50) {
                            HackAPI.leftClick(id);
                        }
                    }
                })
        );


        taskManager.registerTask(2, new Task()
                .withName("PVP Module: Approach Target")
                .executeIf(() -> settingManager.<SettingBoolean>getSetting("approach_target").getValue() && canMove)
                .onTick(() -> {
                    if(distanceToTarget < 0.5) {
                        minecraft.thePlayer.movementInput.moveForward = -1;
                    } else {
                        if(distanceToTarget > 2.5) {
                            HackAPI.startMovingAndSprinting();
                        } else {
                            HackAPI.stopMoving();
                        }
                    }
                })
        );

        taskManager.registerTask(2, new Task()
                .withName("PVP Module: Strafe Target")
                .executeIf(() -> settingManager.<SettingBoolean>getSetting("strafe").getValue() && distanceToTarget > 2.5 && distanceToTarget < 10)
                .whenCannotExecute(() -> minecraft.thePlayer.movementInput.moveStrafe = 0)
                .onInit(() -> targetStrafeTicks = 10 + minecraft.theWorld.rand.nextInt(15))
                .callEvery(() -> targetStrafeTicks, () -> {
                    targetStrafeTicks = 10 + minecraft.theWorld.rand.nextInt(15);
                    if(strafe.equals("left")) {
                        strafe = "right";
                    } else {
                        strafe = "left";
                    }
                }).nextInterval(() -> targetStrafeTicks)
                .onTick(() -> {
                    if(strafe.equals("left")) {
                        minecraft.thePlayer.movementInput.moveStrafe = -1;
                    } else {
                        minecraft.thePlayer.movementInput.moveStrafe = 1;
                    }
                })
        );

        taskManager.registerTask(3, new Task()
                .withName("PVP Module: Rod Target")
                .executeIf(() -> distanceToTarget > 6)
                .onTick(() -> rodTimer.update())
        );
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
                    HackAPI.jump();
                }
            }
        }
        if(event instanceof EventPlayerDamaged) {
            System.out.println("Who was damaged?");
            System.out.println("WHO!");
            System.out.println(((EventPlayerDamaged) event).player.getName() + " was!");

            if(((EventPlayerDamaged) event).damageSource instanceof EntityDamageSourceIndirect) {
                EntityDamageSourceIndirect sourceIndirect = (EntityDamageSourceIndirect) ((EventPlayerDamaged) event).damageSource;
                if(sourceIndirect.getEntity() == target || sourceIndirect.getEntity() == minecraft.thePlayer) {
                    HackAPI.startMovingAndSprinting();
                }
            }


            /*if(((EventPlayerDamaged) event).player == minecraft.thePlayer) {
                // Some sources say this reduces KB, others don't
                TimerManager.registerTimer(new TickTimer(tickTimer -> minecraft.thePlayer.jump(), 3, false));
            }*/
        }
        if(event instanceof EventEntityAttackedByPlayer) {
            if(minecraft.inWorld() && target != null) {
                if(((EventEntityAttackedByPlayer) event).getAttacker() == minecraft.thePlayer && ((EventEntityAttackedByPlayer) event).getTarget() == target) {
                    if(target instanceof EntityPlayer) {
                        if(HackAPI.getPingForPlayer((EntityPlayer) target) > 65) {
                            HackAPI.rightClick(id);
                        }
                    }
                    if(canMove) {
                        canMove = false;
                        TimerManager.registerTimer(new TickTimer(() -> {
                            canMove = true;
                            HackAPI.startMovingAndSprinting();
                        }, 5, false));
                    }
                }
            }
        }
        if(event instanceof EventTick && target != null && minecraft.inWorld()) {
            taskManager.tick();

            //navigation();
            //pathFinderNavigation();
        }
    }

    @Override
    protected void disable() {
        super.disable();
        loseTarget();
    }

    private void loseTarget() {
        taskManager.endChain();

        target = null;

        rodTimer = null;
        cameraInterpolationTimer = null;
        mustRodCounter = 0;

        HackAPI.stopMoving();
    }

    private void pickTarget(EntityLivingBase target) {
        this.target = target;

        rodTimer = new TickTimer(() -> rod(15), 18, true);

        cameraInterpolationTimer = TickTimer.createNoAction(5 - 1, true); // 5 - 1 because of > instead of >=

        taskManager.start();
    }

    @Override
    protected void enable() {
        super.enable();
        targetStrafeTicks = 10 + minecraft.theWorld.rand.nextInt(15);
    }

    private void rod(int castTimeTicks) {
        canSword = false;
        HackAPI.changeInventorySlotAndUpdate(inventoryRodSlot);
        HackAPI.rightClick(id);

        TimerManager.registerTimer(new TickTimer(() -> {
            HackAPI.changeInventorySlotAndUpdate(inventoryRodSlot);
            HackAPI.rightClick(id);

            canSword = true;
        }, castTimeTicks, false));
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
            ArrayMath.printNeat(pathData);
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
                //stopMoving(true);
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

    public EntityLivingBase getTarget() {
        return target;
    }
}
