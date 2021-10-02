package com.daytrip.shared;

public class LoadingManager {
    private static int phase;
    private static int total;

    public static void setTotal(int total) {
        LoadingManager.total = total;
    }

    public static void next() {
        phase++;
    }

    public static float getPercentage() {
        return (float) phase / total;
    }
}
