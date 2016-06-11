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

package com.github.rskupnik.pigeon.commons;

import com.github.rskupnik.pigeon.commons.annotations.AnnotationsScanner;
import com.github.rskupnik.pigeon.commons.annotations.FieldBlueprint;
import com.github.rskupnik.pigeon.commons.annotations.PacketBlueprint;
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;
import com.github.rskupnik.pigeon.commons.util.DataTypeDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class PacketFactory {

    private static final Logger log = LogManager.getLogger(PacketFactory.class);

    static Optional<Object> incomingPacket(int id, DataInputStream inputStream) throws PigeonException {
        try {
            PacketBlueprint packetBlueprint = AnnotationsScanner.getInstance().getPacketBlueprint(id);
            Class<? extends Packet> packetClass = packetBlueprint.getPacketClass();

            if (packetBlueprint.getDefaultConstructor() == null)
                throw new PigeonException(String.format("Cannot find default constructor in class [%s]", packetClass));

            Packet packet = (Packet) packetBlueprint.getDefaultConstructor().newInstance();
            for (FieldBlueprint fieldBlueprint : packetBlueprint.getFields()) {
                if (fieldBlueprint.getSetter() == null)
                    throw new PigeonException(String.format("Cannot find setter for field [%s] in class [%s]", fieldBlueprint.getField().getName(), packet.getClass().getName()));

                Object value = DataTypeDecoder.read(inputStream, fieldBlueprint.getField());
                fieldBlueprint.getSetter().invoke(packet, value);
            }
            return Optional.of(packet);
        } catch (InstantiationException | IllegalAccessException  | InvocationTargetException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}
