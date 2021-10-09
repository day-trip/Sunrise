package com.daytrip.shared.math;

import java.util.Objects;

public class Vec2 implements Cloneable {
    private int x;
    private int z;

    public Vec2(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Vec2) {
            return ((Vec2) o).getX() == x && ((Vec2) o).getZ() == z;
        }
        return false;
    }

    @Override
    public String toString() {
        return "com.daytrip.shared.math.Vec2{ x=" + x + ", z=" + z + " };";
    }

    @Override
    public Vec2 clone() {
        return new Vec2(x, z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public Vec2 setX(int x) {
        this.x = x;
        return this;
    }

    public Vec2 setZ(int z) {
        this.z = z;
        return this;
    }

    public Vec2 add(int x, int z) {
        this.x += x;
        this.z += z;
        return this;
    }

    public Vec2 subtract(int x, int z) {
        this.x -= x;
        this.z -= z;
        return this;
    }

    public Vec2 multiply(int x, int z) {
        this.x *= x;
        this.z *= z;
        return this;
    }

    public Vec2 divide(int x, int z) {
        this.x /= x;
        this.z /= z;
        return this;
    }
}
