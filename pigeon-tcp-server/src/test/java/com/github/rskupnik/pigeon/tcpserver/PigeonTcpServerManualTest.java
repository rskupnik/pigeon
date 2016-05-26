package com.github.rskupnik.pigeon.tcpserver;

import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;
import com.github.rskupnik.pigeon.tcpserver.init.Pigeon;
import com.github.rskupnik.pigeon.tcpserver.networking.PigeonTcpServer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

public class PigeonTcpServerManualTest {

    public static void main(String[] args) throws PigeonException {
        Pigeon.newServer()
                .withPort(9050)
                .withReceiverThreadsNumber(0)
                .withIncomingPacketHandleMode(PigeonTcpServer.IncomingPacketHandleMode.QUEUE)
                .build()
                .start();

        while (true) {

        }
    }
}
