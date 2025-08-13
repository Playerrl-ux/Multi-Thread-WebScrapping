package com.web;

public interface Pipe<T> {

    void put(T t) throws InterruptedException;

    default void putPersitent(T t) {
        while (true) {
            try {
                put(t);
                return;
            } catch (InterruptedException ignored) {
            }
        }
    }
}
