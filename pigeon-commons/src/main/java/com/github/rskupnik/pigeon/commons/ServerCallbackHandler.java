package com.github.rskupnik.pigeon.commons;

public interface ServerCallbackHandler {
    void onStarted();
    void onNewConnection(Connection connection);
}
