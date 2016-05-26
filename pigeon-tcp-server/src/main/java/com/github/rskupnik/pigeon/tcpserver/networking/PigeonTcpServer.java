package com.github.rskupnik.pigeon.tcpserver.networking;

import com.github.rskupnik.pigeon.commons.*;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonServerException;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Message;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observable;
import com.github.rskupnik.pigeon.tcpserver.init.PigeonTcpServerBuilder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public final class PigeonTcpServer extends Thread implements PigeonServer {

    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(PigeonTcpServer.class);

    private final int port;
    private final int receiverThreadsNumber;

    private final ServerSocket serverSocket;
    private final Map<UUID, Connection> connections = new HashMap<UUID, Connection>();
    private final ExecutorService executorService;
    private final IncomingPacketHandleMode incomingPacketHandleMode;
    private final IncomingPacketQueue incomingPacketQueue;
    private final PacketHandler packetHandler;
    private final ServerCallbackHandler serverCallbackHandler;

    private boolean exit;

    public PigeonTcpServer(PigeonTcpServerBuilder builder) throws PigeonServerException {
        this.port = builder.getPort();
        this.receiverThreadsNumber = builder.getReceiverThreadsNumber();
        this.incomingPacketHandleMode = builder.getIncomingPacketHandleMode();
        this.serverCallbackHandler = builder.getServerCallbackHandler();

        if (incomingPacketHandleMode == IncomingPacketHandleMode.QUEUE) {
            packetHandler = null;
            incomingPacketQueue = new IncomingPacketQueue();
        } else {    // HANDLER mode is the default mode
            packetHandler = builder.getPacketHandler();
            incomingPacketQueue = null;
        }

        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new PigeonServerException(e.getMessage(), e);
        }

        if (receiverThreadsNumber <= 0) {
            executorService = Executors.newCachedThreadPool();
        } else {
            executorService = Executors.newFixedThreadPool(receiverThreadsNumber);
        }
    }

    @Override
    public void run() {
        if (serverCallbackHandler != null)
            serverCallbackHandler.onStarted();

        try {
            while (!exit) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    if (exit)
                        break;
                }

                if (clientSocket == null)
                    continue;

                // Determine the number of free threads in case of THREADPOOLED mode and decline connection if there are no threads available
                if (fixedNumberOfThreads()) {
                    ThreadPoolExecutor tpc = (ThreadPoolExecutor) executorService;
                    if (tpc.getActiveCount() >= receiverThreadsNumber) {
                        log.info("Connection from IP " + clientSocket.getInetAddress().getHostAddress() + " was declined due to not enough threads to handle it.");
                        continue;
                    }
                }

                UUID uuid = UUID.randomUUID();
                Connection connection = new Connection(uuid, clientSocket);
                if (connection.isOk()) {    // Connection is not considered ok when there is an IOException in the constructor
                    connection.attach(this);
                    connections.put(uuid, connection);
                    log.info(String.format("Accepted a new connection [%s] from IP: %s", uuid, clientSocket.getInetAddress().getHostAddress()));

                    executorService.execute(connection);

                    if (serverCallbackHandler != null)
                        serverCallbackHandler.onNewConnection(connection);
                }
            }
        } catch (IOException e) {
            if (!(e instanceof SocketException))
                log.error(e.getMessage(), e);
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void update(Observable observable, Message message, Object payload) {
        switch (message) {
            case DISCONNECTED:
                UUID connectionUuid = (UUID) payload;
                Connection connection = connections.get(connectionUuid);
                connections.remove(connectionUuid);
                log.debug("Removed connection [" + connectionUuid + "] from [" + connection.getHost() + "]");
                log.debug("Remaining connections: " + connections.entrySet().size());
                connection.disconnect();
                break;
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
        }
    }

    @Override
    public void send(Packet packet, Connection connection) {
        connection.send(packet);
    }

    @Override
    public void send(Packet packet, List<Connection> connections) {
        for (Connection connection : connections) {
            send(packet, connection);
        }
    }

    public IncomingPacketQueue getIncomingPacketQueue() {
        return incomingPacketQueue;
    }

    private boolean fixedNumberOfThreads() {
        return receiverThreadsNumber > 0;
    }
}
