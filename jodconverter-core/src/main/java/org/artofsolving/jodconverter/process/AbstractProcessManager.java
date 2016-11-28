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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * Base class for all process manager implementations.
 */
public abstract class AbstractProcessManager implements ProcessManager {

    /**
     * Execute the specified command and return the output.
     * 
     * @param command
     *            a string array containing the program and its arguments.
     * @return the execution output.
     * @throws IOException
     *             if an I/O error occurs.
     */
    protected List<String> execute(String... command) throws IOException {

        Process process = new ProcessBuilder(command).start();
        process.getOutputStream().close(); // don't wait for stdin
        List<String> lines = IOUtils.readLines(process.getInputStream(), "UTF-8");
        try {
            process.waitFor();
        } catch (InterruptedException interruptedEx) {
            // sorry for the interruption
        }
        return lines;
    }

    /**
     * Finds a PID of a running process that has the specified command line.
     * 
     * @param query
     *            a query used to find the process with the pid we are looking for.
     * @return the pid if found, {@link #PID_NOT_FOUND} if not, or {@link #PID_UNKNOWN} if this
     *         implementation is unable to find out
     * @throws IOException
     */
    public long findPid(ProcessQuery query) throws IOException {

        String processRegex = Pattern.quote(query.getCommand()) + ".*" + Pattern.quote(query.getArgument());
        Pattern commandPattern = Pattern.compile(processRegex);
        Pattern processLinePattern = getProcessLinePattern();
        for (String line : execute(getCurrentProcessesCommand())) {
            Matcher lineMatcher = processLinePattern.matcher(line);
            if (lineMatcher.matches()) {
                String commandLine = lineMatcher.group(1);
                String pid = lineMatcher.group(2);
                Matcher commandMatcher = commandPattern.matcher(commandLine);
                if (commandMatcher.find()) {
                    return Long.parseLong(pid);
                }
            }
        }
        return PID_NOT_FOUND;
    }

    /**
     * Gets the command to be executed to get a snapshot of all the running processes.
     * 
     * @return a string array containing the program and its arguments.
     */
    protected abstract String[] getCurrentProcessesCommand();

    /**
     * Gets the pattern to be used to match an output line containing the information about a
     * running process.
     * 
     * @return the pattern.
     */
    protected abstract Pattern getProcessLinePattern();

    /**
     * Kills the specified process.
     * 
     * @param process
     *            the process to kill.
     * @param pid
     *            the id of the process to kiil.
     */
    public abstract void kill(Process process, long pid) throws IOException;
}
