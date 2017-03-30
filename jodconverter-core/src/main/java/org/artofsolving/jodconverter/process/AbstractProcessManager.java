/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.process;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/** Base class for all process manager implementations. */
public abstract class AbstractProcessManager implements ProcessManager {

  /**
   * Execute the specified command and return the output.
   *
   * @param command a specified system command.
   * @return the execution output.
   * @throws IOException if an I/O error occurs.
   */
  protected List<String> execute(final String command) throws IOException {

    final Process process = Runtime.getRuntime().exec(command);
    process.getOutputStream().close(); // don't wait for stdin

    final List<String> lines = IOUtils.readLines(process.getInputStream(), "UTF-8");
    try {
      process.waitFor();
    } catch (InterruptedException interruptedEx) { // NOSONAR
      // sorry for the interruption
    }
    return lines;
  }

  @Override
  public long findPid(final ProcessQuery query) throws IOException {

    final String processRegex =
        Pattern.quote(query.getCommand()) + ".*" + Pattern.quote(query.getArgument());
    final Pattern commandPattern = Pattern.compile(processRegex);
    final Pattern processLinePattern = getProcessLinePattern();
    String currentProcessCommand = getCurrentProcessesCommand(query.getCommand());
    List<String> lines = execute(currentProcessCommand);
    for (final String line : lines) {
      final Matcher lineMatcher = processLinePattern.matcher(line);
      if (lineMatcher.matches()) {
        final String commandLine = lineMatcher.group(1);
        final String pid = lineMatcher.group(2);
        final Matcher commandMatcher = commandPattern.matcher(commandLine);
        if (commandMatcher.find()) {
          return Long.parseLong(pid);
        }
      }
    }
    return PID_NOT_FOUND;
  }

  /**
   * Gets the command to be executed to get a snapshot of all the running processes starting with
   * the specified argument (process).
   *
   * @param process name of the process to query for.
   * @return a string containing the program and its arguments.
   */
  protected abstract String getCurrentProcessesCommand(String process);

  /**
   * Gets the pattern to be used to match an output line containing the information about a running
   * process.
   *
   * @return the pattern.
   */
  protected abstract Pattern getProcessLinePattern();
}
