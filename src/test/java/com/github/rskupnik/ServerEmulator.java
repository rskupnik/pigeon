package com.github.rskupnik;

import com.github.rskupnik.annotations.IncomingPacket;
import com.github.rskupnik.annotations.PacketDataField;
import com.github.rskupnik.exceptions.PigeonException;
import com.github.rskupnik.networking.Server;

public class ServerEmulator {

    public ServerEmulator() throws PigeonException {
        Pigeon pigeon = Pigeon.newServer()
                .withPort(9434)
                .build();

        pigeon.start();

        while (true) {

        }
    }

    public static void main(String[] args) throws PigeonException {
        new ServerEmulator();
    }
}
