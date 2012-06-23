//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
//    -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
//    -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter;

import java.lang.reflect.Field;

public class ReflectionUtils {

    private ReflectionUtils() {
        throw new AssertionError("utility class must not be instantiated");
    }

    public static Object getPrivateField(Object instance, String fieldName) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        return getPrivateField(instance.getClass(), instance, fieldName);
    }

    public static Object getPrivateField(Class<?> type, Object instance, String fieldName) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }

}
