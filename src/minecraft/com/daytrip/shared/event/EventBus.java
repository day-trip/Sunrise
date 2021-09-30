package com.daytrip.shared.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private static final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    public static void registerListener(EventListener listener) {
        listeners.add(listener);
    }

    public static void post(Event event) throws Exception {
        for(EventListener listener : listeners) {
            if(listener.ignore(event) || event.isExcluded(listener.getListenerName())) {
                continue;
            }
            listener.onEvent(event);
            if(event.isCancelled()) {
                break;
            }
        }
    }
}
