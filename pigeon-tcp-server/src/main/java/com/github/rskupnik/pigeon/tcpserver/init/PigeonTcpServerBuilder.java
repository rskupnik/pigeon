package com.github.rskupnik.pigeon.tcpserver.init;

import com.github.rskupnik.pigeon.commons.PigeonServerBuilder;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonServerException;
import com.github.rskupnik.pigeon.tcpserver.networking.PigeonTcpServer;

public final class PigeonTcpServerBuilder implements PigeonServerBuilder {

    private int port;
    private int receiverThreadsNumber;
    private PigeonTcpServer.IncomingPacketHandleMode incomingPacketHandleMode;

    public PigeonTcpServerBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public PigeonTcpServerBuilder withReceiverThreadsNumber(int receiverThreadsNumber) {
        this.receiverThreadsNumber = receiverThreadsNumber;
        return this;
    }

    public PigeonTcpServerBuilder withIncomingPacketHandleMode(PigeonTcpServer.IncomingPacketHandleMode incomingPacketHandleMode) {
        this.incomingPacketHandleMode = incomingPacketHandleMode;
        return this;
    }

    public int getPort() {
        return port;
    }

    public int getReceiverThreadsNumber() {
        return receiverThreadsNumber;
    }

    public PigeonTcpServer.IncomingPacketHandleMode getIncomingPacketHandleMode() {
        return incomingPacketHandleMode;
    }

    public PigeonTcpServer build() throws PigeonServerException {
        return new PigeonTcpServer(this);
    }
}
