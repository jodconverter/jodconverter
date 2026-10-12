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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeWorker;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.local.task.PasswordProtectedExceptionSupportTask;

/**
 * An {@link AttachedOfficeWorker} executes the tasks submitted through an {@link
 * AttachedOfficeManager} with its connection to an office process it does not own, which it drives
 * with its inner {@link AttachedOfficeConnectionManager}.
 *
 * <p>It is ready when it is connected and its connection has not executed its maximum number of
 * tasks, or when it was asked not to connect on start and has not connected yet: it then connects
 * when it executes its first task. When it is not ready anymore, the pool reconnects it before
 * giving it another task.
 *
 * @see AttachedOfficeManager
 * @see AttachedOfficeConnectionManager
 */
class AttachedOfficeWorker implements OfficeWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(AttachedOfficeWorker.class);

  private final boolean connectOnStart;
  private final int maxTasksPerConnection;
  private final AttachedOfficeConnectionManager connectionManager;
  private final RunningTask runningTask = new RunningTask(task -> true);

  // Only used by the thread of this worker.
  private int taskCount;
  private boolean connectOnFirstTask;
  private boolean passwordInteraction;

  /**
   * Creates a new worker for the specified connection with the specified configuration.
   *
   * @param connectOnStart Should a connection be attempted on start? If {@code false}, a connection
   *     will only be attempted the first time an {@link OfficeTask} is executed.
   * @param maxTasksPerConnection The maximum number of tasks a connection can execute before
   *     reconnecting; 0 means no limit.
   * @param connectionManager The connection manager.
   */
  /* default */ AttachedOfficeWorker(
      final boolean connectOnStart,
      final int maxTasksPerConnection,
      final AttachedOfficeConnectionManager connectionManager) {

    this.connectOnStart = connectOnStart;
    this.maxTasksPerConnection = maxTasksPerConnection;
    this.connectionManager = connectionManager;

    // Listen to the connection to the office instance, to be notified when it is closed or lost.
    connectionManager
        .getConnection()
        .addConnectionEventListener(
            new OfficeConnectionEventListener() {

              @Override
              public void connected(final OfficeConnectionEvent event) {
                // Nothing to do: the worker is ready as long as it is connected.
              }

              @Override
              public void disconnected(final OfficeConnectionEvent event) {
                runningTask.interrupt();
              }
            });
  }

  @Override
  public void start() throws OfficeException {

    // Connect on start only if required.
    if (connectOnStart) {
      connectionManager.connect();
    } else {
      connectOnFirstTask = true;
    }
    taskCount = 0;
  }

  @Override
  public void restart() throws OfficeException {

    connectOnFirstTask = false;
    connectionManager.reconnect();
    taskCount = 0;
  }

  @Override
  public boolean isReady() {

    return (connectOnFirstTask || connectionManager.getConnection().isConnected())
        && (maxTasksPerConnection <= 0 || taskCount < maxTasksPerConnection);
  }

  @Override
  public void execute(final @NonNull OfficeTask task) throws OfficeException {
    LOGGER.debug("Executing task: {}", task);

    // Ensure we are connected.
    connectionManager.connect();
    connectOnFirstTask = false;

    // Execute the task.
    runningTask.begin(task);
    try {
      task.execute(connectionManager.getConnection());
    } finally {
      runningTask.end();
      // A password request makes a recent office drop the connection: the task is the reason.
      passwordInteraction =
          task instanceof PasswordProtectedExceptionSupportTask supportTask
              && supportTask.hasPasswordInteractionRequest();
    }

    LOGGER.debug("Task executed successfully: {}", task);

    // Now check if we must reconnect to the external process.
    taskCount++;
    if (taskCount == maxTasksPerConnection) {
      LOGGER.info(
          "Reached limit of {} maximum tasks per connection; reconnecting...",
          maxTasksPerConnection);
    }
  }

  @Override
  public boolean isLost() {
    return !passwordInteraction && !connectionManager.getConnection().isConnected();
  }

  @Override
  public void abort() {

    // Closing the connection ends whatever this worker is blocked in.
    connectionManager.disconnect();
  }

  @Override
  public void stop() {

    connectionManager.disconnect();
  }
}
