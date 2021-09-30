package com.daytrip.shared;

public class MemoryCache {
    public static long MAXIMUM_MEMORY;

    public static void generateCache() {
        MAXIMUM_MEMORY = Runtime.getRuntime().maxMemory();
    }

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }
}
