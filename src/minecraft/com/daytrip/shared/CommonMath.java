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

    public static int round(double a) {
        if(a < 0) {
            return (int) Math.ceil(a + 0.5d);
        }
        return (int) Math.round(a);
    }
}
