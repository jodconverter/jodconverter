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

/**
 * Provides services required to manage a running process.
 */
public interface ProcessManager {

    public static final long PID_NOT_FOUND = -2;
    public static final long PID_UNKNOWN = -1;

    /**
     * Kills the specified process.
     * 
     * @param process
     *            the process to kill.
     * @param pid
     *            the id of the process to kiil.
     */
    void kill(Process process, long pid) throws IOException;

    /**
     * Finds a PID of a running process that has the specified command line.
     * 
     * @param query
     *            a query used to find the process with the pid we are looking for.
     * @return the pid if found, {@link #PID_NOT_FOUND} if not, or {@link #PID_UNKNOWN} if this
     *         implementation is unable to find out
     * @throws IOException
     */
    long findPid(ProcessQuery query) throws IOException;
}
