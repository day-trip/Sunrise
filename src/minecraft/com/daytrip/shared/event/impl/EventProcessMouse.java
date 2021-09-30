package com.daytrip.shared.event.impl;

import com.daytrip.shared.event.Event;

public class EventProcessMouse extends Event {
    private int invert;
    private float dX;
    private float dY;
    private float sensitivity;

    public int getInvert() {
        return invert;
    }

    public void setInvert(int invert) {
        this.invert = invert;
    }

    public float getdX() {
        return dX;
    }

    public void setdX(float dX) {
        this.dX = dX;
    }

    public float getdY() {
        return dY;
    }

    public void setdY(float dY) {
        this.dY = dY;
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }
}
