package com.github.rskupnik.pigeon.commons;

import com.github.rskupnik.pigeon.commons.annotations.PacketDataField;
import com.github.rskupnik.pigeon.commons.annotations.PigeonPacket;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Message;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observable;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        //observers.forEach(observer -> observer.update(this, message, payload));
        for (Observer observer : observers) {
            observer.update(this, message, payload);
        }
    }

    public void attach(Observer observer) {
        observers.add(observer);
    }

    public void send(Packet packet) throws PigeonException {
        inject(packet);

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

    private void inject(Packet packet) throws PigeonException {
        // Inject the id from annotation
        PigeonPacket annotation = packet.getClass().getAnnotation(PigeonPacket.class);
        if (annotation == null) {
            throw new PigeonException("Failed to send packet - it's not annotated with @PigeonPacket!");
        }
        packet.setId(annotation.id());
    }

    // TODO: Make sure this is optimized
    // TODO: Extract duplicate code from here and PacketFactory/AnnotationsScanner into a separate class
    private void sendData(Packet packet) throws PigeonException {
        for (Field field : packet.getClass().getDeclaredFields()) {
            if (field.getAnnotation(PacketDataField.class) == null)
                continue;

            StringBuilder name = new StringBuilder();
            name.append(field.getName().substring(0, 1).toUpperCase());
            name.append(field.getName().substring(1));
            Method[] methods = packet.getClass().getDeclaredMethods();
            Method chosenMethod = null;
            inner: for (Method method : methods) {
                if (method.getName().equals("get"+name)) {
                    chosenMethod = method;
                    break inner;
                }
            }
            chosenMethod.setAccessible(true);
            Object value = null;
            try {
                value = chosenMethod.invoke(packet);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new PigeonException(e.getMessage());
            }
            if (value == null) {
                throw new PigeonException("Cannot retrieve value of field "+field.getName());
            }

            try {
                if (field.getType().equals(Integer.TYPE) || field.getType().isInstance(Integer.class)) {
                    clientOutputStream.writeInt((int) value);
                } else if (field.getType().isInstance(String.class)) {
                    clientOutputStream.writeUTF((String) value);
                } else {
                    throw new PigeonException("Cannot handle this data type: "+field.getType());
                }
            } catch (IOException e) {
                throw new PigeonException(e.getMessage());
            }
        }
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
