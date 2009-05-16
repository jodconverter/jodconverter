//
// JODConverter - Java OpenDocument Converter
// Copyright 2009 Art of Solving Ltd
// Copyright 2004-2009 Mirko Nasato
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
package org.artofsolving.jodconverter.office;

import java.io.File;
import java.util.Map;

import org.artofsolving.jodconverter.util.OsUtils;


import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.UnoRuntime;

public abstract class OfficeUtils {

    public static final String SERVICE_DESKTOP = "com.sun.star.frame.Desktop";

    @SuppressWarnings("unchecked")
    public static <T> T cast(Class<T> type, Object object) {
        return (T) UnoRuntime.queryInterface(type, object);
    }

    public static PropertyValue property(String name, Object value) {
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.Name = name;
        propertyValue.Value = value;
        return propertyValue;
    }

    public static PropertyValue[] toUnoProperties(Map<String,?> properties) {
        PropertyValue[] propertyValues = new PropertyValue[properties.size()];
        int i = 0;
        for (Map.Entry<String,?> entry : properties.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String,Object> subProperties = (Map<String,Object>) value;
                value = toUnoProperties(subProperties);
            }
            propertyValues[i++] = property((String) entry.getKey(), value);
        }
        return propertyValues;
    }

    public static String toUrl(File file) {
        String url = "file://" + file.toURI().getRawPath();
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        } else {
            return url;
        }
    }

    public static File getDefaultOfficeHome() {
        if (System.getProperty("office.home") != null) {
            return new File(System.getProperty("office.home"));
        }
        if (OsUtils.isWindows()) {
            return new File(System.getenv("ProgramFiles"), "OpenOffice.org 3");
        } else if (OsUtils.isMac()) {
            return new File("/Applications/OpenOffice.org.app/Contents");
        } else {
            // Linux or Solaris
            return new File("/opt/openoffice.org3");
        }
    }

    public static File getDefaultProfileDir() {
        if (System.getProperty("office.profile") != null) {
            return new File(System.getProperty("office.profile"));
        }
        if (OsUtils.isWindows()) {
            return new File(System.getenv("APPDATA"), "OpenOffice.org/3");
        } else if (OsUtils.isMac()) {
            return new File(System.getProperty("user.home"), "Library/Application Support/OpenOffice.org/3");
        } else {
            // Linux or Solaris
            return new File(System.getProperty("user.home"), ".openoffice.org/3");
        }
    }

}
