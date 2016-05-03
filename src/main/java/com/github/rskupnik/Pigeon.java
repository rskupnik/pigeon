package com.github.rskupnik;

import com.github.rskupnik.exceptions.PigeonException;
import com.github.rskupnik.exceptions.PigeonInitializationException;
import com.github.rskupnik.networking.Server;
import com.github.rskupnik.parrot.Parrot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public final class Pigeon {

    private static final Logger log = LogManager.getLogger(Pigeon.class);

    private int port = -1;
    private String receiverMode = null;

    private Server server;

    private Pigeon(Builder builder) {
        this.port = builder.port;
        this.receiverMode = builder.receiverMode;
    }

    public static Builder newServer() {
        return new Builder();
    }

    public void start() throws PigeonException {
        log.info("Initializing Pigeon");
        initProperties();
        log.debug("Port: " + port);
        server = new Server(port, receiverMode);
        server.setDaemon(true);
        server.setName("Pigeon-server");
        server.start();
    }

    public void stop() {
        if (server != null)
            server.halt();
    }

    private void initProperties() throws PigeonInitializationException {
        Parrot.newInstance("pidgeon");

        if (port == -1)
            port = Integer.parseInt(Parrot.getInstance().get("port").orElse(String.valueOf(Defaults.PORT)));

        if (receiverMode == null)
            receiverMode = Parrot.getInstance().get("receiver_mode").orElse(Defaults.RECEIVER_MODE);
    }

    public int getPort() {
        return port;
    }

    public Server.ReceiverMode getReceiverMode() {
        return server != null ? server.getReceiverMode() : null;
    }

    public static final class Builder {

        private int port = -1;
        private String receiverMode = null;

        public Builder() {

        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withReceiverMode(String receiverMode) {
            this.receiverMode = receiverMode;
            return this;
        }

        public Pigeon build() {
            return new Pigeon(this);
        }
    }
}
