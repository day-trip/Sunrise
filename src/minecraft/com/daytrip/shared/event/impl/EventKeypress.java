package com.daytrip.shared.event.impl;

import com.daytrip.shared.event.Event;

public class EventKeypress extends Event {
    private int key;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
