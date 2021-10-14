package com.daytrip.sunrise.util.thread;

public interface ReturnCallback<T> {
    void complete(T value);
}
