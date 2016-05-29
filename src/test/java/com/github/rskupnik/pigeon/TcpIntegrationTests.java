package com.github.rskupnik.pigeon;

import com.github.rskupnik.pigeon.commons.Connection;
import com.github.rskupnik.pigeon.commons.IncomingPacketHandleMode;
import com.github.rskupnik.pigeon.commons.Packet;
import com.github.rskupnik.pigeon.commons.PacketHandler;
import com.github.rskupnik.pigeon.commons.callback.ClientCallbackHandler;
import com.github.rskupnik.pigeon.commons.callback.ServerCallbackHandler;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;
import com.github.rskupnik.pigeon.tcpclient.PigeonTcpClient;
import com.github.rskupnik.pigeon.tcpserver.PigeonTcpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TcpIntegrationTests {

    private static final int PORT = 10708;

    @Mock
    private ServerCallbackHandler serverCallbackHandler;

    @Mock
    private ClientCallbackHandler clientCallbackHandler;

    @Mock
    private PacketHandler packetHandler;

    private PigeonTcpServer server;
    private List<PigeonTcpClient> clients = new ArrayList<>();
    private List<Connection> connections = new ArrayList<>();

    @Before
    public void before() {
        clients.clear();
    }

    @After
    public void after() {
        server.shutdown();
        for (PigeonTcpClient client : clients) {
            client.disconnect();
        }
    }

    @Test
    public void shouldConnect() throws PigeonException {
        server = com.github.rskupnik.pigeon.tcpserver.Pigeon.newServer()
                .withPort(PORT)
                .withPacketHandler(packetHandler)
                .withServerCallbackHandler(serverCallbackHandler)
                .withIncomingPacketHandleMode(IncomingPacketHandleMode.HANDLER)
                .withReceiverThreadsNumber(0)
                .build();
        server.start();

        PigeonTcpClient client = com.github.rskupnik.pigeon.tcpclient.Pigeon.newClient()
                .withHost("localhost")
                .withPort(PORT)
                .withIncomingPacketHandleMode(IncomingPacketHandleMode.HANDLER)
                .withPacketHandler(packetHandler)
                .withClientCallbackHandler(clientCallbackHandler)
                .build();

        clients.add(client);

        verify(serverCallbackHandler, times(1)).onStarted();
        verify(serverCallbackHandler, times(1)).onNewConnection(any(Connection.class));
        verify(clientCallbackHandler, times(1)).onConnected();
    }

    @Test
    public void shouldAcceptOnlyOneConnectionWhenReceiverThreadsSetToOne() throws PigeonException {
        server = com.github.rskupnik.pigeon.tcpserver.Pigeon.newServer()
                .withPort(PORT)
                .withPacketHandler(packetHandler)
                .withServerCallbackHandler(serverCallbackHandler)
                .withIncomingPacketHandleMode(IncomingPacketHandleMode.HANDLER)
                .withReceiverThreadsNumber(1)
                .build();
        server.start();

        PigeonTcpClient client = com.github.rskupnik.pigeon.tcpclient.Pigeon.newClient()
                .withHost("localhost")
                .withPort(PORT)
                .withIncomingPacketHandleMode(IncomingPacketHandleMode.HANDLER)
                .withPacketHandler(packetHandler)
                .withClientCallbackHandler(clientCallbackHandler)
                .build();

        clients.add(client);

        try {
            PigeonTcpClient client2 = com.github.rskupnik.pigeon.tcpclient.Pigeon.newClient()
                    .withHost("localhost")
                    .withPort(PORT)
                    .withIncomingPacketHandleMode(IncomingPacketHandleMode.HANDLER)
                    .withPacketHandler(packetHandler)
                    .withClientCallbackHandler(clientCallbackHandler)
                    .build();

            clients.add(client2);
        } catch (PigeonException e) {
            assertEquals("Server refused connection", e.getMessage());
        }

        verify(serverCallbackHandler, times(1)).onStarted();
        verify(serverCallbackHandler, times(1)).onNewConnection(any(Connection.class));
        verify(clientCallbackHandler, times(1)).onConnected();
    }

    @Test
    public void shouldReceivePacketServerSide() throws PigeonException {
        server = com.github.rskupnik.pigeon.tcpserver.Pigeon.newServer()
                .withPort(PORT)
                .withPacketHandler(packetHandler)
                .withServerCallbackHandler(serverCallbackHandler)
                .withIncomingPacketHandleMode(IncomingPacketHandleMode.HANDLER)
                .withReceiverThreadsNumber(1)
                .build();
        server.start();

        PigeonTcpClient client = com.github.rskupnik.pigeon.tcpclient.Pigeon.newClient()
                .withHost("localhost")
                .withPort(PORT)
                .withIncomingPacketHandleMode(IncomingPacketHandleMode.QUEUE)
                .withClientCallbackHandler(clientCallbackHandler)
                .build();

        clients.add(client);

        client.send(new TestPacket());

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        verify(packetHandler, times(1)).handle(any(Packet.class));
    }

    @Test
    public void shouldReceivePacketClientSide() throws PigeonException {
        server = com.github.rskupnik.pigeon.tcpserver.Pigeon.newServer()
                .withPort(PORT)
                .withPacketHandler(packetHandler)
                .withServerCallbackHandler(new TestServerCallbackHandler())
                .withIncomingPacketHandleMode(IncomingPacketHandleMode.HANDLER)
                .withReceiverThreadsNumber(1)
                .build();
        server.start();

        PigeonTcpClient client = com.github.rskupnik.pigeon.tcpclient.Pigeon.newClient()
                .withHost("localhost")
                .withPort(PORT)
                .withIncomingPacketHandleMode(IncomingPacketHandleMode.HANDLER)
                .withPacketHandler(packetHandler)
                .withClientCallbackHandler(clientCallbackHandler)
                .build();

        clients.add(client);

        server.send(new TestPacket(), connections.get(0));

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        verify(packetHandler, times(1)).handle(any(Packet.class));
    }

    class TestServerCallbackHandler implements ServerCallbackHandler {

        @Override
        public void onStarted() {

        }

        @Override
        public void onNewConnection(Connection connection) {
            connections.add(connection);
        }
    }

    class TestClientCallbackHandler implements ClientCallbackHandler {

        @Override
        public void onConnected() {

        }
    }
}
