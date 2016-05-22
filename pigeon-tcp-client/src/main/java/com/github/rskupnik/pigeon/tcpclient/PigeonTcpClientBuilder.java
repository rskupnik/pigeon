package com.github.rskupnik.pigeon.tcpclient;

import com.github.rskupnik.pigeon.commons.PigeonClient;
import com.github.rskupnik.pigeon.commons.PigeonClientBuilder;

public class PigeonTcpClientBuilder implements PigeonClientBuilder {

    private String host;
    private int port;

    public PigeonTcpClientBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    public PigeonTcpClientBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public PigeonTcpClient build() {
        return new PigeonTcpClient(this);
    }
}
