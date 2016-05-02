package com.github.rskupnik.glue.designpatterns.observer;

public interface Observable {
    void notify(Message message, Object payload);
    void attach(Observer observer);
}
