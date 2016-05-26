package com.github.rskupnik.pigeon.commons;

import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observer;

import java.util.List;

public interface PigeonServer extends Runnable, Observer {
    void send(Packet packet, Connection connection);
    void send(Packet packet, List<Connection> connections);
}
