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

import java.io.IOException;

public abstract class UnixProcessUtils {

    public static String SIGNAL_TERM = "TERM";
    public static String SIGNAL_INT = "INT";
    public static String SIGNAL_HUP = "HUP";
    public static String SIGNAL_KILL = "KILL";

    private static final String UNIX_PROCESS_CLASS_NAME = "java.lang.UNIXProcess";

    public static boolean isUnixProcess(Process process) {
        return UNIX_PROCESS_CLASS_NAME.equals(process.getClass().getName());
    }

    public static int getUnixPid(Process process) throws UnixProcessException {
        if (!isUnixProcess(process)) {
            throw new UnixProcessException("process is not a UNIXProcess: " + process.getClass());
        }
        try {
            return (Integer) ReflectionUtils.getPrivateField(process, "pid");
        } catch (Exception exception) {
            throw new UnixProcessException("could not obtain pid using Java reflection", exception);
        }
    }

    public static void killUnixProcess(Process process, String signal) throws UnixProcessException {
        int pid = getUnixPid(process);
        try {
            new ProcessBuilder("kill", signal, Integer.toString(pid)).start();
        } catch (IOException ioException) {
            throw new UnixProcessException("could not execute kill command", ioException);
        }
    }

}
