package com.daytrip.sunrise.util;

import com.daytrip.sunrise.hack.task.ActionCallable;

public class ThreadTesting {
    public static void callbackCode() {
        System.out.println(Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        Thread.currentThread().setName("Main Thread");
        Thread thread = new Thread(new ARunnableWithACallback(ThreadTesting::callbackCode));
        thread.setName("Testing Thread");
        thread.start();
    }

    static class ARunnableWithACallback implements Runnable {
        private final ActionCallable callback;

        public ARunnableWithACallback(ActionCallable callable) {
            callback = callable;
        }

        @Override
        public void run() {
            callback.call();
        }
    }
}
