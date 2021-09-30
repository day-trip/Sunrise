package com.daytrip.shared;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;

import java.util.List;

public class CommonMath {
    public static final float PI = 3.1415927f;
    public static final float PI2 = PI * 2;

    public static double dirx;
    public static double diry;
    public static double dirz;
    public static double len;

    /**
     * Updates the values for following functions
     * @param attacker The attacker
     * @param target The target
     */
    public static void updateValues(EntityLivingBase attacker, EntityLivingBase target) {
        dirx = attacker.posX - target.posX;
        diry = attacker.posY - target.posY + (target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY) / 2;
        dirz = attacker.posZ - target.posZ;
        len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);
    }


    /**
     * Updates the values for following functions
     * @param attacker The attacker
     * @param target The target
     * @param vec3 The custom defined position for the target
     */
    public static void updateValues(EntityLivingBase attacker, EntityLivingBase target, Vec3 vec3) {
        dirx = attacker.posX - vec3.xCoord;
        diry = attacker.posY - vec3.yCoord + (target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY) / 2;
        dirz = attacker.posZ - vec3.zCoord;
        len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);
    }

    public static void updateValues(EntityLivingBase attacker, Vec3 vec3) {
        dirx = attacker.posX - vec3.xCoord;
        diry = attacker.posY - vec3.yCoord;
        dirz = attacker.posZ - vec3.zCoord;
        len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);
    }

    /**
     * Uses trigonometry to find the yaw rotation necessary to be facing the target
     * @return the yaw rotation
     */
    public static float yawToFaceEntity() {
        dirx /= len;
        dirz /= len;

        double yaw = Math.atan2(dirz, dirx);

        yaw = yaw * 180.0 / Math.PI;

        yaw += 90f;

        return (float) yaw;
    }

    /**
     * Uses trigonometry to find the pitch rotation necessary to be facing the target
     * @return the pitch rotation
     */
    public static float pitchToFaceEntity() {
        diry /= len;
        double pitch = Math.asin(diry);
        pitch = pitch * 180.0 / Math.PI;
        return (float) pitch;
    }

    public static float lerpAngle(float fromRadians, float toRadians, float progress) {
        float delta = ((toRadians - fromRadians + PI2 + PI) % PI2) - PI;
        return (fromRadians + delta * progress + PI2) % PI2;
    }

    public static EntityLivingBase getTarget(float par1, double distance) {
        Minecraft mc = Minecraft.getMinecraft();

        Entity pointedEntity;
        MovingObjectPosition omo = mc.getRenderViewEntity().rayTrace(distance, par1);
        Vec3 vec3 = mc.getRenderViewEntity().getPositionVector();
        Vec3 vec31 = mc.getRenderViewEntity().getLook(par1);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance);
        pointedEntity = null;
        Vec3 vec33 = null;
        float f1 = 1.0F;
        List<Entity> list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.getRenderViewEntity(), mc.getRenderViewEntity().getEntityBoundingBox().addCoord(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance).expand(f1, f1, f1));
        double d2 = distance;

        for (Entity value : list) {

            if (value.canBeCollidedWith()) {
                float f2 = value.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = value.getEntityBoundingBox().expand(f2, f2, f2);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (0.0D < d2 || d2 == 0.0D) {
                        pointedEntity = value;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        pointedEntity = value;
                        vec33 = movingobjectposition.hitVec;
                        d2 = d3;
                    }
                }
            }
        }
        if (pointedEntity != null && (d2 < distance || omo == null))
        {
            omo = new MovingObjectPosition(pointedEntity, vec33);

            if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame)
            {
                mc.pointedEntity = pointedEntity;
            }
        }
        if (omo != null)
        {
            if (omo.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
            {
                if(omo.entityHit instanceof EntityLivingBase)
                {
                    return (EntityLivingBase)omo.entityHit;
                }
            }
        }
        return null;
    }
}
