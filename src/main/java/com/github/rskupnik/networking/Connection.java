package com.github.rskupnik.networking;

import com.github.rskupnik.glue.designpatterns.observer.Message;
import com.github.rskupnik.glue.designpatterns.observer.Observable;
import com.github.rskupnik.glue.designpatterns.observer.Observer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

final class Connection implements Runnable, Observable {

    private static final Logger log = LogManager.getLogger(Connection.class);

    private final UUID uuid;
    private final ServerSocket serverSocket;
    private Socket clientSocket;
    private DataInputStream clientInputStream;
    private DataOutputStream clientOutputStream;
    private List<Observer> observers = new ArrayList<Observer>();

    private boolean exit;
    private boolean ok = true;

    protected Connection(UUID uuid, ServerSocket serverSocket, Socket clientSocket) {
        this.uuid = uuid;
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        try {
            clientSocket.setSoTimeout(200);
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

                log.trace("Received a packet, ID: " + packetId);
                Optional<Object> packet = PacketFactory.incomingPacket(packetId, clientInputStream);
                log.info("Packet "+(packet.isPresent() ? "is present" : "is not present"));
                //PacketHandler.handle(packetId, clientInputStream, uuid);
            }
        } catch (IOException e) {
            if (!(e instanceof SocketException))
                log.error(e.getMessage(), e);
            notify(Message.DISCONNECTED, uuid);
        } finally {
            try {
                clientSocket.close();
                clientSocket = null;
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

    void send(Packet packet) {
        try {
            packet.send(clientOutputStream);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    void disconnect() {
        exit = true;
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isOk() {
        return ok;
    }

    public String getHost() {
        return clientSocket.getInetAddress().getHostAddress();
    }

    public UUID getUuid() {
        return uuid;
    }
}
