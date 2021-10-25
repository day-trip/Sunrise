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
import com.daytrip.sunrise.hack.setting.impl.SettingBoolean;
import com.daytrip.sunrise.hack.task.Task;
import com.daytrip.sunrise.module.ModulePathFinding;
import com.daytrip.sunrise.util.math.RandomMath;
import com.daytrip.sunrise.util.math.interpolation.InterpolationMath;
import com.daytrip.sunrise.util.minecraft.ExtendedReach;
import com.daytrip.sunrise.util.timer.TickTimer;
import com.daytrip.sunrise.util.timer.TimerManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.function.Supplier;

public class BotAutoFighter extends Hack {
    // HotBar slots TODO: make customizable
    private static final int inventorySwordSlot = 0;
    private static final int inventoryRodSlot = 3;

    // The target (rip them lol)
    private EntityLivingBase target;
    // The distance to the target
    private double distanceToTarget;

    private float yaw;
    private float pitch;
    private double angleDistance;

    // The timers
    private TickTimer rodTimer;
    private TickTimer cameraInterpolationTimer;

    // Whether or not the bot can melee
    private boolean canSword = true;

    // Whether or not the bot can approach the target
    private boolean canMove = true;

    private String strafe = "left";
    private RandomMath.RandomInteger targetStrafeTicks;

    private int hitCounter;
    private RandomMath.RandomInteger targetHitCounter;
    private RandomMath.RandomInteger targetWTapTicks;

    private ModulePathFinding modulePathFinding;

    public BotAutoFighter() {
        super(Keyboard.KEY_P, "Auto Fighter", "auto_fighter");
    }

