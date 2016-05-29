package com.github.rskupnik.pigeon.commons.util;

import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class DataTypeDecoder {

    public static void write(DataOutputStream clientOutputStream, Object value, Field field) throws PigeonException {
        try {
            if (field.getType().equals(Integer.TYPE) || field.getType().isInstance(Integer.class)) {
                clientOutputStream.writeInt((int) value);
            } else if (field.getType().isInstance(String.class)) {
                clientOutputStream.writeUTF((String) value);
            } else {
                throw new PigeonException("Cannot handle this data type: "+field.getType());
            }
        } catch (IOException e) {
            throw new PigeonException(e.getMessage());
        }
    }

    public static Object read(DataInputStream inputStream, Field field) throws PigeonException {
        try {
            if (field.getType().equals(Integer.TYPE) || field.getType().isInstance(Integer.class)) {
                return inputStream.readInt();
            } else if (field.getType().isInstance(String.class)) {
                return inputStream.readUTF();
            } else return null;
        } catch (IOException e) {
            throw new PigeonException(e.getMessage());
        }
    }
}
