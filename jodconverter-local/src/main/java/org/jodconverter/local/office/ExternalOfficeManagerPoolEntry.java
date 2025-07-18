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

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.AbstractOfficeManagerPoolEntry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.OfficeTask;

/**
 * An {@link ExternalOfficeManagerPoolEntry} is responsible to execute tasks submitted through a
 * {@link ExternalOfficeManager}. It will execute tasks and wait until the task is done or a
 * configured task execution timeout is reached.
 *
 * @see ExternalOfficeManager
 */
class ExternalOfficeManagerPoolEntry extends AbstractOfficeManagerPoolEntry {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ExternalOfficeManagerPoolEntry.class);

  private final boolean connectOnStart;
  private final int maxTasksPerConnection;
  private final ExternalOfficeConnectionManager connectionManager;
  private final AtomicInteger taskCount = new AtomicInteger(0);
  private final AtomicBoolean disconnectExpected = new AtomicBoolean(false);

  /**
   * Creates a new pool entry for the specified office URL with the specified configuration.
   *
   * @param connectOnStart Should a connection be attempted on start? If {@code false}, a connection
   *     will only be attempted the first time an {@link org.jodconverter.core.task.OfficeTask} is
   *     executed.
   * @param maxTasksPerConnection The maximum number of tasks an office process can execute before
   *     restarting.
   * @param taskExecutionTimeout The maximum time allowed to process a task. If the processing time
   *     of a task is longer than this timeout, this task will be aborted and the next task is
   *     processed.
   * @param connectionManager The connection manager.
   */
  /* default */ ExternalOfficeManagerPoolEntry(
      final boolean connectOnStart,
      final int maxTasksPerConnection,
      final long taskExecutionTimeout,
      final ExternalOfficeConnectionManager connectionManager) {
    super(taskExecutionTimeout);

    this.connectOnStart = connectOnStart;
    this.maxTasksPerConnection = maxTasksPerConnection;
    this.connectionManager = connectionManager;

    // This connection event listener will be notified when a connection
    // is established or closed/lost to/from an office instance.
    final OfficeConnectionEventListener connectionEventListener =
        new OfficeConnectionEventListener() {

          // A connection is established.
          @Override
          public void connected(final OfficeConnectionEvent event) {

            // Reset the task count and make the manager available.
            taskCount.set(0);
            setAvailable(true);
          }

          // A connection is closed/lost.
          @Override
          public void disconnected(final OfficeConnectionEvent event) {

            // Make the manager unavailable.
            setAvailable(false);

            // When it comes from an expected behavior (we have put
            // the field to true before calling a function), just reset
            // the disconnectExpected value to false. When we didn't expect
            // the disconnection, we must restart the office process, which
            // will cancel any task that may be running.
            if (!disconnectExpected.compareAndSet(true, false)) {

              // Here, we didn't expect this disconnection. We must reconnect
              // to the office process, canceling any task that may be running.
              LOGGER.warn("Connection lost unexpectedly; attempting reconnect");
              cancelTask();
              connectionManager.reconnect();
            }
          }
        };

    // Listen to any connection events to the office instance.
    connectionManager.getConnection().addConnectionEventListener(connectionEventListener);
  }

  @Override
  protected void doExecute(final @NonNull OfficeTask task) throws OfficeException {

    // Ensure we are connected.
    connectionManager.ensureConnected();

    // Execute the task.
    task.execute(connectionManager.getConnection());

    // Increment the task count.
    final int count = taskCount.incrementAndGet();

    // Now check if we must reconnect to the external process.
    if (count == maxTasksPerConnection) {
      LOGGER.info(
          "Reached limit of {} maximum tasks per connection; reconnecting...",
          maxTasksPerConnection);
      reconnect();
    }
  }

  @Override
  protected void handleExecuteTimeoutException(final @NonNull TimeoutException timeoutEx) {

    // If the task did not complete within the configured timeout, we must reconnect
    reconnect();
  }

  @Override
  public boolean isRunning() {
    return super.isRunning() && connectionManager.getConnection().isConnected();
  }

  @Override
  public void doStart() throws OfficeException {

    // Connect on start only if required.
    if (connectOnStart) {
      connectionManager.connect();
    }
  }

  @Override
  public void doStop() throws OfficeException {

    // The manager is no longer available
    setAvailable(false);

    // Indicates that the disconnection to follow is expected
    disconnectExpected.set(true);

    connectionManager.disconnect();
  }

  private void reconnect() {

    // The manager is no longer available
    setAvailable(false);

    // Indicates that the disconnection to follow is expected
    disconnectExpected.set(true);

    // Reconnect to the external office process
    connectionManager.reconnect();
  }
}
