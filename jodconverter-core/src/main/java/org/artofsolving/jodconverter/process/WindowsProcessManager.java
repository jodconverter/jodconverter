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
package org.artofsolving.jodconverter.process;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * {@link ProcessManager} implementation for Windows.
 * <p>
 * Requires wmic.exe and taskkill.exe, that should be available at least on Windows XP, Windows
 * Vista, and Windows 7 (except Home versions).
 */
public class WindowsProcessManager extends AbstractProcessManager {

    private static final Pattern PROCESS_GET_LINE = Pattern.compile("^(.*?)\\s+(\\d+)\\s*$");

    @Override
    public void kill(Process process, long pid) throws IOException {

        execute("taskkill", "/t", "/f", "/pid", String.valueOf(pid));
    }

    /**
     * Gets whether the commands we need are available for a Windows OS.
     * 
     * @return {@code true} if the required commands are available, {@code false} otherwise.
     */
    public boolean isUsable() {

        try {
            execute("wmic", "quit");
            execute("taskkill", "/?");
            return true;
        } catch (IOException ioException) {
            return false;
        }
    }

    @Override
    protected String[] getCurrentProcessesCommand() {

        return new String[] { "wmic", "process", "get", "CommandLine,ProcessId" };
    }

    @Override
    protected Pattern getProcessLinePattern() {

        return PROCESS_GET_LINE;
    }
}
