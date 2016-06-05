package com.github.rskupnik.pigeon.commons;

public enum IncomingPacketHandleMode {
    QUEUE,
    HANDLER;

    public static IncomingPacketHandleMode fromString(String input) {
        switch (input.toUpperCase()) {
            case "QUEUE":
                return QUEUE;
            case "HANDLER":
                return HANDLER;
        }

        return null;
    }
}
