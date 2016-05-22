package com.github.rskupnik.pigeon.tcpclient;

import com.github.rskupnik.pigeon.commons.PigeonClient;

public class PigeonTcpClient implements PigeonClient {

    private String host;
    private int port;

    public PigeonTcpClient(PigeonTcpClientBuilder builder) {
        this.host = host;
        this.port = port;
    }
}
