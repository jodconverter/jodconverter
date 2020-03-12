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

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.AbstractOfficeManagerPoolEntry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.local.process.ProcessManager;

/**
 * An {@link OfficeProcessManagerPoolEntry} is responsible to execute tasks submitted through a
 * {@link org.jodconverter.local.office.LocalOfficeManager}. It will submit tasks to its inner
 * {@link org.jodconverter.local.office.OfficeProcessManager} and wait until the task is done or a
 * configured task execution timeout is reached.
 *
 * <p>An {@link OfficeProcessManagerPoolEntry} is also responsible to restart an office process when
 * the maximum number of tasks per process is reached.
 *
 * @see org.jodconverter.local.office.OfficeProcessManager
 * @see org.jodconverter.local.office.LocalOfficeManager
 */
class OfficeProcessManagerPoolEntry extends AbstractOfficeManagerPoolEntry {

  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeProcessManagerPoolEntry.class);

  // The default maximum number of tasks an office process can execute before restarting.
  private static final int DEFAULT_MAX_TASKS_PER_PROCESS = 200;

  private final int maxTasksPerProcess;
  private final OfficeProcessManager officeProcessManager;
  private final AtomicInteger taskCount = new AtomicInteger(0);
  private final AtomicBoolean disconnectExpected = new AtomicBoolean(false);

  /**
   * Creates a new pool entry for the specified office URL with the specified configuration.
   *
   * @param officeUrl The URL for which the office process is created.
   * @param officeHome The home directory of the office installation.
   * @param workingDir The working directory to set to the office process.
   * @param processManager The process manager to use to deal with the office process.
   * @param runAsArgs The sudo arguments that will be used with unix commands.
   * @param templateProfileDir The directory to copy to the temporary office profile directories to
   *     be created.
   * @param killExistingProcess Indicates whether an existing office process is killed when starting
   *     a new office process for the same connection string.
   * @param processTimeout The timeout, in milliseconds, when trying to execute an office process
   *     call (start/terminate).
   * @param processRetryInterval The delay, in milliseconds, between each try when trying to execute
   *     an office process call (start/terminate).
   * @param taskExecutionTimeout The maximum time allowed to process a task. If the processing time
   *     of a task is longer than this timeout, this task will be aborted and the next task is
   *     processed.
   * @param maxTasksPerProcess The maximum number of tasks an office process can execute before
   *     restarting.
   * @param disableOpengl Indicates whether OpenGL must be disabled when starting a new office
   *     process. Nothing will be done if OpenGL is already disabled according to the user profile
   *     used with the office process. If the options is changed, then office must be restarted.
   */
  /* default */ OfficeProcessManagerPoolEntry(
      final OfficeUrl officeUrl,
      final File officeHome,
      final File workingDir,
      final ProcessManager processManager,
      final List<String> runAsArgs,
      final File templateProfileDir,
      final Boolean killExistingProcess,
      final Long processTimeout,
      final Long processRetryInterval,
      final Long taskExecutionTimeout,
      final Integer maxTasksPerProcess,
      final Boolean disableOpengl) {
    super(taskExecutionTimeout);

    // Create the process manager that will deal with the office instance
    officeProcessManager =
        new OfficeProcessManager(
            officeUrl,
            officeHome,
            workingDir,
            processManager,
            runAsArgs,
            templateProfileDir,
            killExistingProcess,
            processTimeout,
            processRetryInterval,
            disableOpengl);

    this.maxTasksPerProcess =
        maxTasksPerProcess == null ? DEFAULT_MAX_TASKS_PER_PROCESS : maxTasksPerProcess;

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

              // Here, we didn't expect this disconnection. We must restart
              // the office process, canceling any task that may be running.
              LOGGER.warn("Connection lost unexpectedly; attempting restart");
              cancelTask();
              officeProcessManager.restartDueToLostConnection();
            }
          }
        };

    // Listen to any connection events to the office instance.
    officeProcessManager.getConnection().addConnectionEventListener(connectionEventListener);
  }

  @Override
  public void doExecute(@NonNull final OfficeTask task) throws OfficeException {

    // Execute the task.
    task.execute(officeProcessManager.getConnection());

    // Increment the task count
    final int count = taskCount.incrementAndGet();

    // Now check if the office process must be restarted.
    if (maxTasksPerProcess > 0 && count == maxTasksPerProcess) {

      LOGGER.info(
          "Reached limit of {} maximum tasks per process; restarting...", maxTasksPerProcess);
      restart();
    }
  }

  @Override
  protected void handleExecuteTimeoutException(@NonNull final TimeoutException timeoutEx) {

    // Is the the task did not complete within the configured timeout, we must restart
    officeProcessManager.restartDueToTaskTimeout();
  }

  @Override
  public boolean isRunning() {

    return super.isRunning() && officeProcessManager.getConnection().isConnected();
  }

  @Override
  public void doStart() {

    // Start the office process and connect to it.
    officeProcessManager.start();
  }

  @Override
  public void doStop() {

    // The manager is no longer available
    setAvailable(false);

    // From here on, any disconnection from an office process is expected.
    disconnectExpected.set(true);

    // Now we can stopped the running office process
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
