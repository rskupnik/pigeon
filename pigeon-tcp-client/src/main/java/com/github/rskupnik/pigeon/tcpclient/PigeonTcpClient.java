package com.github.rskupnik.pigeon.tcpclient;

import com.github.rskupnik.pigeon.commons.*;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Message;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observable;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class PigeonTcpClient implements PigeonClient {

    private final String host;
    private final int port;
    private final Socket clientSocket;
    private final Connection connection;
    private final IncomingPacketHandleMode incomingPacketHandleMode;
    private final IncomingPacketQueue incomingPacketQueue;
    private final PacketHandler packetHandler;

    public PigeonTcpClient(PigeonTcpClientBuilder builder) throws PigeonException {
        this.host = builder.getHost();
        this.port = builder.getPort();
        this.incomingPacketHandleMode = builder.getIncomingPacketHandleMode();
        this.packetHandler = builder.getPacketHandler();

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

        this.connection = new Connection(UUID.randomUUID(), clientSocket);
        if (connection.isOk()) {
            connection.attach(this);
            connection.start();
        }
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
}
