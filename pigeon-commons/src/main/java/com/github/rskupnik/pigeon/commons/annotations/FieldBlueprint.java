package com.github.rskupnik.pigeon.commons.annotations;

public class FieldBlueprint {

    private Class fieldType;
    private String fieldName;

    FieldBlueprint(Class fieldType, String fieldName) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public Class getFieldType() {
        return fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }
}
