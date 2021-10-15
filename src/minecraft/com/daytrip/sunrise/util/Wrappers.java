package com.daytrip.sunrise.util;

import com.daytrip.sunrise.event.Event;

import java.util.concurrent.Callable;

public class Wrappers {
    public static <T> T call(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean post(Event event) {
        try {
            event.post();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean notNull(Boolean b) {
        if(b == null) {
            return false;
        }
        return b;
    }
}
