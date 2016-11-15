//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
// -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
// -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.office;

import java.io.File;
import java.util.Map;

import org.artofsolving.jodconverter.process.MacProcessManager;
import org.artofsolving.jodconverter.process.ProcessManager;
import org.artofsolving.jodconverter.process.PureJavaProcessManager;
import org.artofsolving.jodconverter.process.UnixProcessManager;
import org.artofsolving.jodconverter.process.WindowsProcessManager;
import org.artofsolving.jodconverter.util.PlatformUtils;

import com.sun.star.beans.PropertyValue;

public class OfficeUtils {

    public static ProcessManager findBestProcessManager() {
        if (PlatformUtils.isLinux()) {
            return new UnixProcessManager();
        } else if (PlatformUtils.isMac()) {
            return new MacProcessManager();
        } else if (PlatformUtils.isWindows()) {
            WindowsProcessManager windowsProcessManager = new WindowsProcessManager();
            return windowsProcessManager.isUsable() ? windowsProcessManager : new PureJavaProcessManager();
        } else {
            // NOTE: UnixProcessManager can't be trusted to work on Solaris
            // because of the 80-char limit on ps output there
            return new PureJavaProcessManager();
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

    public static File getDefaultOfficeHome() {
        if (System.getProperty("office.home") != null) {
            return new File(System.getProperty("office.home"));
        }
        if (PlatformUtils.isWindows()) {

            // Try to find the most recent version of LibreOffice or OpenOffice,
            // starting with the 64-bit version. %ProgramFiles(x86)% on 64-bit
            // machines; %ProgramFiles% on 32-bit ones
            String programFiles64 = System.getenv("ProgramFiles");
            String programFiles32 = System.getenv("ProgramFiles(x86)");
            //@formatter:off
            return findOfficeHome(
                    programFiles64 + File.separator + "LibreOffice 5",
                    programFiles64 + File.separator + "LibreOffice 4",
                    programFiles64 + File.separator + "LibreOffice 3",
                    programFiles32 + File.separator + "LibreOffice 5",
                    programFiles32 + File.separator + "LibreOffice 4",
                    programFiles32 + File.separator + "LibreOffice 3",
                    programFiles32 + File.separator + "OpenOffice.org 3");
            //@formatter:on

        } else if (PlatformUtils.isMac()) {
            //@formatter:off
            return findOfficeHome(
                    "/Applications/LibreOffice.app/Contents",
                    "/Applications/OpenOffice.org.app/Contents");
            //@formatter:on
        } else {
            // Linux or other *nix variants
            //@formatter:off
            return findOfficeHome(
                    "/usr/lib64/libreoffice",
                    "/usr/lib/libreoffice",
                    "/opt/libreoffice",
                    "/usr/lib64/openoffice",
                    "/usr/lib64/openoffice.org3",
                    "/usr/lib64/openoffice.org",
                    "/usr/lib/openoffice",
                    "/usr/lib/openoffice.org3",
                    "/usr/lib/openoffice.org",
                    "/opt/openoffice.org3");
            //@formatter:on
        }
    }

    public static File getOfficeExecutable(File officeHome) {
        if (PlatformUtils.isMac()) {
            // Starting with LibreOffice 4.1 the location of the executable has changed on Mac.
            // It's now in program/soffice. Handle both cases!
            File executableFile = new File(officeHome, "MacOS/soffice.bin");
            if (!executableFile.isFile()) {
                executableFile = new File(officeHome, "program/soffice");
            }
            return executableFile;
        } else if (PlatformUtils.isWindows()) {
            return new File(officeHome, "program/soffice.exe");
        } else {
            return new File(officeHome, "program/soffice.bin");
        }
    }

    public static PropertyValue property(String name, Object value) {
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.Name = name;
        propertyValue.Value = value;
        return propertyValue;
    }

    public static PropertyValue[] toUnoProperties(Map<String, ?> properties) {

        PropertyValue[] propertyValues = new PropertyValue[properties.size()];
        int i = 0;
        for (Map.Entry<String, ?> entry : properties.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> subProperties = (Map<String, Object>) value;
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

    // Private ctor.
    private OfficeUtils() {
        throw new AssertionError("utility class must not be instantiated");
    }

}
