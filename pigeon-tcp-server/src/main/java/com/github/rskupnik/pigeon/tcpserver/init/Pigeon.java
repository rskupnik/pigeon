package com.github.rskupnik.pigeon.tcpserver.init;

import com.github.rskupnik.pigeon.commons.PigeonClientBuilder;

public class Pigeon implements com.github.rskupnik.pigeon.commons.Pigeon {

    public PigeonTcpServerBuilder newServer() {
        return new PigeonTcpServerBuilder();
    }

    public PigeonClientBuilder newClient() {
        return null;
    }
}
