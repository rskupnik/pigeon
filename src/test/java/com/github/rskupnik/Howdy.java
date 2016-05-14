package com.github.rskupnik;

import com.github.rskupnik.annotations.IncomingPacket;
import com.github.rskupnik.annotations.PacketDataField;
import com.github.rskupnik.networking.Packet;

@IncomingPacket(id=5)
public class Howdy extends Packet {

    @PacketDataField
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
