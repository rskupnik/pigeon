package com.github.rskupnik.pigeon.commons.glue.designpatterns.observer;

public interface Observable {
    void notify(Message message, Object payload);
    void attach(Observer observer);
}
