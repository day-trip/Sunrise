package com.daytrip.sunrise.event;

public class EventExceptionWrapper {
    public static void post(Event event) {
        try {
            event.post();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
