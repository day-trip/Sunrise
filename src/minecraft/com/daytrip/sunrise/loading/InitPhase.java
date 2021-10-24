package com.daytrip.sunrise.loading;

import java.util.Arrays;

public class InitPhase {
    private static Phase phase = Phase.INIT;

    public static void next() {
        int i = Arrays.binarySearch(Phase.values(), phase);
        if(i < Phase.values().length) {
            phase = Phase.values()[i + 1];
        }
    }

    public static Phase get() {
        return phase;
    }

    public enum Phase {
        INIT,
        LOOPING,
        END
    }
}
