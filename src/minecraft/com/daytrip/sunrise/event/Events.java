package com.daytrip.sunrise.event;

import com.daytrip.sunrise.event.impl.*;
import com.daytrip.sunrise.event.impl.init.EventGameInit;
import com.daytrip.sunrise.event.impl.init.EventGamePostInit;
import com.daytrip.sunrise.event.impl.init.EventGamePreInit;
import com.daytrip.sunrise.event.impl.input.EventClickMouse;
import com.daytrip.sunrise.event.impl.input.EventKeypress;
import com.daytrip.sunrise.event.impl.input.EventProcessMouse;
import com.daytrip.sunrise.event.impl.input.EventUpdateMovementInput;

public enum Events {
    LISTENER_REGISTER(0, EventRegisterListeners.class),

    MINECRAFT_PRE_INIT(1, EventGamePreInit.class),
    MINECRAFT_INIT(2, EventGameInit.class),
    MINECRAFT_POST_INIT(3, EventGamePostInit.class),

    TICK(4, EventTick.class),
    RENDER_HUD(5, EventRenderHud.class),
    RENDER_BRIGHTNESS_BUFFER(6, EventRenderBrightnessBuffer.class),
    PLAYER_DAMAGED(7, EventPlayerDamaged.class),
    ENTITY_DEATH(8, EventEntityDeath.class),
    CHUNK_LOAD(9, EventChunkLoad.class),

    CLICK_MOUSE(10, EventClickMouse.class),
    KEYPRESS(11, EventKeypress.class),
    PROCESS_MOUSE(12, EventProcessMouse.class),
    UPDATE_MOVEMENT_INPUT(13, EventUpdateMovementInput.class),

    ;
    private final int eventId;
    private final Class<? extends Event> eventsClass;

    Events(int eventId, Class<? extends Event> eventsClass) {
        this.eventId = eventId;
        this.eventsClass = eventsClass;
    }

    public Class<? extends Event> getEventsClass() {
        return eventsClass;
    }

    public int getEventId() {
        return eventId;
    }

    public static Events fromId(int id) {
        for(Events events : values()) {
            if(events.eventId == id) return events;
        }
        return null;
    }
}
