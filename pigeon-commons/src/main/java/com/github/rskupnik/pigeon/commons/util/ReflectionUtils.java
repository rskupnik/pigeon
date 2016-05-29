package com.github.rskupnik.pigeon.commons.util;

import com.github.rskupnik.pigeon.commons.Packet;
import com.github.rskupnik.pigeon.commons.annotations.PacketDataField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReflectionUtils {

    private enum AccessMethodType {
        GET, SET;
    }

    public static List<Field> getDataFields(Class<? extends Packet> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(fields -> fields.getAnnotation(PacketDataField.class) != null)
                .collect(Collectors.toList());
    }

    public static Optional<Constructor> findDefaultConstructor(Class<? extends Packet> clazz) {
        Constructor[] constructors = clazz.getDeclaredConstructors();
        Constructor chosenConstructor = null;
        for (Constructor constructor : constructors) {
            if (constructor.getGenericParameterTypes().length == 0) {
                chosenConstructor = constructor;
                break;
            }
        }

        if (chosenConstructor == null)
            return Optional.empty();

        chosenConstructor.setAccessible(true);
        return Optional.of(chosenConstructor);
    }

    public static Optional<Method> findGetter(Class<? extends Packet> clazz, Field field) {
        return findAccessMethod(clazz, field, AccessMethodType.GET);
    }

    public static Optional<Method> findSetter(Class<? extends Packet> clazz, Field field) {
        return findAccessMethod(clazz, field, AccessMethodType.SET);
    }

    private static Optional<Method> findAccessMethod(Class<? extends Packet> clazz, Field field, AccessMethodType type) {
        StringBuilder name = new StringBuilder();
        name.append(field.getName().substring(0, 1).toUpperCase());
        name.append(field.getName().substring(1));
        Method[] methods = clazz.getDeclaredMethods();
        Method chosenMethod = null;
        for (Method method : methods) {
            if (method.getName().equals((type == AccessMethodType.GET ? "get" : "set") + name)) {
                chosenMethod = method;
                break;
            }
        }

        if (chosenMethod == null)
            return Optional.empty();

        chosenMethod.setAccessible(true);
        return Optional.of(chosenMethod);
    }
}
