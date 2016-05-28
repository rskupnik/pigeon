package com.github.rskupnik.pigeon.tcpclient;

import com.github.rskupnik.pigeon.commons.*;
import com.github.rskupnik.pigeon.commons.callback.ClientCallbackHandler;
import com.github.rskupnik.pigeon.commons.client.PigeonClient;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Message;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class PigeonTcpClient implements PigeonClient {

    private static final Logger log = LogManager.getLogger(PigeonTcpClient.class);

    private final String host;
    private final int port;
    private final Socket clientSocket;
    private final Connection connection;
    private final IncomingPacketHandleMode incomingPacketHandleMode;
    private final IncomingPacketQueue incomingPacketQueue;
    private final PacketHandler packetHandler;
    private final ClientCallbackHandler clientCallbackHandler;

    public PigeonTcpClient(PigeonTcpClientBuilder builder) throws PigeonException {
        this.host = builder.getHost();
        this.port = builder.getPort();
        this.incomingPacketHandleMode = builder.getIncomingPacketHandleMode();
        this.packetHandler = builder.getPacketHandler();
        this.clientCallbackHandler = builder.getClientCallbackHandler();

        if (incomingPacketHandleMode == IncomingPacketHandleMode.QUEUE) {
            incomingPacketQueue = new IncomingPacketQueue();
        } else {
            incomingPacketQueue = null;
        }

        try {
            clientSocket = new Socket(host, port);
        } catch (IOException e) {
            throw new PigeonException(e.getMessage(), e);
        }

        try {
            int read = clientSocket.getInputStream().read();
            if (read == 0) {
                throw new PigeonException("Server refused connection");
            }
        } catch (IOException e) {
            throw new PigeonException("Server refused connection");
        }

        this.connection = new Connection(UUID.randomUUID(), clientSocket);
        if (connection.isOk()) {
            connection.attach(this);
            connection.start();
        }

        if (clientCallbackHandler != null)
            clientCallbackHandler.onConnected();
    }

    public void update(Observable observable, Message message, Object payload) {
        switch (message) {
            case RECEIVED_PACKET:
                Packet packet = (Packet) payload;
                switch (incomingPacketHandleMode) {
                    case QUEUE:
                        incomingPacketQueue.push(packet);
                        break;
                    default:
                    case HANDLER:
                        packetHandler.handle(packet);
                        break;
                }
                break;
            case DISCONNECTED:
                break;
        }
    }

    public void send(Packet packet) throws PigeonException {
        connection.send(packet);
    }

    public void disconnect() {
        try {
            connection.disconnect();
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public IncomingPacketQueue getIncomingPacketQueue() {
        return incomingPacketQueue;
    }
}
