package com.github.rskupnik.networking;

import com.github.rskupnik.annotations.AnnotationsScanner;
import com.github.rskupnik.annotations.FieldBlueprint;
import com.github.rskupnik.annotations.IncomingPacketBlueprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class PacketFactory {

    private static final Logger log = LogManager.getLogger(PacketFactory.class);

    // TODO: Optimize this - store constructor and methods in the blueprint so this method only has to instantiate and call the methods
    static Optional<Object> incomingPacket(int id, DataInputStream inputStream) {
        try {
            IncomingPacketBlueprint packetBlueprint = AnnotationsScanner.getInstance().getIncomingPacketBlueprint(id);
            Class<?> packetClass = packetBlueprint.getPacketClass();
            log.trace("Packet class is: "+packetClass);
            Constructor[] constructors = packetClass.getDeclaredConstructors();
            Constructor chosenConstructor = null;
            for (Constructor constructor : constructors) {
                log.trace("Found constructor: "+constructor);
                if (constructor.getGenericParameterTypes().length == 0) {
                    chosenConstructor = constructor;
                    break;
                }
            }
            chosenConstructor.setAccessible(true);
            Object packet = chosenConstructor.newInstance();
            for (FieldBlueprint field : packetBlueprint.getFields()) {
                StringBuilder name = new StringBuilder();
                name.append(field.getFieldName().substring(0, 1).toUpperCase());
                name.append(field.getFieldName().substring(1));
                Method[] methods = packetClass.getDeclaredMethods();
                Method chosenMethod = null;
                for (Method method : methods) {
                    log.trace("Found method: "+method);
                    if (method.getName().equals("set"+name)) {
                        chosenMethod = method;
                        break;
                    }
                }
                Object value = getValue(inputStream, field.getFieldType());
                chosenMethod.setAccessible(true);
                chosenMethod.invoke(packet, value);
            }
            return Optional.of(packet);
        } catch (InstantiationException | IllegalAccessException  | IOException | InvocationTargetException e) {
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
