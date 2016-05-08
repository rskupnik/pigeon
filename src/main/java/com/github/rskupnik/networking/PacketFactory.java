package com.github.rskupnik.networking;

import com.github.rskupnik.annotations.AnnotationsScanner;
import com.github.rskupnik.annotations.FieldBlueprint;
import com.github.rskupnik.annotations.IncomingPacketBlueprint;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class PacketFactory {

    static Optional<Object> incomingPacket(int id, DataInputStream inputStream) {
        try {
            IncomingPacketBlueprint packetBlueprint = AnnotationsScanner.getInstance().getIncomingPacketBlueprint(id);
            Object packet = packetBlueprint.getPacketClass().getConstructor().newInstance();
            for (FieldBlueprint field : packetBlueprint.getFields()) {
                StringBuilder name = new StringBuilder();
                name.append(field.getFieldName().substring(0, 1).toUpperCase());
                name.append(field.getFieldName().substring(1));
                Method method = packetBlueprint.getPacketClass().getMethod("set"+name);
                Object value = getValue(inputStream, field.getFieldType());
                method.invoke(packet, value);
            }
            return Optional.of(packet);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | IOException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    static Object getValue(DataInputStream inputStream, Class fieldType) throws IOException {
        if (fieldType.isInstance(Integer.class)) {
            return inputStream.readInt();
        } else if (fieldType.isInstance(String.class)) {
            return inputStream.readUTF();
        } else return null;
    }
}
