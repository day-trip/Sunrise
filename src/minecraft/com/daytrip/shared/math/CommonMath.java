package com.daytrip.shared.math;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

import java.util.Arrays;

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


    /**
     * Updates the values for following functions
     * @param attacker The attacker
     * @param vec3 The position of the target
     */
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

    public static float lerp(float point1, float point2, float alpha) {
        return point1 + alpha * (point2 - point1);
    }

    public static <T> T[][] combineArray(T[][] left, T[][] right) {
        Object[][] array = new Object[left[0].length + right[0].length][left.length];

        int x = 0;
        int y = 0;
        for(T[] objectL : left) {
            for(T object : objectL) {
                System.out.println(object);
                System.out.println(x);
                System.out.println(y);
                array[x][y] = object;
                y++;
            }
            x++;
        }

        int xx = 0;
        int yy = 0;
        for(T[] objectL : right) {
            for(T object : objectL) {
                array[xx + x][yy + y] = object;
                yy++;
            }
            xx++;
        }


        return (T[][]) array;
    }

    public static void main(String[] args) {
        int[][] array = {
                {1, 2, 3},
                {11, 22, 33},
                {111, 222, 333}
        };

        int[][] otherArray = {
                {4, 5, 6},
                {44, 55, 66},
                {444, 555, 666}
        };

        int[][] combinedArray = {
                {4, 5, 6, 1, 2, 3},
                {4, 5, 6, 1, 2, 3},
                {4, 5, 6, 1, 2, 3}
        };

        int[][] otherCombinedArray = {
                {30, 17, 139, 65, 335, 99},
                {50, 43, 57, 53, 423, 534},
                {90, 67, 78, 24, 99, 67}
        };

        int[][] finalCombinedArray = {
                {30, 17, 139, 65, 335, 99},
                {50, 43, 57, 53, 423, 534},
                {90, 67, 78, 24, 99, 67},
                {4, 5, 6, 1, 2, 3},
                {4, 5, 6, 1, 2, 3},
                {4, 5, 6, 1, 2, 3}
        };

        //Integer[][] didItWork = CommonMath.<Integer>combineArray(otherArray, array);
        printNeat(mergeAsRows(otherArray, array));
        printNeat(mergeAsColumns(otherArray, array));
    }

    public static void printNeat(int[][] dirty) {
        System.out.println("{");
        for(int[] aha : dirty) {
            System.out.println("    " + Arrays.toString(aha) + ",");
        }
        System.out.println("}");
        System.out.println("\n");
    }

    public static int[][] mergeAsRows(int[][] a, int[][] b) {
        int rows = a.length;
        int columns = a[0].length;

        int[][] merged = new int[2 * rows][columns];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(a[i], 0, merged[i], 0, columns);
            System.arraycopy(b[i], 0, merged[rows + i], 0, columns);
        }

        return merged;
    }

    public static int[][] mergeAsColumns(int[][] a, int[][] b) {
        int rows = a.length;
        int columns = a[0].length;

        int[][] merged = new int[rows][2 * columns];

        for (int i = 0; i < a.length; i++) {
            System.arraycopy(a[i], 0, merged[i], 0, columns);
            System.arraycopy(b[i], 0, merged[i], rows, columns);
        }

        return merged;
    }

    public static long ticksToMillis(int ticks) {
        return (ticks / 20L) * 1000L;
    }

    public static long millisToTicks(long millis) {
        return (millis / 1000L) * 20L;
    }
}
