package com.github.rskupnik.annotations;

import java.util.HashSet;
import java.util.Set;

public class IncomingPacketBlueprint {

    private final int id;
    private final Class<?> packetClass;
    private final Set<FieldBlueprint> fields = new HashSet<>();

    IncomingPacketBlueprint(int id, Class<?> packetClass) {
        this.id = id;
        this.packetClass = packetClass;
    }

    public int getId() {
        return id;
    }

    public Set<FieldBlueprint> getFields() {
        return fields;
    }

    public Class<?> getPacketClass() {
        return packetClass;
    }

    void addField(FieldBlueprint field) {
        fields.add(field);
    }
}
