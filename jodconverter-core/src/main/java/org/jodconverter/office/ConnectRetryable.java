/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.office;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Performs a connection to an office process. */
public class ConnectRetryable extends Retryable {

  private static final Integer EXIT_CODE_NEW_INSTALLATION = Integer.valueOf(81);
  private static final Logger logger = LoggerFactory.getLogger(ConnectRetryable.class);

  private final OfficeProcess process;
  private final OfficeConnection connection;

  /**
   * Creates a new instance of the class for the specified connection.
   *
   * @param connection the office connection to connect.
   */
  public ConnectRetryable(final OfficeConnection connection) {
    this(null, connection);
  }

  /**
   * Creates a new instance of the class for the specified process and connection.
   *
   * @param process the office process whose exit code is to be retrieved.
   * @param connection the office connection to connect.
   */
  public ConnectRetryable(final OfficeProcess process, final OfficeConnection connection) {
    super();

    this.process = process;
    this.connection = connection;
  }

  @Override
  protected void attempt() throws Exception {

    try {
      // Try to connect
      connection.connect();

    } catch (OfficeConnectionException connectionEx) {

      // If we cannot get the exit code of a process, just
      // throw a TemporaryException
      if (process == null) {
        throw new TemporaryException(connectionEx);
      }

      // Here, we can get the exit code of the process
      final Integer exitCode = process.getExitCode();
      if (exitCode == null) {

        // Process is running; retry later
        throw new TemporaryException(connectionEx);

      } else if (exitCode.equals(EXIT_CODE_NEW_INSTALLATION)) {

        // Restart and retry later
        // see http://code.google.com/p/jodconverter/issues/detail?id=84
        logger.warn("Office process died with exit code 81; restarting it");
        process.start(true);
        throw new TemporaryException(connectionEx);

      } else {
        throw new OfficeException("Office process died with exit code " + exitCode);
      }
    }
  }
}
