package com.github.rskupnik.pigeon.tcpserver;

import com.github.rskupnik.pigeon.commons.IncomingPacketHandleMode;
import com.github.rskupnik.pigeon.commons.PacketHandler;
import com.github.rskupnik.pigeon.commons.server.PigeonServerBuilder;
import com.github.rskupnik.pigeon.commons.callback.ServerCallbackHandler;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonServerException;

public final class PigeonTcpServerBuilder implements PigeonServerBuilder {

    private int port;
    private int receiverThreadsNumber;
    private IncomingPacketHandleMode incomingPacketHandleMode;
    private PacketHandler packetHandler;
    private ServerCallbackHandler serverCallbackHandler;

    public PigeonTcpServerBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public PigeonTcpServerBuilder withReceiverThreadsNumber(int receiverThreadsNumber) {
        this.receiverThreadsNumber = receiverThreadsNumber;
        return this;
    }

    public PigeonTcpServerBuilder withIncomingPacketHandleMode(IncomingPacketHandleMode incomingPacketHandleMode) {
        this.incomingPacketHandleMode = incomingPacketHandleMode;
        return this;
    }

    public PigeonTcpServerBuilder withPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
        return this;
    }

    public PigeonTcpServerBuilder withServerCallbackHandler(ServerCallbackHandler serverCallbackHandler) {
        this.serverCallbackHandler = serverCallbackHandler;
        return this;
    }

    public int getPort() {
        return port;
    }

    public int getReceiverThreadsNumber() {
        return receiverThreadsNumber;
    }

    public IncomingPacketHandleMode getIncomingPacketHandleMode() {
        return incomingPacketHandleMode;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public ServerCallbackHandler getServerCallbackHandler() {
        return serverCallbackHandler;
    }

    public PigeonTcpServer build() throws PigeonServerException {
        return new PigeonTcpServer(this);
    }
}
