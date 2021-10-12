package com.daytrip.sunrise.util.timer;

import com.daytrip.sunrise.event.Event;
import com.daytrip.sunrise.event.EventListener;
import com.daytrip.sunrise.event.impl.EventTick;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TimerManager implements EventListener {
    private static final List<TickTimer> tickTimers = new CopyOnWriteArrayList<>();

    public static void registerTimer(TickTimer tickTimer) {
        tickTimers.add(tickTimer);
    }

    public static void destroy() {
        for(TickTimer tickTimer : tickTimers) {
            tickTimer.stop();
            finished(tickTimer);
        }
    }

    public static void finished(TickTimer tickTimer) {
        tickTimers.remove(tickTimer);
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventTick) {
            for(TickTimer tickTimer : tickTimers) {
                tickTimer.update();
            }
        }
    }
}
