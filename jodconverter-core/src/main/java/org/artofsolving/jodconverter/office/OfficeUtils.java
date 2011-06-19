//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2011 Mirko Nasato and contributors
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

import org.artofsolving.jodconverter.util.PlatformUtils;

import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.UnoRuntime;

public class OfficeUtils {

    public static final String SERVICE_DESKTOP = "com.sun.star.frame.Desktop";

    private OfficeUtils() {
        throw new AssertionError("utility class must not be instantiated");
    }

    public static <T> T cast(Class<T> type, Object object) {
        return (T) UnoRuntime.queryInterface(type, object);
    }

    public static PropertyValue property(String name, Object value) {
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.Name = name;
        propertyValue.Value = value;
        return propertyValue;
    }

    @SuppressWarnings("unchecked")
    public static PropertyValue[] toUnoProperties(Map<String,?> properties) {
        PropertyValue[] propertyValues = new PropertyValue[properties.size()];
        int i = 0;
        for (Map.Entry<String,?> entry : properties.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                Map<String,Object> subProperties = (Map<String,Object>) value;
                value = toUnoProperties(subProperties);
            }
            propertyValues[i++] = property((String) entry.getKey(), value);
        }
        return propertyValues;
    }

    public static String toUrl(File file) {
        String path = file.toURI().getRawPath();
        String url = path.startsWith("//") ? "file:" + path : "file://" + path;
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static File getDefaultOfficeHome() {
        if (System.getProperty("office.home") != null) {
            return new File(System.getProperty("office.home"));
        }
        if (PlatformUtils.isWindows()) {
            // %ProgramFiles(x86)% on 64-bit machines; %ProgramFiles% on 32-bit ones
            String programFiles = System.getenv("ProgramFiles(x86)");
            if (programFiles == null) {
                programFiles = System.getenv("ProgramFiles");
            }
            return findOfficeHome(
                programFiles + File.separator + "OpenOffice.org 3",
                programFiles + File.separator + "LibreOffice 3"
            );
        } else if (PlatformUtils.isMac()) {
            return findOfficeHome(
                "/Applications/OpenOffice.org.app/Contents",
                "/Applications/LibreOffice.app/Contents"
            );
        } else {
            // Linux or other *nix variants
            return findOfficeHome(
                "/opt/openoffice.org3",
                "/opt/libreoffice",
                "/usr/lib/openoffice",
                "/usr/lib/libreoffice"
            );
        }
    }

    private static File findOfficeHome(String... knownPaths) {
        for (String path : knownPaths) {
            File home = new File(path);
            if (getOfficeExecutable(home).isFile()) {
                return home;
            }
        }
        return null;
    }

    public static File getOfficeExecutable(File officeHome) {
        if (PlatformUtils.isMac()) {
            return new File(officeHome, "MacOS/soffice.bin");
        } else {
            return new File(officeHome, "program/soffice.bin");
        }
    }

}
