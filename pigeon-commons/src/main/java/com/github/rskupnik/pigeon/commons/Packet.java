package com.github.rskupnik.pigeon.commons;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet {

    int id;

    public void send(DataOutputStream outputStream) throws IOException {
        outputStream.writeByte(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
