package com.github.rskupnik.pigeon.tcpserver;

import com.github.rskupnik.pigeon.commons.IncomingPacketHandleMode;
import com.github.rskupnik.pigeon.commons.Packet;
import com.github.rskupnik.pigeon.commons.PacketHandler;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class PigeonTcpServerBuilderTest {

    private PigeonTcpServer server;

    @After
    public void after() {
        server.shutdown();
    }

    @Test
    public void shouldBuildServerWithDefaultValues() throws PigeonException {
        server = Pigeon.newServer()
                .withPropertiesFilename("nonexistant-config.properties")
                .withPacketHandler(new PacketHandler() {
                    @Override
                    public void handle(Packet packet) {

                    }
                })
                .build();

        assertNotNull(server);
        assertEquals(TcpServerDefaults.PORT, server.getPort());
        assertEquals(TcpServerDefaults.RECEIVER_THREADS_NUMBER, server.getReceiverThreadsNumber());
        assertEquals(TcpServerDefaults.PACKAGE_TO_SCAN, server.getPackageToScan());
        assertEquals(TcpServerDefaults.PACKET_HANDLE_MODE, server.getIncomingPacketHandleMode());
    }

    @Test
    public void shouldBuildServerWithDefaultPropertiesFile() throws PigeonException {
        server = Pigeon.newServer()
                .withPacketHandler(new PacketHandler() {
                    @Override
                    public void handle(Packet packet) {

                    }
                })
                .build();

        assertNotNull(server);
        assertEquals(9192, server.getPort());
        assertEquals(1, server.getReceiverThreadsNumber());
        assertEquals("com.github.rskupnik.pigeon.tcpserver", server.getPackageToScan());
        assertEquals(IncomingPacketHandleMode.HANDLER, server.getIncomingPacketHandleMode());
    }

    @Test
    public void shouldBuildServerWithSpecificPropertiesFile() throws PigeonException {
        server = Pigeon.newServer()
                .withPropertiesFilename("pigeon-tcp-server-2")
                .build();

        assertNotNull(server);
        assertEquals(9193, server.getPort());
        assertEquals(2, server.getReceiverThreadsNumber());
        assertEquals("com.github.rskupnik.pigeon.tcpserver", server.getPackageToScan());
        assertEquals(IncomingPacketHandleMode.QUEUE, server.getIncomingPacketHandleMode());
    }

    @Test
    public void shouldBuildServerWithUserSpecifiedProperties() throws PigeonException {
        int port = 9194;
        int threads = 3;
        IncomingPacketHandleMode handleMode = IncomingPacketHandleMode.QUEUE;

        server = Pigeon.newServer()
                .withPort(port)
                .withReceiverThreadsNumber(threads)
                .withIncomingPacketHandleMode(handleMode)
                .build();

        assertNotNull(server);
        assertEquals(port, server.getPort());
        assertEquals(threads, server.getReceiverThreadsNumber());
        assertEquals("com.github.rskupnik.pigeon.tcpserver", server.getPackageToScan());
        assertEquals(handleMode, server.getIncomingPacketHandleMode());
    }
}
