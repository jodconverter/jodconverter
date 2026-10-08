/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jodconverter.local.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.util.StringUtils;

/**
 * Base class of the process managers that list the running processes with a command of the
 * operating system, when the JVM cannot read their command lines. The output of the command is
 * matched line by line against a pattern that captures the {@code Pid} and the {@code CommandLine}
 * groups.
 */
public abstract class AbstractProcessManager implements ProcessManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProcessManager.class);

  /** The number of times the command that lists the processes is tried before giving up. */
  public static final int ATTEMPTS = 5;

  /**
   * The delay before the second attempt, in milliseconds. It doubles before each following attempt
   * (250, 500, 1000 and 2000 ms): a listing that fails may keep failing for a few seconds.
   */
  public static final long RETRY_DELAY = 250L;

  /** Initializes a new instance of the class. */
  protected AbstractProcessManager() {
    super();
  }

  /**
   * Executes the specified command and returns the lines of its output. The error output is logged
   * at the trace level, and reported when the command fails.
   *
   * @param command The command to execute.
   * @return The lines of the standard output of the command.
   * @throws IOException If the command cannot be started, or if it exits with a status other than
   *     0; the message then has the error output of the command.
   */
  protected @NonNull List<@NonNull String> execute(final @NonNull String... command)
      throws IOException {

    final var process = new ProcessBuilder(command).start();
    // The error output is drained on its own thread, so that neither stream fills its buffer
    // while the other one is read.
    final var errorLines = new ArrayList<String>();
    final var errorReader =
        new Thread(
            () -> {
              try (var reader = process.errorReader()) {
                reader
                    .lines()
                    .filter(StringUtils::isNotBlank)
                    .forEach(
                        line -> {
                          LOGGER.trace("Command Error: {}", line);
                          errorLines.add(line);
                        });
              } catch (IOException ex) {
                LOGGER.trace("Could not read the error output of the command", ex);
              }
            },
            "jodconverter-command-err");
    errorReader.setDaemon(true);
    errorReader.start();
    final List<String> lines;
    try (var reader = process.inputReader()) {
      lines = reader.lines().toList();
    }
    final int status;
    try {
      status = process.waitFor();
      errorReader.join();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while waiting for the command to end", ex);
    }
    if (LOGGER.isTraceEnabled()) {
      lines.stream()
          .filter(StringUtils::isNotBlank)
          .forEach(line -> LOGGER.trace("Command Output: {}", line));
    }
    if (status != 0) {
      throw new IOException(
          "The command "
              + command[0]
              + " exited with the status "
              + status
              + (errorLines.isEmpty() ? "" : ": " + String.join(" ", errorLines)));
    }
    return lines;
  }

  /**
   * Executes the specified command, trying again after a short delay when it fails: the process
   * listing of an operating system can fail now and then (a cancelled WMI call on Windows, for
   * example), and the failure has nothing to do with the processes looked for.
   *
   * @param command The command to execute.
   * @return The lines of the standard output of the command.
   * @throws IOException If the command fails {@link #ATTEMPTS} times in a row.
   * @see #getRetryDelay(int)
   */
  protected @NonNull List<@NonNull String> executeWithRetries(final @NonNull String... command)
      throws IOException {

    var attempt = 1;
    while (true) {
      try {
        return execute(command);
      } catch (IOException ex) {
        if (attempt >= ATTEMPTS) {
          throw ex;
        }
        LOGGER.debug("The command listing the processes failed on attempt #{}; retrying", attempt);
        attempt++;
        try {
          Thread.sleep(getRetryDelay(attempt - 1));
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          ex.addSuppressed(interrupted);
          throw ex;
        }
      }
    }
  }

  /**
   * Gets the delay before a new attempt to list the processes.
   *
   * @param failures The number of attempts that failed so far, at least 1.
   * @return The delay, in milliseconds: {@link #RETRY_DELAY}, doubled for each failure after the
   *     first one.
   */
  protected long getRetryDelay(final int failures) {
    return RETRY_DELAY << Math.max(0, failures - 1);
  }

  @Override
  public @NonNull Optional<ProcessHandle> find(final @NonNull ProcessQuery query)
      throws IOException {

    final var commandPattern = query.commandLinePattern();
    final var linePattern = getRunningProcessLinePattern();
    final var command = getRunningProcessesCommand(query.command());
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(
          "Finding a process: command {}; line pattern {}; command line pattern {}",
          List.of(command),
          linePattern,
          commandPattern);
    }
    for (final var line : executeWithRetries(command)) {
      final var lineMatcher = linePattern.matcher(line);
      if (lineMatcher.matches()
          && commandPattern.matcher(lineMatcher.group("CommandLine")).find()) {
        final var pid = Long.parseLong(lineMatcher.group("Pid"));
        LOGGER.debug("Found a process matching the query; pid {}", pid);
        return ProcessHandle.of(pid);
      }
    }
    LOGGER.debug("No process matches the query");
    return Optional.empty();
  }

  /**
   * Gets the command that lists the running processes whose executable has the given name.
   *
   * @param process The name of the executable.
   * @return The command and its arguments.
   */
  protected abstract @NonNull String[] getRunningProcessesCommand(@NonNull String process);

  /**
   * Gets the pattern that matches a line of the output of the command, capturing the {@code Pid}
   * and the {@code CommandLine} groups.
   *
   * @return The pattern.
   */
  protected abstract @NonNull Pattern getRunningProcessLinePattern();
}
