package com.github.rskupnik.pigeon.tcpclient;

import com.github.rskupnik.pigeon.commons.IncomingPacketHandleMode;

public interface TcpClientDefaults {
    String HOST = null;
    String PROPERTIES_FILENAME = "pigeon-tcp-client.properties";
    int PORT = 9191;
    String PACKAGE_TO_SCAN = null;
    IncomingPacketHandleMode PACKET_HANDLE_MODE = IncomingPacketHandleMode.HANDLER;
}
