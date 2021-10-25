package com.daytrip.sunrise.util.math.interpolation;

/**
 * For performing math related to interpolations
 */
public class InterpolationMath {
    public static final float PI = 3.1415927f;
    public static final float PI2 = PI * 2;

    public static float angleLinearInterpolate(float fromRadians, float toRadians, float progress) {
        float delta = ((toRadians - fromRadians + PI2 + PI) % PI2) - PI;
        return (fromRadians + delta * progress + PI2) % PI2;
    }

    public static float linearInterpolate(float point1, float point2, float alpha) {
        return point1 + alpha * (point2 - point1);
    }
}
