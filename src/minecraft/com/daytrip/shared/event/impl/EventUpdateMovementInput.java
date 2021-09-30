package com.daytrip.shared.event.impl;

import com.daytrip.shared.event.Event;
import net.minecraft.util.MovementInput;

public class EventUpdateMovementInput extends Event {
    private MovementInput movementInput;

    public MovementInput getMovementInput() {
        return movementInput;
    }

    public void setMovementInput(MovementInput movementInput) {
        this.movementInput = movementInput;
    }
}
