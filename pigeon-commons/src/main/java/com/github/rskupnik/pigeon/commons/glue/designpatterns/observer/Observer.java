package com.github.rskupnik.pigeon.commons.glue.designpatterns.observer;

public interface Observer {
    void update(Observable observable, Message message, Object payload);
}
