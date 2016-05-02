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
    private Server server;

    private Pigeon(Builder builder) {
        this.port = builder.port;
    }

    public static Builder newServer() {
        return new Builder();
    }

    public void start() throws PigeonException {
        log.info("Initializing Pigeon");
        initProperties();
        log.debug("Port: " + port);
        server = new Server(port);
        server.setDaemon(true);
        server.setName("Pigeon-server");
        server.start();
    }

    private void initProperties() throws PigeonInitializationException {
        try {
            Parrot.init();
        } catch (IOException e) {
            throw new PigeonInitializationException(e.getMessage(), e);
        }

        if (port == -1)
            port = Integer.parseInt(Parrot.get("port").orElse(String.valueOf(Defaults.PORT)));
    }

    public int getPort() {
        return port;
    }

    public static final class Builder {

        private int port = -1;

        public Builder() {

        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Pigeon build() {
            return new Pigeon(this);
        }
    }
}
