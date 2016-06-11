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
