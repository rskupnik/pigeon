package com.github.rskupnik.pigeon.commons.callback;

import com.github.rskupnik.pigeon.commons.Connection;

public interface ServerCallbackHandler {
    void onStarted();
    void onNewConnection(Connection connection);
}
