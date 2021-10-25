package com.daytrip.sunrise.util.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

public class InterpolationTable {
    // X = TIME, Y = AMOUNT
    private final TreeMap<Integer, Float> data = new TreeMap<>();

    public void addData(int time, float value) {
        data.put(time, value);
    }

    public float get(int ticks) {
        if(data.containsKey(ticks)) return data.get(ticks);
        Map.Entry<Integer, Float> low = data.floorEntry(ticks);
        Map.Entry<Integer, Float> high = data.ceilingEntry(ticks);
        return InterpolationMath.linearInterpolate(low.getValue(), high.getValue(), (float) ticks / (high.getKey() - low.getKey()));
    }

    @Override
    public String toString() {
        return "InterpolationTable{" +
                "data=" + data +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InterpolationTable)) return false;

        InterpolationTable that = (InterpolationTable) o;

        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    public static void main(String[] args) {
        InterpolationTable table = new InterpolationTable();
        table.addData(0, 0);
        table.addData(10, 20);
        table.addData(50, 30);
        System.out.println(table);
        System.out.println(table.get(0));
        System.out.println(table.get(5));
        System.out.println(table.get(10));
        System.out.println(table.get(30));
        System.out.println(table.get(50));
    }
}
