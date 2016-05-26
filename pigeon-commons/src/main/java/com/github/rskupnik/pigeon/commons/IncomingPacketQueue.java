package com.github.rskupnik.pigeon.commons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IncomingPacketQueue {

    private static final Logger log = LogManager.getLogger(IncomingPacketQueue.class);

    private ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<>();

    public void push(Packet packet) {
        packetQueue.add(packet);
        log.trace("Pushed a new packet to the queue, the size is now: "+packetQueue.size());
    }

    public Optional<Packet> pop() {
        Packet packet =  packetQueue.poll();
        return packet != null ? Optional.of(packet) : Optional.empty();
    }

    public List<Packet> popAll() {
        ArrayList<Packet> output = new ArrayList<>();
        synchronized (packetQueue) {
            Packet packet = null;
            while ((packet = packetQueue.poll()) != null) {
                output.add(packet);
            }
        }
        return output;
    }
}
