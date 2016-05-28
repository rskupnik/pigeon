package com.github.rskupnik.pigeon.commons.server;

import com.github.rskupnik.pigeon.commons.Connection;
import com.github.rskupnik.pigeon.commons.IncomingPacketQueue;
import com.github.rskupnik.pigeon.commons.Packet;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observer;

import java.util.List;

public interface PigeonServer extends Runnable, Observer {
    void send(Packet packet, Connection connection) throws PigeonException;
    void send(Packet packet, List<Connection> connections) throws PigeonException;
    IncomingPacketQueue getIncomingPacketQueue();
}