    @Override
    protected void init() {
        targetStrafeTicks = new RandomMath.RandomInteger(9, 18, true);
        targetHitCounter = new RandomMath.RandomInteger(1, 4, true);
        targetWTapTicks = new RandomMath.RandomInteger(2, 5, true);

        modulePathFinding = new ModulePathFinding();
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
                    yaw = math.yawToFaceEntity(minecraft.thePlayer.getPositionVector(), target.getPositionVector(), (float) ((target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY) / 2));
                    pitch = math.pitchToFaceEntity(minecraft.thePlayer.getPositionVector(), target.getPositionVector(), (float) ((target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY) / 2));
                })
                .onTick(() -> {
                    distanceToTarget = target.getDistanceToEntity(minecraft.thePlayer);

                    Vec3 vec3 = target.getPositionVector();
                    yaw = math.yawToFaceEntity(minecraft.thePlayer.getPositionVector(), vec3, (float) (((target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY) / 2)));
                    pitch = math.pitchToFaceEntity(minecraft.thePlayer.getPositionVector(), vec3, (float) (((target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY) / 2)));

                    angleDistance = Math.atan2(Math.sin(yaw - minecraft.thePlayer.getRotationYaw()), Math.cos(yaw - minecraft.thePlayer.getRotationYaw()));
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
                    if(settingManager.<SettingBoolean>getSetting("aim_lock").getValue()) {
                        if(settingManager.<SettingBoolean>getSetting("interpolate").getValue()) {
                            cameraInterpolationTimer.update();
                            float progress = (cameraInterpolationTimer.getCurrentTicks() + minecraft.timer.elapsedPartialTicks) / cameraInterpolationTimer.getTargetTicks();
                            minecraft.thePlayer.setRotationYaw((float) Math.toDegrees(InterpolationMath.angleLinearInterpolate((float) Math.toRadians(minecraft.thePlayer.getRotationYaw()), (float) Math.toRadians(yaw), progress)));
                            minecraft.thePlayer.setRotationPitch((float) Math.toDegrees(InterpolationMath.angleLinearInterpolate((float) Math.toRadians(minecraft.thePlayer.getRotationPitch()), (float) Math.toRadians(pitch), progress)));
                        } else {
                            minecraft.thePlayer.setRotationYaw(yaw);
                            minecraft.thePlayer.setRotationPitch(pitch);
                        }
                    }
                })
        );

        taskManager.registerTask(1, new Task()
                .withName("PVP Module: Sword")
                .executeIf(() -> canSword && distanceToTarget < 6 && angleDistance < 25)
                .onTick(() -> {
                    rodTimer.reset();

                    if(canMove) {
                        HackAPI.startSprinting();

                    }

                    if(RandomMath.getRandomBoolean(0.3f)) { // Accounts for people randomly changing slots
                        HackAPI.changeInventorySlotAndUpdate(inventorySwordSlot);
                    }

                    if(RandomMath.getRandomBoolean(0.7f)) { // About 14 cps
                        if(RandomMath.getRandomBoolean(0.95f)) { // Sometimes you forget to use your sword
                            HackAPI.changeInventorySlotAndUpdate(inventorySwordSlot);
                        }
                        HackAPI.leftClick(id);
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
                            HackAPI.stopMovingAndSprinting();
                        }
                    }
                })
        );

        taskManager.registerTask(2, new Task()
                .withName("PVP Module: Strafe Target")
                .executeIf(() -> settingManager.<SettingBoolean>getSetting("strafe").getValue() && canMove)
                .whenCannotExecute(() -> minecraft.thePlayer.movementInput.moveStrafe = 0)
                .callEvery(() -> targetStrafeTicks.get(), () -> {
                    targetStrafeTicks.generate();
                    if("left".equals(strafe)) {
                        strafe = "right";
                    } else {
                        strafe = "left";
                    }
                }).nextInterval(targetStrafeTicks::get)
                .onTick(() -> {
                    if(distanceToTarget > 2.5 && distanceToTarget < 10) {
                        if("left".equals(strafe)) {
                            minecraft.thePlayer.movementInput.moveStrafe = -1;
                        } else {
                            minecraft.thePlayer.movementInput.moveStrafe = 1;
                        }
                    }
                })
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
            EventRenderBrightnessBuffer eventRenderBrightnessBuffer = (EventRenderBrightnessBuffer) event;
            if(eventRenderBrightnessBuffer.onEntity == target) {
                event.setCancelled(true);
                Render<EntityLivingBase> render = minecraft.getRenderManager().getEntityRenderObject(target);
                if(render instanceof RendererLivingEntity) {
                    float brightness = target.getBrightness(0);
                    int colorMultiplier = ((RendererLivingEntity<?>) render).getColorMultiplier(target, brightness, 0);
                    boolean flag = (colorMultiplier >> 24 & 255) > 0;
                    boolean showDamageOverlay = target.hurtTime > 0 || target.deathTime > 0;

                    if (!flag && !eventRenderBrightnessBuffer.combineTextures)
                    {
                        return;
                    }

                    ((RendererLivingEntity<?>) render).prepareBrightness();
                    if(showDamageOverlay) {
                        eventRenderBrightnessBuffer.buffer.put(0);
                        eventRenderBrightnessBuffer.buffer.put(1);
                        eventRenderBrightnessBuffer.buffer.put(0);
                        eventRenderBrightnessBuffer.buffer.put(0.7f);
                    } else {
                        eventRenderBrightnessBuffer.buffer.put(0);
                        eventRenderBrightnessBuffer.buffer.put(1);
                        eventRenderBrightnessBuffer.buffer.put(1);
                        eventRenderBrightnessBuffer.buffer.put(0.7f);
                    }
                    ((RendererLivingEntity<?>) render).afterBrightness();
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
            if(((EventPlayerDamaged) event).damageSource instanceof EntityDamageSourceIndirect) {
                EntityDamageSourceIndirect sourceIndirect = (EntityDamageSourceIndirect) ((EventPlayerDamaged) event).damageSource;
                if(sourceIndirect.getEntity() == target || sourceIndirect.getEntity() == minecraft.thePlayer) {
                    HackAPI.startMovingAndSprinting();
                }
            }

            // Some sources say this reduces KB, others don't
            /*if(((EventPlayerDamaged) event).player == minecraft.thePlayer) {
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
                    if(hitCounter >= targetHitCounter.get()) {
                        hitCounter = 0;
                        targetHitCounter.generate();
                        if(canMove) {
                            canMove = false;
                            if(RandomMath.getRandomBoolean(0.35f)) {
                                HackAPI.rightClick(id);
                            }
                            HackAPI.stopMovingAndSprinting();
                            TimerManager.registerTimer(new TickTimer(() -> {
                                targetWTapTicks.generate();
                                canMove = true;
                                HackAPI.startMovingAndSprinting();
                            }, targetWTapTicks.get(), false));
                        }
                    } else {
                        hitCounter++;
                    }
                }
            }
        }
        if(event instanceof EventTick && target != null && minecraft.inWorld()) {
            taskManager.tick();

            //modulePathFinding.run(target);
        }
    }

    @Override
    protected void disable() {
        super.disable();
        loseTarget();
    }

    private void loseTarget() {
        taskManager.stopAndEndLine();

        target = null;

        rodTimer = null;
        cameraInterpolationTimer = null;

        HackAPI.stopMovingAndSprinting();
    }

    private void pickTarget(EntityLivingBase target) {
        this.target = target;

        rodTimer = new TickTimer(() -> rod(() -> Math.max(17, Math.min(33, (int) distanceToTarget * 5))), 45, true);

        cameraInterpolationTimer = TickTimer.createNoAction(5 - 1, true); // 5 - 1 because of > instead of >=

        taskManager.start();
    }

    @Override
    protected void enable() {
        super.enable();
    }

    private void rod(Supplier<Integer> castTimeTicks) {
        canSword = false;
        canMove = false;

        HackAPI.stopMovingAndSprinting();

        HackAPI.changeInventorySlotAndUpdate(inventoryRodSlot);
        TimerManager.registerTimer(new TickTimer(() -> {
            HackAPI.changeInventorySlotAndUpdate(inventoryRodSlot);
            HackAPI.rightClick(id);
        }, 7, false));

        TimerManager.registerTimer(new TickTimer(() -> {
            HackAPI.changeInventorySlotAndUpdate(inventoryRodSlot);
            HackAPI.rightClick(id);

            canSword = true;
            canMove = true;
            HackAPI.startMovingAndSprinting();
        }, castTimeTicks.get(), false));
    }

    public EntityLivingBase getTarget() {
        return target;
    }
}
