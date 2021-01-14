/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

import static org.jodconverter.local.process.ProcessManager.PID_UNKNOWN;

import java.io.IOException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.AbstractRetryable;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.TemporaryException;
import org.jodconverter.core.util.OSUtils;
import org.jodconverter.local.process.ProcessManager;
import org.jodconverter.local.process.ProcessQuery;

/** Performs a starts of an office process. */
class StartProcessAndConnectRetryable extends AbstractRetryable<Exception> {

  // TODO: Make "FIND_PID_*" constants configurable
  private static final int FIND_PID_RETRIES = 10;
  private static final long FIND_PID_DELAY = 2_000L;
  private static final long FIND_PID_INTERVAL = 250L;
  private static final Integer EXIT_CODE_81 = 81;
  private static final Logger LOGGER =
      LoggerFactory.getLogger(StartProcessAndConnectRetryable.class);

  private final ProcessManager processManager;
  private final ProcessBuilder processBuilder;
  private final ProcessQuery processQuery;
  private final OfficeConnection connection;
  private StartProcessResult result;

  /** Private class holding the result of a started process attempt. */
  private static class StartProcessResult {
    /* default */ VerboseProcess process;
    /* default */ long pid = PID_UNKNOWN;
    /* default */ Integer exitCode;
  }

  /**
   * Creates a new instance of the class.
   *
   * @param processManager The office process manager used to find the process id.
   * @param processBuilder The builder used to build the start the process.
   * @param processQuery The process query.
   * @param connection The office connection used to connect.
   */
  /* default */ StartProcessAndConnectRetryable(
      final ProcessManager processManager,
      final ProcessBuilder processBuilder,
      final ProcessQuery processQuery,
      final OfficeConnection connection) {
    super();

    this.processManager = processManager;
    this.processBuilder = processBuilder;
    this.processQuery = processQuery;
    this.connection = connection;
  }

  @Override
  protected void attempt() throws Exception {

    // Do not start the process if already done.
    if (result == null) {

      // Try to start the process
      result = startProcess();

      if (result.exitCode != null) {
        // The process has died.

        if (result.exitCode.equals(EXIT_CODE_81)) {
          result = null; // In order to restart the process

          // Restart and retry later.
          // see http://code.google.com/p/jodconverter/issues/detail?id=84
          LOGGER.warn("Office process died with exit code 81; restarting it");
          throw new TemporaryException("Office process died with exit code 81");
        }

        throw new OfficeException("Office process died with exit code: " + result.exitCode);
      }

      if (processManager.canFindPid() && result.pid <= PID_UNKNOWN) {

        // The pid could not be found.
        try {
          result.process.getProcess().destroy();
        } catch (Exception ex) {
          LOGGER.warn("Could not destroy the process", ex);
        }
        result = null; // In order to restart the process
        throw new TemporaryException(
            String.format(
                "A process with --accept '%s' started but its pid could not be found; restarting it",
                processQuery.getArgument()));
      }
    }

    // Now, try to connect.
    try {
      connection.connect();
      // SUCCESS
    } catch (OfficeConnectionException ex) {
      LOGGER.trace("An attempt to connect to an office process has failed", ex);

      // Here, we can get the exit code of the process
      final Integer exitCode = result.process.getExitCode();
      if (exitCode == null) {
        // Process is still running; we must retry to reconnect only.
        throw new TemporaryException(ex);
      } else if (exitCode.equals(EXIT_CODE_81)) {
        result = null; // In order to restart the process

        // Restart and retry later
        // see http://code.google.com/p/jodconverter/issues/detail?id=84
        LOGGER.warn("Office process died with exit code 81; restarting it");
        throw new TemporaryException(ex);

      } else {
        // Process has died trying to connect.
        throw new OfficeException("Office process died with exit code " + exitCode, ex);
      }
    }
  }

  /**
   * Gets the process started by this retryable.
   *
   * @return The started process.
   */
  public @NonNull VerboseProcess getProcess() {
    return result.process;
  }

  /**
   * Gets the process id of the process started by this retryable.
   *
   * @return The started process id.
   */
  public long getProcessId() {
    return result.pid;
  }

  private StartProcessResult startProcess() throws IOException {

    final StartProcessResult result = new StartProcessResult();

    // Start the process.
    result.process = new VerboseProcess(processBuilder.start());

    // Try to retrieve the PID.

    // Add an initial delay for FreeBSD. Without this delay, on FreeBSD only, the
    // OfficeConnection.connect() will hang for more than 5 minutes before throwing
    // a timeout exception, we do not know why.
    // TODO: Investigate FreeBSD.
    if (OSUtils.IS_OS_FREE_BSD) {
      LOGGER.debug("Waiting for process to start on FreeBSD...");
      sleep(FIND_PID_DELAY);
    }

    // Try to retrieve the PID.
    int tryCount = 0;
    while (true) {
      tryCount++;
      LOGGER.debug("Trying to find pid, attempt #{}", tryCount);

      // Return if the process is already dead.
      try {
        result.exitCode = result.process.getProcess().exitValue();
        // Process is already dead, no need to wait longer...
        return result;
      } catch (IllegalThreadStateException ignored) {
        // Process is still up.
      }

      if (!processManager.canFindPid()) {
        LOGGER.debug(
            "The current process manager does not support finding the pid: {}",
            processManager.getClass().getName());
        return result;
      }

      // Try to find the PID.
      result.pid = processManager.findPid(processQuery);

      // Return if the PID was found or if we have reached the maximum try count.
      if (result.pid > PID_UNKNOWN || tryCount == FIND_PID_RETRIES) {
        return result;
      }

      // Wait a bit before retrying.
      sleep(FIND_PID_INTERVAL);
    }
  }

  private void sleep(final long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
  }
}
