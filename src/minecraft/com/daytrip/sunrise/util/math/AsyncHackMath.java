package com.daytrip.sunrise.util.math;

import net.minecraft.util.Vec3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * For doing asynchronous math (mostly used by hacks)
 */
public class AsyncHackMath {
    private final ExecutorService yawExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService pitchExecutor = Executors.newSingleThreadExecutor();

    public float yawToFaceEntity(Vec3 attacker, Vec3 target, float yOffset) {
        Future<Float> future = yawExecutor.submit(() -> {
            double dirx = attacker.xCoord - target.xCoord;
            double diry = (attacker.yCoord - target.yCoord) + yOffset;
            double dirz = attacker.zCoord - target.zCoord;
            double len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);

            dirx /= len;
            dirz /= len;

            double yaw = Math.atan2(dirz, dirx);

            yaw = yaw * 180.0 / Math.PI;

            yaw += 90f;

            return (float) yaw;
        });
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public float pitchToFaceEntity(Vec3 attacker, Vec3 target, float yOffset) {
        Future<Float> future = pitchExecutor.submit(() -> {
            double dirx = attacker.xCoord - target.xCoord;
            double diry = (attacker.yCoord - target.yCoord) + yOffset;
            double dirz = attacker.zCoord - target.zCoord;
            double len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);
            diry /= len;
            double pitch = Math.asin(diry);
            pitch = pitch * 180.0 / Math.PI;
            return (float) pitch;
        });
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
