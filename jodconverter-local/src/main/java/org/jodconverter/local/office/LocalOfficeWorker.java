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
 * An {@link LocalOfficeWorker} executes the tasks submitted through a {@link
 * org.jodconverter.local.office.LocalOfficeManager} with its own office process, which it drives
 * with its inner {@link LocalOfficeProcessManager}.
 *
 * <p>It is ready when it is connected to its office process and that process has not executed its
 * maximum number of tasks. When it is not ready anymore, the pool restarts it before giving it
 * another task.
 *
 * @see org.jodconverter.local.office.LocalOfficeManager
 * @see LocalOfficeProcessManager
 */
class LocalOfficeWorker implements OfficeWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalOfficeWorker.class);

  private final int maxTasksPerProcess;
  private final LocalOfficeProcessManager officeProcessManager;

  // Only used by the thread of this worker.
  private int taskCount;
  private boolean passwordInteraction;

  // Set by the thread that aborts this worker.
  private volatile boolean aborted;

  // We don't have to interrupt a task whose password interaction caused the disconnection. A
  // PasswordProtectedException has already been thrown or will be thrown by the task.
  private final RunningTask runningTask =
      new RunningTask(task -> !hasPasswordInteractionRequest(task));

  /**
   * Creates a new worker with the specified configuration.
   *
   * @param maxTasksPerProcess The maximum number of tasks an office process can execute before
   *     restarting; 0 means no limit.
   * @param officeProcessManager The office process manager.
   */
  /* default */ LocalOfficeWorker(
      final int maxTasksPerProcess, final LocalOfficeProcessManager officeProcessManager) {

    this.maxTasksPerProcess = maxTasksPerProcess;
    this.officeProcessManager = officeProcessManager;

    // Listen to the connection to the office instance, to be notified when it is closed or lost.
    officeProcessManager
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

  private static boolean hasPasswordInteractionRequest(final OfficeTask task) {
    return task instanceof PasswordProtectedExceptionSupportTask supportTask
        && supportTask.hasPasswordInteractionRequest();
  }

  @Override
  public void start() throws OfficeException {

    // Start the office process and connect to it.
    officeProcessManager.start();
    taskCount = 0;
  }

  @Override
  public void restart() throws OfficeException {

    // The office process is still usable when it only reached its maximum number of tasks, or
    // when it was only disconnected by a password interaction: it is restarted with the same
    // instance profile directory, which is faster. Otherwise (lost connection, crash, aborted
    // task, failed start), whatever is left of it is cleaned up first.
    final var usable =
        !aborted && (passwordInteraction || officeProcessManager.getConnection().isConnected());
    aborted = false;
    passwordInteraction = false;

    if (usable) {
      officeProcessManager.restart();
    } else {
      officeProcessManager.restartDueToLostConnection();
    }
    taskCount = 0;
  }

  @Override
  public boolean isReady() {

    if (!officeProcessManager.getConnection().isConnected()) {
      return false;
    }

    // The office process survived the last task.
    passwordInteraction = false;
    return maxTasksPerProcess <= 0 || taskCount < maxTasksPerProcess;
  }

  @Override
  public void execute(final @NonNull OfficeTask task) throws OfficeException {
    LOGGER.debug("Executing task: {}", task);

    runningTask.begin(task);
    try {
      task.execute(officeProcessManager.getConnection());
    } finally {
      runningTask.end();
      // We have to check here for password protection for LibreOffice 24+ since
      // a password interaction causes a disconnection when the password is not
      // provided.
      // https://github.com/jodconverter/jodconverter/issues/423#issue-3000441635
      passwordInteraction = hasPasswordInteractionRequest(task);
    }

    LOGGER.debug("Task executed successfully: {}", task);

    // Now check if the office process must be restarted.
    taskCount++;
    if (taskCount == maxTasksPerProcess) {
      LOGGER.info(
          "Reached limit of {} maximum tasks per process; restarting...", maxTasksPerProcess);
    } else {
      LOGGER.debug(
          "Limit of {} maximum tasks per process not reached yet. Task count is {}",
          maxTasksPerProcess,
          taskCount);
    }
  }

  @Override
  public boolean isLost() {

    // A password request makes the office drop the connection too, and an aborted task ends with
    // a killed process: in both cases the task is the reason, and must not be executed again.
    return !aborted && !passwordInteraction && !officeProcessManager.getConnection().isConnected();
  }

  @Override
  public void abort() {

    // Killing the office process ends whatever this worker is blocked in. What is left of the
    // process is cleaned up by the restart, or the stop, that follows.
    aborted = true;
    officeProcessManager.kill();
  }

  @Override
  public void stop() {

    officeProcessManager.stop();
  }
}
