package com.daytrip.sunrise.util.math;

import java.util.Random;

/**
 * Code related to working with random things
 */
public class RandomMath {
    public static final Random random = new Random();

    /**
     * Gets a random boolean
     * @param chance the decimal change of true being returned
     * @return the random boolean
     */
    public static boolean getRandomBoolean(float chance) {
        return random.nextFloat() < chance;
    }
}
