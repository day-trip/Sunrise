package com.daytrip.sunrise.event.impl.input;

import com.daytrip.sunrise.event.Event;
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
