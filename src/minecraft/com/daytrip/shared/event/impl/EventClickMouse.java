package com.daytrip.shared.event.impl;

import com.daytrip.shared.event.Event;

public class EventClickMouse extends Event {
    private int button;

    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }
}
