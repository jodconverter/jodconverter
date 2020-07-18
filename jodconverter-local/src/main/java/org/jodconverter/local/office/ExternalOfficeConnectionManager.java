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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.NamedThreadFactory;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.RetryTimeoutException;

/**
 * An {@link ExternalOfficeConnectionManager} is responsible to manage an office connection (bridge)
 * to an office process in a {@link ExternalOfficeManagerPoolEntry}.
 *
 * @see OfficeConnection
 */
class ExternalOfficeConnectionManager {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ExternalOfficeConnectionManager.class);

  // Timeout for the stop task.
  // TODO: Should it be configurable.
  private static final long STOP_TASK_TIMEOUT = 60_000L;

  private final OfficeConnection connection;
  private final ExecutorService executor;

  private final long connectTimeout;
  private final long connectRetryInterval;
  private final boolean connectFailFast;

  /**
   * Creates a new manager with the specified configuration.
   *
   * @param connectTimeout Timeout after which a connection attempt will fail.
   * @param connectRetryInterval Timeout between each try to connect.
   * @param connectFailFast Controls whether the manager will "fail fast" if we can't connect to the
   *     office process. If set to {@code true}, the {@link #connect()} operation will wait for the
   *     task to be completed, and will throw an exception if we cannot connect to the office
   *     process. If set to {@code false}, the {@link #connect()} operation will submit the task and
   *     return immediately, meaning a faster operation.
   * @param connection The object that will managed the connection to the office process.
   */
  /* default */ ExternalOfficeConnectionManager(
      final long connectTimeout,
      final long connectRetryInterval,
      final boolean connectFailFast,
      final OfficeConnection connection) {

    this.connectTimeout = connectTimeout;
    this.connectRetryInterval = connectRetryInterval;
    this.connectFailFast = connectFailFast;
    this.connection = connection;

    executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("jodconverter-extlcxnmng"));
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
   * Connects to the external office process.
   *
   * <p>The task of connecting to the external office process is executed by a single thread {@link
   * ExecutorService} and thus, the current {@code restartDueToLostConnection()} function returns
   * immediately.
   *
   * @throws OfficeException If we are unable to connect to the started process.
   */
  /* default */ void connect() throws OfficeException {

    if (connectFailFast) {
      connectAndWait();
    } else {
      // Submit a connect task to the executor and return immediately.
      executor.execute(
          () -> {
            try {
              connect0();
            } catch (Exception ex) {
              LOGGER.error("Could not establish connection to external process.", ex);
            }
          });
    }
  }

  /**
   * Disconnects the current manager and wait until it is fully disconnected.
   *
   * @throws OfficeException If we are not able to stop the manager.
   */
  /* default */ void disconnect() throws OfficeException {

    // Submit a task to stop the manager and wait task termination.
    // This is required if we don't want to let garbage on disk since the
    // disconnect must be fully executed to clean the temp files and
    // directories.

    // Submit a task to disconnect from the external office process.
    executor.execute(this::disconnect0);

    // Shutdown the executor, no other task will be accepted.
    executor.shutdown();

    // Await for task termination. This is required if we don't want to let garbage on disk.
    try {
      // TODO: Add <stop> configuration option for this ?
      // Wait 1 minute max for termination. It seems a safe and reasonable amount of time.
      LOGGER.debug("Waiting for stop task to complete ({}) millisecs)...", STOP_TASK_TIMEOUT);
      executor.awaitTermination(STOP_TASK_TIMEOUT, TimeUnit.MILLISECONDS);
      LOGGER.debug("Stop task executed successfully.");
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new OfficeException(
          "Interruption while disconnecting from external office process.", ex);
    }
  }

  /**
   * Reconnects to the external office process.
   *
   * <p>The task of reconnecting to the external office process is executed by a single thread
   * {@link ExecutorService} and thus, the current {@code restartDueToLostConnection()} function
   * returns immediately.
   */
  /* default */ void reconnect() {
    LOGGER.info("Reconnecting to external office process due to task timeout...");
    executor.execute(
        () -> {
          disconnect0();
          try {
            connect0();
          } catch (OfficeException ex) {
            LOGGER.error("Could not reconnect to external office process.", ex);
          }
        });
  }

  /**
   * Ensures that the connection was made to the external office process.
   *
   * @throws OfficeException If the connection could not be established with the external process.
   */
  /* default */ void ensureConnected() throws OfficeException {
    if (!connection.isConnected()) {
      connectAndWait();
    }
  }

  /**
   * Connects to the external office process and waits until the connection is established.
   *
   * @throws OfficeException If the connection could not be established with the external process.
   */
  private void connectAndWait() throws OfficeException {

    // Submit the connect task to the executor.
    LOGGER.debug("Submitting connect task...");
    final Future<Void> future = executor.submit(this::connect0);

    // Wait for completion of the task.
    try {
      LOGGER.debug("Waiting for connect task to complete...");
      future.get();
      LOGGER.debug("Connect task executed successfully.");

    } catch (ExecutionException ex) {

      // Rethrow the original (cause) exception
      if (ex.getCause() instanceof OfficeException) {
        throw (OfficeException) ex.getCause();
      }

      throw new OfficeException( // NOPMD - Only cause is relevant
          "Connect task did not complete", ex.getCause());

    } catch (InterruptedException ex) {
      LOGGER.debug("Connect task interrupted.");
      Thread.currentThread().interrupt(); // ignore/reset
      throw new OfficeException("Interruption while connecting to external office process.", ex);
    }
  }

  /**
   * Connects to the external office process.
   *
   * @return {@code null}. So it could be used in a {@link java.util.concurrent.Callable}.
   * @throws OfficeException If the connection could not be established with the external process.
   */
  @SuppressWarnings("SameReturnValue")
  private Void connect0() throws OfficeException {

    if (!connection.isConnected()) {
      LOGGER.debug("Connecting to external office process...");
      try {
        new ConnectRetryable(connection).execute(connectRetryInterval, connectTimeout);
      } catch (RetryTimeoutException ex) {
        throw new OfficeException("Could not establish connection to external process.", ex);
      }
    }

    return null;
  }

  /**
   * Disconnects from the external office process.
   *
   * @return {@code null}. So it could be used in a {@link java.util.concurrent.Callable}.
   */
  @SuppressWarnings("SameReturnValue")
  private Void disconnect0() {

    if (connection.isConnected()) {
      LOGGER.debug("Disconnecting from external office process...");
      connection.disconnect();
    }

    return null;
  }
}
