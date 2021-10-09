package com.daytrip.shared.math;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class ArrayGrid {
    private final Table<Integer, Integer, int[][]> grid = HashBasedTable.create();

    private ArrayGrid() {}

    public ArrayGrid add(int[][] array, int row, int column) {
        grid.put(column, row, array); // TODO: Fix order later (not fixed because @method{build()} doesn't match and @class{HackAutoFighter()} doesn't match either)
        return this;
    }

    public int[][] build() {
        int[][] finalArray;

        int[][] arrayTop;
        arrayTop = CommonMath.mergeAsColumns(grid.get(0, 0), grid.get(1, 0));
        arrayTop = CommonMath.mergeAsColumns(arrayTop, grid.get(2, 0));

        int[][] arrayMiddle;
        arrayMiddle = CommonMath.mergeAsColumns(grid.get(0, 1), grid.get(1, 1));
        arrayMiddle = CommonMath.mergeAsColumns(arrayMiddle, grid.get(2, 1));

        int[][] arrayBottom;
        arrayBottom = CommonMath.mergeAsColumns(grid.get(0, 2), grid.get(1, 2));
        arrayBottom = CommonMath.mergeAsColumns(arrayBottom, grid.get(2, 2));

        finalArray = CommonMath.mergeAsRows(arrayTop, arrayMiddle);
        finalArray = CommonMath.mergeAsRows(finalArray, arrayBottom);

        return finalArray;
    }

    public static ArrayGrid create() {
        return new ArrayGrid();
    }
}
