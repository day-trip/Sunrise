package com.daytrip.sunrise.hack.impl;

import com.daytrip.shared.CommonMath;
import com.daytrip.shared.ExtendedReach;
import com.daytrip.shared.event.Event;
import com.daytrip.shared.event.impl.EventClickMouse;
import com.daytrip.shared.event.impl.EventProcessMouse;
import com.daytrip.shared.event.impl.EventTick;
import com.daytrip.sunrise.hack.Hack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;


/**
 * Uses similar math to Auto Fighter
 */
public class HackAimbot extends Hack {
    private float lerpTicks;

    private EntityLivingBase target;

    public HackAimbot() {
        super(Keyboard.KEY_V, "Bow Aimbot", "aimbot");
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventProcessMouse && minecraft.inWorld() && target != null) {
            event.setCancelled(true);
        }
        if(event instanceof EventClickMouse) {
            if(((EventClickMouse) event).getButton() == 2) {
                ExtendedReach reach = new ExtendedReach(null, 25, minecraft.thePlayer);
                reach.calculateMouseOver();
                if(reach.objectMouseOver.entityHit instanceof EntityLivingBase) {
                    target = (EntityLivingBase) reach.objectMouseOver.entityHit;
                }
                event.setCancelled(true);
            }
        }
        if(event instanceof EventTick && minecraft.inWorld() && target != null) {
            if(lerpTicks > 20) {
                lerpTicks = 0;
            } else {
                lerpTicks++;
            }

            Vec3 vec3 = target.getPositionVector();

            vec3.addVector(0, target.height / 4, 0);
            vec3.addVector(0, minecraft.thePlayer.getDistanceToEntity(target), 0);

            CommonMath.updateValues(minecraft.thePlayer, target, vec3);
            //minecraft.thePlayer.rotationYaw = (float) Math.toDegrees(CommonMath.lerpAngle((float) Math.toRadians(minecraft.thePlayer.rotationYaw), (float) Math.toRadians(CommonMath.yawToFaceEntity()), lerpTicks / 20));
            //minecraft.thePlayer.rotationPitch = (float) Math.toDegrees(CommonMath.lerpAngle((float) Math.toRadians(minecraft.thePlayer.rotationPitch), (float) Math.toRadians(CommonMath.pitchToFaceEntity()), lerpTicks / 20));
            minecraft.thePlayer.rotationYaw = CommonMath.yawToFaceEntity();
            minecraft.thePlayer.rotationPitch = CommonMath.pitchToFaceEntity();

            if(target.getHealth() <= 0.0F) {
                target = null;
            }
        }
    }

    @Override
    protected void disable() {
        super.disable();
        target = null;
    }
}
