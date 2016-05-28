package com.github.rskupnik.pigeon.tcpclient;

import com.github.rskupnik.pigeon.commons.client.PigeonClientBuilder;

public final class Pigeon {

    public static PigeonTcpClientBuilder newClient() {
        return new PigeonTcpClientBuilder();
    }
}
