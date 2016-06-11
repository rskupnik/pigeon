/*
    Copyright 2016 Radosław Skupnik

    This file is part of pigeon-commons.

    Pigeon-commons is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Pigeon-commons is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Pigeon-commons; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

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
