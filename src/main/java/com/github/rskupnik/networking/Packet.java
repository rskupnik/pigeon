package com.github.rskupnik.networking;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet {

    final int id;

    public Packet(int id) {
        this.id = id;
    }

    public void send(DataOutputStream outputStream) throws IOException {
        outputStream.writeByte(id);
    }
}
