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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.RetryTimeoutException;

/**
 * An {@link ExternalOfficeConnectionManager} is responsible to manage an office connection (bridge)
 * to an office process in an {@link ExternalOfficeWorker}.
 *
 * <p>All its functions block until they are done. They are called by the thread of the worker that
 * owns the manager, except {@link #disconnect()}, which may also be called by another thread to end
 * a task that must not go on.
 *
 * @see OfficeConnection
 */
class ExternalOfficeConnectionManager {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ExternalOfficeConnectionManager.class);

  private final OfficeConnection connection;
  private final long connectTimeout;
  private final long connectRetryInterval;

  /**
   * Creates a new manager with the specified configuration.
   *
   * @param connectTimeout Timeout after which a connection attempt will fail.
   * @param connectRetryInterval Timeout between each try to connect.
   * @param connection The object that will manage the connection to the office process.
   */
  /* default */ ExternalOfficeConnectionManager(
      final long connectTimeout,
      final long connectRetryInterval,
      final OfficeConnection connection) {

    this.connectTimeout = connectTimeout;
    this.connectRetryInterval = connectRetryInterval;
    this.connection = connection;
  }

  /**
   * Gets the connection of this manager.
   *
   * @return The {@link OfficeConnection} of this manager.
   */
  /* default */ OfficeConnection getConnection() {
    return connection;
  }

  /**
   * Connects to the external office process, if not already connected. The function returns when
   * the connection is established.
   *
   * @throws OfficeException If the connection could not be established with the external process.
   */
  /* default */ void connect() throws OfficeException {

    if (!connection.isConnected()) {
      LOGGER.debug("Connecting to external office process...");
      try {
        new ConnectRetryable(connection).execute(connectRetryInterval, connectTimeout);
      } catch (RetryTimeoutException ex) {
        throw new OfficeException("Could not establish connection to external process.", ex);
      }
    }
  }

  /** Disconnects from the external office process, if connected. */
  /* default */ void disconnect() {

    if (connection.isConnected()) {
      LOGGER.debug("Disconnecting from external office process...");
      connection.disconnect();
    }
  }

  /**
   * Reconnects to the external office process. The function returns when the connection is
   * established again.
   *
   * @throws OfficeException If the connection could not be established with the external process.
   */
  /* default */ void reconnect() throws OfficeException {
    LOGGER.info("Reconnecting to external office process...");

    disconnect();
    connect();
  }
}
