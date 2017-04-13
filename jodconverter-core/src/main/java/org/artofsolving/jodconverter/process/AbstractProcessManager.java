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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all process manager implementations included in the standard JODConverter
 * distribution.
 */
public abstract class AbstractProcessManager implements ProcessManager {

  private static class StreamPumper extends Thread {

    private final InputStream inputStream;
    private final List<String> outputLines;

    /**
     * Creates a new pumper for the specified input stream.
     *
     * @param inputStream input stream to read from.
     */
    public StreamPumper(final InputStream inputStream) {

      this.inputStream = inputStream;
      this.outputLines = new ArrayList<>();
    }

    /**
     * Gets the output lines from this input stream pumper.
     *
     * @return the command output lines.
     */
    public List<String> getOutputLines() {
      return outputLines;
    }

    @Override
    public void run() {

      try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
          BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          outputLines.add(line);
        }
      } catch (IOException ex) {
        logger.error("Unable to read from command input stream.", ex);
      }
    }
  }

  private static final Logger logger = LoggerFactory.getLogger(AbstractProcessManager.class);

  private String buildOutput(final String label, final List<String> lines) {

    final StringBuilder strlines = new StringBuilder(label + ":");
    for (final String line : lines) {
      strlines.append('\n').append(line);
    }
    return strlines.toString();
  }

  /**
   * Executes the specified command and return the output.
   *
   * @param cmdarray array containing the command to call and its arguments.
   * @return the command execution output.
   * @throws IOException if an I/O error occurs.
   */
  protected List<String> execute(final String[] cmdarray) throws IOException {

    final Process process = Runtime.getRuntime().exec(cmdarray);

    final StreamPumper outPumper = new StreamPumper(process.getInputStream());
    final StreamPumper errPumper = new StreamPumper(process.getErrorStream());

    outPumper.start();
    errPumper.start();
    try {
      process.waitFor();
      outPumper.join();
      errPumper.join();
    } catch (InterruptedException ex) {

      // Log the interruption
      logger.warn(
          "The current thread was interrupted while waiting for command execution output.", ex);
      // Restore the interrupted status
      Thread.currentThread().interrupt();
    }

    final List<String> outLines = outPumper.getOutputLines();
    final List<String> errLines = errPumper.getOutputLines();

    if (logger.isDebugEnabled()) {

      logger.debug(buildOutput("Command Output", outLines));
      logger.debug(buildOutput("Command Error", errLines));
    }

    if ((outLines == null || outLines.isEmpty()) && (errLines != null && !errLines.isEmpty())) {
      logger.error(
          "Error running command\n{}\n{}", cmdarray, buildOutput("Command Error", errLines));
    }
    return outLines;
  }

  @Override
  public long findPid(final ProcessQuery query) throws IOException {

    final Pattern commandPattern =
        Pattern.compile(
            Pattern.quote(query.getCommand()) + ".*" + Pattern.quote(query.getArgument()));
    final Pattern processLinePattern = getRunningProcessLinePattern();
    final String[] currentProcessesCommand = getRunningProcessesCommand(query.getCommand());

    logger.debug(
        "Finding PID using\n"
            + "Command to get current running processes: {}\n"
            + "Regex used to match current running process lines: {}\n"
            + "Regex used to match running office process we are looking for: {}",
        currentProcessesCommand,
        processLinePattern.pattern(),
        commandPattern.pattern());

    final List<String> lines = execute(currentProcessesCommand);
    for (final String line : lines) {
      if (StringUtils.isBlank(line)) {
        // Skip this one
        continue;
      }
      logger.debug(
          "Checking if process line matches the process line regex\nProcess line: {}", line);
      final Matcher lineMatcher = processLinePattern.matcher(line);
      if (lineMatcher.matches()) {
        final String pid = lineMatcher.group("Pid");
        final String commandLine = lineMatcher.group("CommanLine");
        logger.debug(
            "Line matches!\n"
                + "pid: {}; Command line: {}\n"
                + "Checking if this command line matches the office command line regex",
            pid,
            commandLine);
        final Matcher commandMatcher = commandPattern.matcher(commandLine);
        if (commandMatcher.find()) {
          logger.debug("Command line matches! Returning pid: {}", pid);
          return Long.parseLong(pid);
        }
      }
    }
    return PID_NOT_FOUND;
  }

  /**
   * Gets the command to be executed to get a snapshot of all the running processes identified by
   * the specified argument (process).
   *
   * @param process name of the process to query for.
   * @return an array containing the command to call and its arguments.
   */
  protected abstract String[] getRunningProcessesCommand(String process);

  /**
   * Gets the pattern to be used to match an output line containing the information about a running
   * process. The output lines being tested against this pattern are the result of the execution of
   * the command returned by the getRunningProcessesCommand function.
   *
   * @return the pattern.
   * @see #getRunningProcessesCommand(String)
   */
  protected abstract Pattern getRunningProcessLinePattern();
}
