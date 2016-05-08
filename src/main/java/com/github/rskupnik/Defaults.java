package com.github.rskupnik;

import com.github.rskupnik.networking.Server;

public final class Defaults {

    public static final int PORT = 9292;
    public static final Server.ReceiverMode RECEIVER_MODE = Server.ReceiverMode.MULTITHREADED;
    public static final int RECEIVER_THREADS = 10;

    private Defaults() {

    }
}
