package com.github.rskupnik.pigeon.tcpserver.networking;

import com.github.rskupnik.pigeon.commons.PigeonServer;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonServerException;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Message;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observable;
import com.github.rskupnik.pigeon.commons.glue.designpatterns.observer.Observer;
import com.github.rskupnik.pigeon.tcpserver.init.PigeonTcpServerBuilder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public final class PigeonTcpServer extends Thread implements PigeonServer, Observer {

    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(PigeonTcpServer.class);

    public enum IncomingPacketHandleMode {
        QUEUE,
        HANDLER
    }

    private final int port;
    private final int receiverThreadsNumber;

    private final ServerSocket serverSocket;
    private final Map<UUID, Connection> connections = new HashMap<UUID, Connection>();
    private final ExecutorService executorService;
    private final IncomingPacketHandleMode incomingPacketHandleMode;
    private final IncomingPacketQueue incomingPacketQueue;

    private boolean exit;

    public PigeonTcpServer(PigeonTcpServerBuilder builder) throws PigeonServerException {
        this.port = builder.getPort();
        this.receiverThreadsNumber = builder.getReceiverThreadsNumber();
        this.incomingPacketHandleMode = builder.getIncomingPacketHandleMode();

        if (incomingPacketHandleMode == IncomingPacketHandleMode.QUEUE) {
            incomingPacketQueue = new IncomingPacketQueue();
        } else {
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
                        log.info("Connection from IP "+clientSocket.getInetAddress().getHostAddress()+" was declined due to not enough threads to handle it.");
                        continue;
                    }
                }

                UUID uuid = UUID.randomUUID();
                Connection connection = new Connection(uuid, serverSocket, clientSocket, incomingPacketHandleMode, incomingPacketQueue);
                if (connection.isOk()) {    // Connection is not considered ok when there is an IOException in the constructor
                    connection.attach(this);
                    connections.put(uuid, connection);
                    log.info(String.format("Accepted a new connection [%s] from IP: %s", uuid, clientSocket.getInetAddress().getHostAddress()));

                    executorService.execute(connection);
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

    private boolean fixedNumberOfThreads() {
        return receiverThreadsNumber > 0;
    }
}
