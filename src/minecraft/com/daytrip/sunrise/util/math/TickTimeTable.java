package com.daytrip.sunrise.util.math;

public class TickTimeTable {
    public static long ticksToMillis(int ticks) {
        return (ticks / 20L) * 1000L;
    }

    public static long millisToTicks(long millis) {
        return (millis / 1000L) * 20L;
    }
}
