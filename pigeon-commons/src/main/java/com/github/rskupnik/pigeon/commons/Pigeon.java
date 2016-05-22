package com.github.rskupnik.pigeon.commons;

public interface Pigeon {
    PigeonServerBuilder newServer();
    PigeonClientBuilder newClient();
}
