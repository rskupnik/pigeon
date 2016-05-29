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
