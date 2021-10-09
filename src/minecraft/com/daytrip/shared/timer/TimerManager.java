package com.daytrip.shared.timer;

import com.daytrip.shared.event.Event;
import com.daytrip.shared.event.EventListener;
import com.daytrip.shared.event.impl.EventTick;

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
