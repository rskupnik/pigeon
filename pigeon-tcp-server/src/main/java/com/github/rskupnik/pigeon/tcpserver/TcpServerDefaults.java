package com.github.rskupnik.pigeon.tcpserver;

import com.github.rskupnik.pigeon.commons.IncomingPacketHandleMode;

public interface TcpServerDefaults {
    String PROPERTIES_FILENAME = "pigeon-tcp-server.properties";
    int PORT = 9191;
    int RECEIVER_THREADS_NUMBER = 0;    // This equals to inifinite
    String PACKAGE_TO_SCAN = null;
    IncomingPacketHandleMode PACKET_HANDLE_MODE = IncomingPacketHandleMode.HANDLER;
}
