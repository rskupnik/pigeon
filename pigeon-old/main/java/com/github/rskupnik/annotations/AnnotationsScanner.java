package com.github.rskupnik.annotations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnnotationsScanner {

    private static AnnotationsScanner INSTANCE;

    public static AnnotationsScanner getInstance() {
        if (INSTANCE == null)
            INSTANCE = new AnnotationsScanner();

        return INSTANCE;
    }

    private static final Logger log = LogManager.getLogger(AnnotationsScanner.class);

    private final Map<Integer, IncomingPacketBlueprint> incomingPacketBlueprints = new HashMap<>();

    private AnnotationsScanner() {

    }

    public void scan() {
        log.debug("Scanning for annotated classes...");
        // Find all classes annotated as IncomingPacket
        // TODO: Allow the user to set a path to be scanned for annotated classes
        Set<Class<?>> annotatedClasses = new Reflections().getTypesAnnotatedWith(IncomingPacket.class);
        for (Class<?> annotatedClass : annotatedClasses) {
            IncomingPacket classAnnotation = annotatedClass.getAnnotation(IncomingPacket.class);
            Integer id = classAnnotation.id();
            if (incomingPacketBlueprints.containsKey(id)) {
                log.warn(String.format("There is already an IncomingPacket registered with id %d, cannot register %s", id, annotatedClass.getName()));
                continue;
            }

            IncomingPacketBlueprint blueprint = new IncomingPacketBlueprint(id, annotatedClass);
            for (Field field : annotatedClass.getDeclaredFields()) {
                if (field.getAnnotation(PacketDataField.class) == null)
                    continue;
                FieldBlueprint fieldBlueprint = new FieldBlueprint(field.getType(), field.getName());
                blueprint.addField(fieldBlueprint);
            }
            incomingPacketBlueprints.put(id, blueprint);
            log.debug(String.format("Added a new annotated class %s with id %d and %d annotated fields.", annotatedClass.getName(), id, blueprint.getFields().size()));
        }
    }

    public Map<Integer, IncomingPacketBlueprint> getIncomingPacketBlueprints() {
        return incomingPacketBlueprints;
    }

    public IncomingPacketBlueprint getIncomingPacketBlueprint(int id) {
        return getIncomingPacketBlueprints().get(id);
    }
}
