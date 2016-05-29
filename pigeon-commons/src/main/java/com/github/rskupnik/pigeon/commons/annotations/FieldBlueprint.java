package com.github.rskupnik.pigeon.commons.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class FieldBlueprint {

    private Field field;
    private Method setter;
    private Method getter;

    FieldBlueprint(Field field, Method getter, Method setter) {
        this.field = field;
        this.getter = getter;
        this.setter = setter;
    }

    public Field getField() {
        return field;
    }

    public Method getSetter() {
        return setter;
    }

    public Method getGetter() {
        return getter;
    }
}
