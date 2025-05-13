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
import java.util.concurrent.atomic.AtomicReference;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.AbstractOfficeManagerPoolEntry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.local.task.PasswordProtectedExceptionSupportTask;

/**
 * An {@link LocalOfficeManagerPoolEntry} is responsible to execute tasks submitted through a {@link
 * org.jodconverter.local.office.LocalOfficeManager}. It will submit tasks to its inner {@link
 * LocalOfficeProcessManager} and wait until the task is done or a configured task execution timeout
 * is reached.
 *
 * <p>An {@link LocalOfficeManagerPoolEntry} is also responsible to restart an office process when
 * the maximum number of tasks per process is reached.
 *
 * @see org.jodconverter.local.office.LocalOfficeManager
 * @see LocalOfficeProcessManager
 */
class LocalOfficeManagerPoolEntry extends AbstractOfficeManagerPoolEntry {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalOfficeManagerPoolEntry.class);

  private final int maxTasksPerProcess;
  private final LocalOfficeProcessManager officeProcessManager;
  private final AtomicInteger taskCount = new AtomicInteger(0);
  private final AtomicBoolean disconnectExpected = new AtomicBoolean(false);
  private final AtomicReference<PasswordProtectedExceptionSupportTask>
      passwordProtectedExceptionSupportTask = new AtomicReference<>();

  /**
   * Creates a new pool entry for the specified office URL with the specified configuration.
   *
   * @param maxTasksPerProcess The maximum number of tasks an office process can execute before
   *     restarting.
   * @param taskExecutionTimeout The maximum time allowed to process a task. If the processing time
   *     of a task is longer than this timeout, this task will be aborted and the next task is
   *     processed.
   * @param officeProcessManager The office process manager.
   */
  /* default */ LocalOfficeManagerPoolEntry(
      final int maxTasksPerProcess,
      final long taskExecutionTimeout,
      final LocalOfficeProcessManager officeProcessManager) {
    super(taskExecutionTimeout);

    this.officeProcessManager = officeProcessManager;
    this.maxTasksPerProcess = maxTasksPerProcess;

    // This connection event listener will be notified when a connection is established or
    // closed/lost to/from an office instance.
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

              // We have to check here for password protection for LibreOffice 24+ since
              // a password interaction causes a disconnection when the password is not
              // provided.
              // https://github.com/jodconverter/jodconverter/issues/423#issue-3000441635
              final PasswordProtectedExceptionSupportTask task =
                  passwordProtectedExceptionSupportTask.getAndSet(null);
              if (task != null && task.hasPasswordInteractionRequest()) {

                // We don't have to cancel the task here. A PasswordProtectedException has
                // already been thrown or will be thrown in another thread, causing the task
                // to fail with an ExecutionException, which will contain the
                // PasswordProtectedException.
                officeProcessManager.restart();

              } else {

                // Here, we didn't expect this disconnection. We must restart
                // the office process, canceling any task that may be running.
                LOGGER.warn("Connection lost unexpectedly; attempting restart");
                cancelTask();
                officeProcessManager.restartDueToLostConnection();
              }
            }
          }
        };

    // Listen to any connection events to the office instance.
    officeProcessManager.getConnection().addConnectionEventListener(connectionEventListener);
  }

  @Override
  public void doExecute(final @NonNull OfficeTask task) throws OfficeException {
    LOGGER.debug("Executing task: {}", task);

    // Keep the task to let us know if we are processing a task that supports
    // throwing PasswordProtectedException.
    passwordProtectedExceptionSupportTask.set(null);
    if (task instanceof PasswordProtectedExceptionSupportTask) {
      passwordProtectedExceptionSupportTask.set((PasswordProtectedExceptionSupportTask) task);
    }

    // Execute the task.
    task.execute(officeProcessManager.getConnection());

    LOGGER.debug("Task executed successfully: {}", task);

    // Increment the task count
    final int count = taskCount.incrementAndGet();

    // Now check if the office process must be restarted.
    if (count == maxTasksPerProcess) {

      LOGGER.info(
          "Reached limit of {} maximum tasks per process; restarting...", maxTasksPerProcess);
      restart();
    } else {
      LOGGER.debug(
          "Limit of {} maximum tasks per process not reached yet. Task count is {}",
          maxTasksPerProcess,
          count);
    }
  }

  @Override
  protected void handleExecuteTimeoutException(final @NonNull TimeoutException timeoutEx) {

    // If the task did not complete within the configured timeout, we must restart.
    officeProcessManager.restartDueToTaskTimeout();
  }

  @Override
  public boolean isRunning() {

    return super.isRunning() && officeProcessManager.getConnection().isConnected();
  }

  @Override
  public void doStart() throws OfficeException {

    // Start the office process and connect to it.
    officeProcessManager.start();
  }

  @Override
  public void doStop() throws OfficeException {

    // The manager is no longer available
    setAvailable(false);

    // From here on, any disconnection from an office process is expected.
    disconnectExpected.set(true);

    // Now we can stop the running office process
    officeProcessManager.stop();
  }

  private void restart() {

    // The manager is no longer available
    setAvailable(false);

    // Indicates that the disconnection to follow is expected
    disconnectExpected.set(true);

    // Restart the office instance
    officeProcessManager.restart();
  }
}
