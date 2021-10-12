package com.daytrip.sunrise.util;

public class PremiumManager {
    private static boolean isPremium;

    public static boolean isIsPremium() {
        return isPremium;
    }

    public static void setIsPremium(boolean isPremium) {
        PremiumManager.isPremium = isPremium;
    }
}
