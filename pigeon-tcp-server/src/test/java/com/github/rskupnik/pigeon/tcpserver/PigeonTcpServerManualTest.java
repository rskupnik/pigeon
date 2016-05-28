package com.github.rskupnik.pigeon.tcpserver;

import com.github.rskupnik.pigeon.commons.IncomingPacketHandleMode;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;

public class PigeonTcpServerManualTest {

    public static void main(String[] args) throws PigeonException {
        Pigeon.newServer()
                .withPort(9050)
                .withReceiverThreadsNumber(0)
                .withIncomingPacketHandleMode(IncomingPacketHandleMode.QUEUE)
                .build()
                .start();

        while (true) {

        }
    }
}
