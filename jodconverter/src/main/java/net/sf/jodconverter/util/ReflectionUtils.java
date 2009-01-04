//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2009 - Mirko Nasato and Contributors
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter.  If not, see
// <http://www.gnu.org/licenses/>.
//
package net.sf.jodconverter.util;

import java.lang.reflect.Field;

public abstract class ReflectionUtils {

    public static Object getPrivateField(Object instance, String fieldName) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        return getPrivateField(instance.getClass(), instance, fieldName);
    }

    public static Object getPrivateField(Class<?> type, Object instance, String fieldName) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }

}
