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

package org.jodconverter.local.office;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A process started by JODConverter, whose standard and error outputs are logged by two daemon
 * threads, so that the process never blocks on a full output buffer.
 */
class VerboseProcess {

  private static final Logger LOGGER = LoggerFactory.getLogger(VerboseProcess.class);

  private final Process process;

  /* default */ VerboseProcess(final Process process) {
    super();

    Objects.requireNonNull(process, "process must not be null");
    this.process = process;
    pump("jodconverter-process-out-" + process.pid(), process.inputReader(), LOGGER::info);
    pump("jodconverter-process-err-" + process.pid(), process.errorReader(), LOGGER::error);
  }

  private static void pump(
      final String name, final BufferedReader reader, final Consumer<String> logger) {

    final var thread =
        new Thread(
            () -> {
              try (reader) {
                reader.lines().forEach(logger);
              } catch (IOException ex) {
                LOGGER.trace("Could not read the output of the process", ex);
              }
            },
            name);
    thread.setDaemon(true);
    thread.start();
  }

  /* default */ Process getProcess() {
    return process;
  }

  /**
   * Gets the exit code of the process.
   *
   * @return The exit code, or null if the process is still running.
   */
  /* default */ @Nullable Integer getExitCode() {
    try {
      final var exitValue = process.exitValue();
      LOGGER.trace("Process has been terminated with exit value {}", exitValue);
      return exitValue;
    } catch (IllegalThreadStateException ex) {
      LOGGER.trace("Could not get exit value; the process is running");
      return null;
    }
  }

  /**
   * Waits for the process to exit.
   *
   * @param timeout The maximum time to wait, in milliseconds.
   * @return {@code true} if the process exited, {@code false} if it is still running after the
   *     timeout, or if the current thread was interrupted.
   */
  /* default */ boolean waitFor(final long timeout) {
    try {
      return process.waitFor(timeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
