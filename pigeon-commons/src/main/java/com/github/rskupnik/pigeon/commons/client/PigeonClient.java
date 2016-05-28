package com.github.rskupnik.pigeon.commons.client;

import com.github.rskupnik.pigeon.commons.IncomingPacketQueue;
import com.github.rskupnik.pigeon.commons.Packet;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observer;

public interface PigeonClient extends Observer {
    void send(Packet packet) throws PigeonException;
    IncomingPacketQueue getIncomingPacketQueue();
}
