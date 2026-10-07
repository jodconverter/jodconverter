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

import java.io.IOException;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.AbstractRetryable;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.TemporaryException;

/**
 * Starts an office process and connects to it, retrying the connection until the process accepts
 * it. A process that exits with the code 81 (a first start that initializes the profile) is started
 * again.
 */
class StartProcessAndConnectRetryable extends AbstractRetryable<Exception> {

  private static final Integer EXIT_CODE_81 = 81;
  private static final long NO_DELAY = 0L;

  private static final Logger LOGGER =
      LoggerFactory.getLogger(StartProcessAndConnectRetryable.class);

  private final ProcessBuilder processBuilder;
  private final long afterStartProcessDelay;
  private final OfficeConnection connection;
  private VerboseProcess process;

  /**
   * Creates a new instance of the class.
   *
   * @param processBuilder The builder used to start the process.
   * @param afterStartProcessDelay The delay after an attempt to start a process before doing
   *     anything else.
   * @param connection The office connection used to connect.
   */
  /* default */ StartProcessAndConnectRetryable(
      final ProcessBuilder processBuilder,
      final long afterStartProcessDelay,
      final OfficeConnection connection) {
    super();

    this.processBuilder = processBuilder;
    this.afterStartProcessDelay = afterStartProcessDelay;
    this.connection = connection;
  }

  @Override
  protected void attempt() throws Exception {

    // Do not start the process if already done.
    if (process == null) {
      process = startProcess();
      checkProcessAlive();
    }

    // Now, try to connect.
    try {
      connection.connect();
      LOGGER.trace("An attempt to connect to an office process succeeded");
    } catch (OfficeConnectionException ex) {
      LOGGER.trace("An attempt to connect to an office process has failed", ex);
      handleConnectionFailure(ex);
    }
  }

  /**
   * Gets the process started by this retryable.
   *
   * @return The started process, or null if no process was started.
   */
  /* default */ @Nullable VerboseProcess getProcess() {
    return process;
  }

  private VerboseProcess startProcess() throws IOException {

    final var started = new VerboseProcess(processBuilder.start());
    LOGGER.debug("Started the process; pid {}", started.getProcess().pid());
    if (afterStartProcessDelay > NO_DELAY) {
      LOGGER.debug("Waiting {} ms after the start of the process...", afterStartProcessDelay);
      sleep(afterStartProcessDelay);
    }
    return started;
  }

  private void checkProcessAlive() throws TemporaryException, OfficeException {

    final var exitCode = process.getExitCode();
    if (exitCode != null) {
      // The process has died.
      if (exitCode.equals(EXIT_CODE_81)) {
        // Restart and retry later.
        // see http://code.google.com/p/jodconverter/issues/detail?id=84
        LOGGER.info("Office process died with exit code 81; restarting it");
        process = null; // In order to restart the process
        throw new TemporaryException("Office process died with exit code 81");
      }
      throw new OfficeException("Office process died with exit code: " + exitCode);
    }
  }

  private void handleConnectionFailure(final OfficeConnectionException ex)
      throws TemporaryException, OfficeException {

    final var exitCode = process.getExitCode();
    if (exitCode == null) {
      // Process is still running; we must retry to reconnect only.
      throw new TemporaryException(ex);
    } else if (exitCode.equals(EXIT_CODE_81)) {
      process = null; // In order to restart the process
      // Restart and retry later
      // see http://code.google.com/p/jodconverter/issues/detail?id=84
      LOGGER.info("Office process died with exit code 81; restarting it");
      throw new TemporaryException(ex);
    } else {
      // Process has died trying to connect.
      throw new OfficeException("Office process died with exit code " + exitCode, ex);
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
