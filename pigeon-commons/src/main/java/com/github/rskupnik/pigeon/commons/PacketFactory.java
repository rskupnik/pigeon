package com.github.rskupnik.pigeon.commons;

import com.github.rskupnik.pigeon.commons.annotations.AnnotationsScanner;
import com.github.rskupnik.pigeon.commons.annotations.FieldBlueprint;
import com.github.rskupnik.pigeon.commons.annotations.IncomingPacketBlueprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class PacketFactory {

    private static final Logger log = LogManager.getLogger(PacketFactory.class);

    // TODO: Extract common code from here and Connection/AnnotationsScanner to a separate class
    // TODO: Optimize this - store constructor and methods in the blueprint so this method only has to instantiate and call the methods
    static Optional<Object> incomingPacket(int id, DataInputStream inputStream) {
        try {
            IncomingPacketBlueprint packetBlueprint = AnnotationsScanner.getInstance().getIncomingPacketBlueprint(id);
            Class<?> packetClass = packetBlueprint.getPacketClass();
            log.trace("PigeonPacket class is: "+packetClass);
            Constructor[] constructors = packetClass.getDeclaredConstructors();
            Constructor chosenConstructor = null;
            for (Constructor constructor : constructors) {
                log.trace("Found constructor: "+constructor);
                if (constructor.getGenericParameterTypes().length == 0) {
                    chosenConstructor = constructor;
                    break;
                }
            }
            if (chosenConstructor == null) {
                log.error("No-args constructor not found for class "+packetClass);
                return Optional.empty();
            }
            chosenConstructor.setAccessible(true);
            Object packet = chosenConstructor.newInstance();
            for (FieldBlueprint field : packetBlueprint.getFields()) {
                StringBuilder name = new StringBuilder();
                name.append(field.getFieldName().substring(0, 1).toUpperCase());
                name.append(field.getFieldName().substring(1));
                Method[] methods = packetClass.getDeclaredMethods();
                Method chosenMethod = null;
                inner: for (Method method : methods) {
                    log.trace("Found method: "+method);
                    if (method.getName().equals("set"+name)) {
                        chosenMethod = method;
                        break inner;
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
        if (fieldType.equals(Integer.TYPE) || fieldType.isInstance(Integer.class)) {
            return inputStream.readInt();
        } else if (fieldType.isInstance(String.class)) {
            return inputStream.readUTF();
        } else return null;
    }
}
