package com.daytrip.sunrise.event.impl;

import com.daytrip.sunrise.event.Event;

public class EventClickMouse extends Event {
    private int button;

    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }
}
