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

    public static class RandomInteger {
        private final int min;
        private final int max;

        private int randomNumber;

        /**
         * Constructor for this class
         * @param min the minimum possible number
         * @param max the maximum possible number
         * @param genOnCreate whether or not to generate a random number on initialization
         */
        public RandomInteger(int min, int max, boolean genOnCreate) {
            this.min = min;
            this.max = max;
            if(genOnCreate) {
                generate();
            }
        }

        public void generate() {
            randomNumber = min + random.nextInt(max - min);
        }

        public int get() {
            return randomNumber;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }
}
