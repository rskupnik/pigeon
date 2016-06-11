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

import com.github.rskupnik.pigeon.commons.annotations.AnnotationsScanner;
import com.github.rskupnik.pigeon.commons.annotations.PacketBlueprint;
import com.github.rskupnik.pigeon.commons.annotations.PigeonPacket;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Message;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observable;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observer;
import com.github.rskupnik.pigeon.commons.util.DataTypeDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Connection extends Thread implements Runnable, Observable {

    private static final Logger log = LogManager.getLogger(Connection.class);

    private final UUID uuid;
    private Socket clientSocket;
    private DataInputStream clientInputStream;
    private DataOutputStream clientOutputStream;
    private List<Observer> observers = new ArrayList<Observer>();

    private boolean exit;
    private boolean ok = true;

    public Connection(UUID uuid, Socket clientSocket) {
        this.uuid = uuid;
        this.clientSocket = clientSocket;

        try {
            clientSocket.setSoTimeout(200);     // TODO: Make sure using socket timeouts is good and find out what value is optimal
            this.clientInputStream = new DataInputStream(clientSocket.getInputStream());
            this.clientOutputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            ok = false;
        }
    }

    @Override
    public void run() {
        try {
            while (!exit) {
                int packetId = -1;
                try {
                    packetId = clientInputStream.read();
                } catch (SocketTimeoutException e) {
                    continue;
                }

                if (packetId == -1) {
                    notify(Message.DISCONNECTED, uuid);
                    exit = true;
                    continue;
                }

                if (packetId == 0) {    // This ID is used to determine connection state when connecting
                    continue;
                }

                // Decode the packet
                log.trace("Received a packet, ID: " + packetId);
                Optional<Object> packet = PacketFactory.incomingPacket(packetId, clientInputStream);
                if (packet.isPresent()) {
                    Packet unwrappedPacket = null;
                    try {
                        unwrappedPacket = (Packet) packet.get();
                    } catch (ClassCastException e) {
                        log.error(String.format("PigeonPacket of ID %d has an invalid class: %s", packetId, e.getMessage()));
                        continue;
                    }

                    // Let the server handle the packet
                    notify(Message.RECEIVED_PACKET, unwrappedPacket);
                } else {
                    // TODO: Handle a packet parsing exception
                }
            }
        } catch (IOException e) {
            if (!(e instanceof SocketException))
                log.error(e.getMessage(), e);
            notify(Message.DISCONNECTED, uuid);
        } catch (PigeonException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                    clientSocket = null;
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void notify(Message message, Object payload) {
        log.trace("Sending a message of type " + message + " with payload: " + payload);
        observers.forEach(observer -> observer.update(this, message, payload));
    }

    public void attach(Observer observer) {
        observers.add(observer);
    }

    public void send(Packet packet) throws PigeonException {
        injectId(packet);

        try {
            packet.send(clientOutputStream);
            sendData(packet);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void disconnect() {
        exit = true;
        if (clientSocket != null) {
            try {
                clientSocket.close();
                clientSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void injectId(Packet packet) throws PigeonException {
        // Inject the id from annotation
        PigeonPacket annotation = packet.getClass().getAnnotation(PigeonPacket.class);
        if (annotation == null) {
            throw new PigeonException("Failed to send packet - it's not annotated with @PigeonPacket!");
        }
        packet.setId(annotation.id());
    }

    // TODO: Possible optimization - use blueprints?
    private void sendData(Packet packet) throws PigeonException {
        PacketBlueprint blueprint = AnnotationsScanner.getInstance().getPacketBlueprint(packet.getId());

        blueprint.getFields()
                .forEach(fieldBlueprint -> {
                    try {
                        if (fieldBlueprint.getGetter() == null)
                            throw new PigeonException(String.format("Cannot find getter for field [%s] in class [%s]", fieldBlueprint.getField().getName(), blueprint.getClass().getName()));

                        Object value = null;
                        try {
                            value = fieldBlueprint.getGetter().invoke(packet);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new PigeonException(e.getMessage());
                        }

                        if (value == null)
                            throw new PigeonException("Cannot retrieve value of field "+fieldBlueprint.getField().getName());

                        DataTypeDecoder.write(clientOutputStream, value, fieldBlueprint.getField());
                    } catch (PigeonException e) {
                        log.error(e.getMessage(), e);
                    }
                });
    }

    public boolean isOk() {
        return ok;
    }

    public String getHost() {
        if (clientSocket == null || clientSocket.getInetAddress() == null)
            return null;

        return clientSocket.getInetAddress().getHostAddress();
    }

    public UUID getUuid() {
        return uuid;
    }
}
