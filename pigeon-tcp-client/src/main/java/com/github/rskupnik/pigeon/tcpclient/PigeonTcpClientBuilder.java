package com.github.rskupnik.pigeon.tcpclient;

import com.github.rskupnik.pigeon.commons.*;
import com.github.rskupnik.pigeon.commons.callback.ClientCallbackHandler;
import com.github.rskupnik.pigeon.commons.client.PigeonClientBuilder;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;

public class PigeonTcpClientBuilder implements PigeonClientBuilder {

    private String host;
    private int port;
    private IncomingPacketHandleMode incomingPacketHandleMode;
    private PacketHandler packetHandler;
    private ClientCallbackHandler clientCallbackHandler;
    private String packageToScan;

    public PigeonTcpClientBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    public PigeonTcpClientBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public PigeonTcpClientBuilder withIncomingPacketHandleMode(IncomingPacketHandleMode incomingPacketHandleMode) {
        this.incomingPacketHandleMode = incomingPacketHandleMode;
        return this;
    }

    public PigeonTcpClientBuilder withPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
        return this;
    }

    public PigeonTcpClientBuilder withClientCallbackHandler(ClientCallbackHandler clientCallbackHandler) {
        this.clientCallbackHandler = clientCallbackHandler;
        return this;
    }

    public PigeonTcpClientBuilder withPackageToScan(String packageToScan) {
        this.packageToScan = packageToScan;
        return this;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public IncomingPacketHandleMode getIncomingPacketHandleMode() {
        return incomingPacketHandleMode;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public ClientCallbackHandler getClientCallbackHandler() {
        return clientCallbackHandler;
    }

    public String getPackageToScan() {
        return packageToScan;
    }

    public PigeonTcpClient build() throws PigeonException {
        return new PigeonTcpClient(this);
    }
}
