package com.daytrip.sunrise.event;

public interface EventListener {
    void onEvent(Event event) throws Exception;

    default boolean ignore(Event event) {
        return false;
    }
}
