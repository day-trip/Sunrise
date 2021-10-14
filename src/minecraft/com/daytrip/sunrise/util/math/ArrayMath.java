package com.daytrip.sunrise.util.math;

import java.util.Arrays;

public class ArrayMath {
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
}
