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

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.AbstractRetryable;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.TemporaryException;
import org.jodconverter.local.process.ProcessManager;
import org.jodconverter.local.process.ProcessQuery;

/** Performs a starts of an office process. */
public class StartProcessRetryable extends AbstractRetryable<Exception> {

  private static final int FIND_PID_RETRIES = 10;
  private static final long FIND_PID_DELAY = 2_000L;
  private static final long FIND_PID_INTERVAL = 250L;
  private static final Integer EXIT_CODE_81 = 81;
  private static final Logger LOGGER = LoggerFactory.getLogger(StartProcessRetryable.class);

  private final ProcessManager processManager;
  private final ProcessBuilder processBuilder;
  private final ProcessQuery processQuery;
  private VerboseProcess process;
  private Integer exitCode;
  private long processId = PID_UNKNOWN;

  /**
   * Creates a new instance of the class.
   *
   * @param processManager The office process manager used to find the process id.
   * @param processBuilder The builder used to build the start the process.
   * @param processQuery The process query.
   */
  public StartProcessRetryable(
      final ProcessManager processManager,
      final ProcessBuilder processBuilder,
      final ProcessQuery processQuery) {
    super();

    this.processManager = processManager;
    this.processBuilder = processBuilder;
    this.processQuery = processQuery;
  }

  @Override
  protected void attempt() throws Exception {

    // Reset exitCode and processId
    exitCode = null;
    processId = PID_UNKNOWN;

    // Start the process.
    process = new VerboseProcess(processBuilder.start());

    // Try to retrieve the PID.

    // Add an initial delay for FreeBSD. Without this delay, on FreeBSD only, the
    // OfficeConnection.connect() will hang for more than 5 minutes before throwing
    // a timeout exception, we do not know why.
    // TODO: Investigate FreeBSD.
    if (SystemUtils.IS_OS_FREE_BSD) {
      LOGGER.debug("Waiting for process to start on FreeBSD...");
      sleep(FIND_PID_DELAY);
    }

    // Try to retrieve the PID.
    tryFindPid();

    if (exitCode != null) {

      if (exitCode.equals(EXIT_CODE_81)) {

        // Restart and retry later
        // see http://code.google.com/p/jodconverter/issues/detail?id=84
        LOGGER.warn("Office process died with exit code 81; restarting it");
        throw new TemporaryException("Office process died with exit code 81");
      }

      throw new OfficeException("Office process died with exit code: " + exitCode);
    }

    if (processManager.canFindPid() && processId <= PID_UNKNOWN) {
      try {
        process.getProcess().destroy();
      } catch (Exception ex) {
        LOGGER.warn("Unable to destroy the process", ex);
      }
      throw new TemporaryException(
          String.format(
              "A process with --accept '%s' started but its pid could not be found; restarting it",
              processQuery.getArgument()));
    }
  }

  /**
   * Gets the process started by this retryable.
   *
   * @return The started process.
   */
  public VerboseProcess getProcess() {
    return process;
  }

  /**
   * Gets the process id of the process started by this retryable.
   *
   * @return The started process id.
   */
  public long getProcessId() {
    return processId;
  }

  private void tryFindPid() throws IOException {

    int tryCount = 0;
    while (true) {
      tryCount++;
      LOGGER.debug("Trying to find pid, attempt #{}", tryCount);

      // Return if the process is already dead.
      try {
        exitCode = process.getProcess().exitValue();
        // Process is already dead, no need to wait longer...
        return;
      } catch (IllegalThreadStateException ignore) {
        // Process is still up.
      }

      if (!processManager.canFindPid()) {
        LOGGER.debug(
            "The current process manager does not support finding the pid: {}",
            processManager.getClass().getName());
        return;
      }

      // Try to find the PID.
      processId = processManager.findPid(processQuery);

      // Return if the PID was found or if we have reached the maximum try count.
      if (processId > PID_UNKNOWN || tryCount == FIND_PID_RETRIES) {
        return;
      }

      // Wait a bit before retrying.
      sleep(FIND_PID_INTERVAL);
    }
  }

  private void sleep(final long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ignore) {
      // ignore
    }
  }
}
