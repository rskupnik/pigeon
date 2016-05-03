package com.github.rskupnik.networking;

import com.github.rskupnik.Defaults;
import com.github.rskupnik.exceptions.PigeonServerException;
import com.github.rskupnik.glue.designpatterns.observer.Message;
import com.github.rskupnik.glue.designpatterns.observer.Observable;
import com.github.rskupnik.glue.designpatterns.observer.Observer;
import com.github.rskupnik.parrot.Parrot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Server extends Thread implements Observer {

    private static final Logger log = LogManager.getLogger(Server.class);

    public enum ReceiverMode {
        THREADPOOLED,
        MULTITHREADED
    }

    private final int port;
    private final ReceiverMode receiverMode;
    private final ServerSocket serverSocket;
    private final Map<UUID, Connection> connections = new HashMap<UUID, Connection>();

    private boolean exit;

    public Server(int port, String receiverMode) throws PigeonServerException {
        this.port = port;

        try {
            this.serverSocket = new ServerSocket(port);
            this.receiverMode = ReceiverMode.valueOf(receiverMode.toUpperCase());
        } catch (IOException | IllegalArgumentException e) {
            throw new PigeonServerException(e.getMessage(), e);
        }

    }

    @Override
    public void run() {
        try {
            while (!exit) {
                Socket clientSocket = serverSocket.accept();

                UUID uuid = UUID.randomUUID();
                Connection connection = new Connection(uuid, serverSocket, clientSocket);
                if (connection.isOk()) {    // Connection is not considered ok when there is an IOException in the constructor
                    connection.attach(this);
                    connections.put(uuid, connection);
                    log.info(String.format("Accepted a new connection [%s] from IP: %s", uuid, clientSocket.getInetAddress().getHostAddress()));
                    connection.start();
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void halt() {
        exit = true;
    }

    public void update(Observable observable, Message message, Object payload) {
        switch (message) {
            case DISCONNECTED:
                UUID connectionUuid = (UUID) payload;
                Connection connection = connections.get(connectionUuid);
                connections.remove(connectionUuid);
                log.debug("Removed connection ["+connectionUuid+"] from ["+connection.getHost()+"]");
                log.debug("Remaining connections: "+connections.entrySet().size());
                connection.disconnect();
                break;
        }
    }

    public void send(UUID connectionId, Packet packet) {
        Connection connection = connections.get(connectionId);

        if (connection == null) {
            log.debug("Connection ["+connectionId+"] doesn't exist, cannot send packet");
            return;
        }

        connection.send(packet);
    }

    public ReceiverMode getReceiverMode() {
        return receiverMode;
    }
}
