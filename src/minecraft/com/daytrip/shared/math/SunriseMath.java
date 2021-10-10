package com.daytrip.shared.math;

public class SunriseMath {
    //public static native void yawToFacePosition(float x, float y, float z, float targetX, float targetY, float targetZ);

    //public static native void pitchToFacePosition(float x, float y, float z, float targetX, float targetY, float targetZ);

    public static native int[] utilizeArray( int[] arr);

    public static void main(String[] args) {
        System.loadLibrary("mylib");
    }
}
