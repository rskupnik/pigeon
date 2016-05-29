package com.github.rskupnik.pigeon.commons.annotations;

import com.github.rskupnik.pigeon.commons.Packet;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

public final class PacketBlueprint {

    private final int id;
    private final Class<? extends Packet> packetClass;
    private final Constructor defaultConstructor;
    private final Set<FieldBlueprint> fields = new HashSet<>();

    PacketBlueprint(int id, Class<? extends Packet> packetClass, Constructor defaultConstructor) {
        this.id = id;
        this.packetClass = packetClass;
        this.defaultConstructor = defaultConstructor;
    }

    public int getId() {
        return id;
    }

    public Set<FieldBlueprint> getFields() {
        return fields;
    }

    public Class<? extends Packet> getPacketClass() {
        return packetClass;
    }

    public Constructor getDefaultConstructor() {
        return defaultConstructor;
    }

    void addField(FieldBlueprint field) {
        fields.add(field);
    }
}
