/*
    Copyright 2016 Radosław Skupnik

    This file is part of pigeon-commons.

    Pigeon-commons is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Pigeon-commons is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Pigeon-commons; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

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
