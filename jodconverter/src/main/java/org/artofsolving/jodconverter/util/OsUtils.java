package org.artofsolving.jodconverter.util;

public abstract class OsUtils {

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

}
