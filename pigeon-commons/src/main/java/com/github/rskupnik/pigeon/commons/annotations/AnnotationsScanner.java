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
import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;
import com.github.rskupnik.pigeon.commons.util.ReflectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
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

    private final Map<Integer, PacketBlueprint> packetBlueprints = new HashMap<>();

    private AnnotationsScanner() {

    }

    public void scan(String packageToScan) {
        log.debug("Scanning for annotated classes "+(packageToScan == null ? "everywhere" : "in "+packageToScan));

        // TODO: Allow the user to set a path to be scanned for annotated classes
        Reflections reflections = packageToScan != null ? new Reflections(packageToScan) : new Reflections();
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(PigeonPacket.class);
        for (Class<?> annotatedClass : annotatedClasses) {
            PigeonPacket classAnnotation = annotatedClass.getAnnotation(PigeonPacket.class);
            Integer id = classAnnotation.id();
            if (packetBlueprints.containsKey(id)) {
                log.warn(String.format("There is already a PigeonPacket registered with id %d, cannot register %s", id, annotatedClass.getName()));
                continue;
            }

            try {
                final Class<? extends Packet> clazz;
                try {
                    clazz = (Class<? extends Packet>) annotatedClass;
                } catch (ClassCastException e) {
                    throw new PigeonException("This class is annotated with @PigeonPacket but does not extend Packet - "+annotatedClass.getName());
                }

                Constructor defaultConstructor = ReflectionUtils.findDefaultConstructor(clazz)
                        .orElseThrow(() -> new PigeonException(
                                String.format("Cannot find default constructor in class [%s]",
                                        annotatedClass
                                )
                        )
                );

                PacketBlueprint blueprint = new PacketBlueprint(id, clazz, defaultConstructor);

                ReflectionUtils.getDataFields(clazz)
                        .forEach(field -> {
                            try {
                                Method getter = ReflectionUtils.findGetter(clazz, field)
                                        .orElseThrow(() -> new PigeonException(
                                                        String.format("Cannot find getter for field [%s] in class [%s]",
                                                                field.getName(),
                                                                annotatedClass.getName()
                                                        )
                                                )
                                        );
                                Method setter = ReflectionUtils.findSetter(clazz, field)
                                        .orElseThrow(() -> new PigeonException(
                                                        String.format("Cannot find setter for field [%s] in class [%s]",
                                                                field.getName(),
                                                                annotatedClass.getName()
                                                        )
                                                )
                                        );
                                blueprint.addField(new FieldBlueprint(field, getter, setter));
                            } catch (PigeonException e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                );

                packetBlueprints.put(id, blueprint);
                log.debug(String.format("Added a new annotated class [%s] with id [%d] and [%d] data fields.", annotatedClass.getName(), id, blueprint.getFields().size()));
            } catch (PigeonException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public Map<Integer, PacketBlueprint> getPacketBlueprints() {
        return packetBlueprints;
    }

    public PacketBlueprint getPacketBlueprint(int id) {
        return getPacketBlueprints().get(id);
    }
}
