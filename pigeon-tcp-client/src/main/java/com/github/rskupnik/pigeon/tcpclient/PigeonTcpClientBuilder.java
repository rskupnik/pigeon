package com.github.rskupnik.pigeon.tcpclient;

import com.github.rskupnik.parrot.Parrot;
import com.github.rskupnik.pigeon.commons.*;
import com.github.rskupnik.pigeon.commons.callback.ClientCallbackHandler;
import com.github.rskupnik.pigeon.commons.client.PigeonClientBuilder;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;

public class PigeonTcpClientBuilder implements PigeonClientBuilder {

    private final String PROPERTY_HOST = "host";
    private final String PROPERTY_PORT = "port";
    private final String PROPERTY_PACKAGE_TO_SCAN = "package_to_scan";
    private final String PROPERTY_PACKET_HANDLE_MODE = "packet_handle_mode";

    private String propertiesFilename = TcpClientDefaults.PROPERTIES_FILENAME;

    private String host;
    private Integer port;
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

    public PigeonTcpClientBuilder withPropertiesFilename(String propertiesFilename) {
        this.propertiesFilename = propertiesFilename;
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
        load();
        validate();
        return new PigeonTcpClient(this);
    }

    private void validate() throws PigeonException {
        if (port == null)
            throw new PigeonException("Port cannot be null");

        if (host == null || host.length() == 0)
            throw new PigeonException("Host cannot be null");

        if (incomingPacketHandleMode == null)
            throw new PigeonException("Incoming packet handle mode cannot be null");

        if (incomingPacketHandleMode == IncomingPacketHandleMode.HANDLER && packetHandler == null)
            throw new PigeonException("Incoming packet handle mode is set to HANDLER but no handler was specified");
    }

    private void load() throws PigeonException {
        Parrot parrot = new Parrot(propertiesFilename);

        try {
            if (host == null || host.length() == 0)
                host = parrot.get(PROPERTY_HOST).isPresent() ? parrot.get(PROPERTY_HOST).get() : TcpClientDefaults.HOST;

            if (port == null)
                port = parrot.get(PROPERTY_PORT).isPresent() ? Integer.parseInt(parrot.get(PROPERTY_PORT).get()) : TcpClientDefaults.PORT;

            if (packageToScan == null)
                packageToScan = parrot.get(PROPERTY_PACKAGE_TO_SCAN).orElse(TcpClientDefaults.PACKAGE_TO_SCAN);

            if (incomingPacketHandleMode == null)
                incomingPacketHandleMode = parrot.get(PROPERTY_PACKET_HANDLE_MODE).isPresent() ? IncomingPacketHandleMode.fromString(parrot.get(PROPERTY_PACKET_HANDLE_MODE).get()) : TcpClientDefaults.PACKET_HANDLE_MODE;

        } catch (ClassCastException e) {
            throw new PigeonException(e.getMessage());
        }
    }
}
