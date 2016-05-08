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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public final class Server extends Thread implements Observer {

    private static final Logger log = LogManager.getLogger(Server.class);

    public enum ReceiverMode {
        THREADPOOLED,
        MULTITHREADED
    }

    private final int port;
    private final ReceiverMode receiverMode;
    private final int receiverThreads;

    private final ServerSocket serverSocket;
    private final Map<UUID, Connection> connections = new HashMap<UUID, Connection>();
    private final ExecutorService executorService;

    private boolean exit;

    public Server(int port, Server.ReceiverMode receiverMode, int receiverThreads) throws PigeonServerException {
        this.port = port;
        this.receiverThreads = receiverThreads;
        this.receiverMode = receiverMode;

        try {
            this.serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(200);
        } catch (IOException e) {
            throw new PigeonServerException(e.getMessage(), e);
        }

        if (this.receiverMode == ReceiverMode.THREADPOOLED) {
            executorService = Executors.newFixedThreadPool(this.receiverThreads);
        } else
            executorService = null;
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
                if (receiverMode == ReceiverMode.THREADPOOLED) {
                    ThreadPoolExecutor tpc = (ThreadPoolExecutor) executorService;
                    if (tpc.getActiveCount() >= receiverThreads) {
                        log.info("Connection from IP "+clientSocket.getInetAddress().getHostAddress()+" was declined due to not enough threads to handle it.");
                        continue;
                    }
                }

                UUID uuid = UUID.randomUUID();
                Connection connection = new Connection(uuid, serverSocket, clientSocket);
                if (connection.isOk()) {    // Connection is not considered ok when there is an IOException in the constructor
                    connection.attach(this);
                    connections.put(uuid, connection);
                    log.info(String.format("Accepted a new connection [%s] from IP: %s", uuid, clientSocket.getInetAddress().getHostAddress()));

                    if (receiverMode == ReceiverMode.MULTITHREADED) {
                        Thread thread = new Thread(connection);
                        thread.setDaemon(true);
                        thread.setName("Connection-" + uuid.toString().substring(0, 8));
                        thread.start();
                    } else if (receiverMode == ReceiverMode.THREADPOOLED) {
                        executorService.execute(connection);
                    }
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

    public void halt() {
        exit = true;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
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
