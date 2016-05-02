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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class Connection extends Thread implements Observable {

    private static final Logger log = LogManager.getLogger(Connection.class);

    private final UUID uuid;
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private DataInputStream clientInputStream;
    private DataOutputStream clientOutputStream;
    private List<Observer> observers = new ArrayList<Observer>();

    private boolean exit;
    private boolean ok = true;

    protected Connection(UUID uuid, ServerSocket serverSocket, Socket clientSocket) {
        this.setName("Connection-" + uuid.toString().substring(0, 8));
        this.uuid = uuid;
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        try {
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
                int packetId = clientInputStream.read();

                if (packetId == -1) {
                    notify(Message.DISCONNECTED, uuid);
                    exit = true;
                    continue;
                }

                log.trace("Received a packet, ID: " + packetId);
                //PacketHandler.handle(packetId, clientInputStream, uuid);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            notify(Message.DISCONNECTED, uuid);
        } finally {
            try {
                clientSocket.close();
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
    }

    public boolean isOk() {
        return ok;
    }

    public String getHost() {
        return clientSocket.getInetAddress().getHostAddress();
    }
}
