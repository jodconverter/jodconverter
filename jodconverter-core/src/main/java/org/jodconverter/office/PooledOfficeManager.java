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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PooledOfficeManager is responsible to execute tasks submitted through a {@link
 * ProcessPoolOfficeManager}. It will submit tasks to its inner {@link ManagedOfficeProcess} and
 * wait until the task is done or a configured task execution timeout is reached.
 *
 * <p>A PooledOfficeManager is also responsible to restart an office process when the maximum number
 * of tasks per process is reached.
 *
 * @see ManagedOfficeProcess
 * @see ProcessPoolOfficeManager
 */
class PooledOfficeManager implements OfficeManager {

  private static final Logger logger = LoggerFactory.getLogger(PooledOfficeManager.class);

  private final PooledOfficeManagerSettings settings;
  private final ManagedOfficeProcess managedOfficeProcess;
  private final SuspendableThreadPoolExecutor taskExecutor;

  private Future<?> currentTask;
  private final AtomicBoolean stopping = new AtomicBoolean(false);
  private final AtomicInteger taskCount = new AtomicInteger(0);

  /**
   * This connection event listener will be notified when a connection is established or closed/lost
   * to/from an office instance.
   */
  private final OfficeConnectionEventListener connectionEventListener =
      new OfficeConnectionEventListener() { // NOSONAR

        // A connection is established.
        @Override
        public void connected(final OfficeConnectionEvent event) {
          logger.trace("> OfficeConnectionEventListener.connected");

          // Reset the task count and make the task executor available.
          taskCount.set(0);
          taskExecutor.setAvailable(true);

          logger.trace("< OfficeConnectionEventListener.connected");
        }

        // A connection is closed/lost.
        @Override
        public void disconnected(final OfficeConnectionEvent event) {
          logger.trace("> OfficeConnectionEventListener.disconnected");

          // Make the task executor unavailable.
          taskExecutor.setAvailable(false);

          // When it comes from an expected behavior (we have put
          // the field to true before calling a function), just reset
          // the stopping value to false. When we didn't expect the
          // disconnection, we must restart the office process, canceling
          // any task that may be running.
          if (!stopping.compareAndSet(true, false)) {

            // Here, we didn't expect this disconnection. We must restart
            // the office process, canceling any task that may be running.
            logger.warn("Connection lost unexpectedly; attempting restart");
            if (currentTask != null) {
              currentTask.cancel(true);
            }
            managedOfficeProcess.restartDueToLostConnection();
          }

          logger.trace("< OfficeConnectionEventListener.disconnected");
        }
      };

  /**
   * Creates a new instance of the class with the specified settings.
   *
   * @param settings the settings used to initialize the instance.
   */
  public PooledOfficeManager(final PooledOfficeManagerSettings settings) {

    this.settings = settings;
    managedOfficeProcess = new ManagedOfficeProcess(settings);
    taskExecutor = new SuspendableThreadPoolExecutor(new NamedThreadFactory("OfficeTaskThread"));

    // Listen to any connection events to the office instance.
    managedOfficeProcess.getConnection().addConnectionEventListener(connectionEventListener);
  }

  @Override
  public void execute(final OfficeTask task) throws OfficeException {

    // Create the command to be executed
    final Callable<Void> command = new Callable<Void>() { // NOSONAR

          @Override
          public Void call() throws Exception {

            // First check if the office process must be restarted
            final int count = taskCount.getAndIncrement();
            if (settings.getMaxTasksPerProcess() > 0 && count == settings.getMaxTasksPerProcess()) {
              logger.info(
                  "Reached limit of {} maximum tasks per process; restarting...",
                  settings.getMaxTasksPerProcess());

              // The executor is no longer available
              taskExecutor.setAvailable(false);

              // Indicates that the disconnection to follow is expected
              stopping.set(true);

              // Restart the office instance
              managedOfficeProcess.restartAndWait();

              // taskCount will be 0 rather than 1 at this point, so fix this.
              taskCount.getAndIncrement();
            }

            // Execute the task
            task.execute(managedOfficeProcess.getConnection());

            return null;
          }
        };

    // Submit the task to the executor
    currentTask = taskExecutor.submit(command);

    // Wait for completion of the task, (maximum wait time is the
    // configured task execution timeout)
    try {
      logger.debug("Waiting for task to complete...");
      currentTask.get(settings.getTaskExecutionTimeout(), TimeUnit.MILLISECONDS);
      logger.debug("Task executed successfully");

    } catch (TimeoutException timeoutEx) {

      // The task did not complete withing the configured timeout...
      managedOfficeProcess.restartDueToTaskTimeout();
      throw new OfficeException("task did not complete within timeout", timeoutEx);

    } catch (ExecutionException executionEx) { // NOSONAR

      // Rethrow the original (cause) exception
      if (executionEx.getCause() instanceof OfficeException) {
        throw (OfficeException) executionEx.getCause();
      }
      throw new OfficeException("Task failed", executionEx.getCause());

    } catch (Exception ex) {

      // Unexpected exception
      throw new OfficeException("Task failed", ex);

    } finally {
      currentTask = null;
    }
  }

  /**
   * Gets the number of tasks executed by the current office process so far.
   *
   * @return the number of tasks executed by the current office process.
   */
  public int getCurrentTaskCount() {
    return taskCount.get();
  }

  /**
   * Gets the ManagedOfficeProcess of this PooledOfficeManager.
   *
   * @return the {@link ManagedOfficeProcess} of this PooledOfficeManager.
   */
  public ManagedOfficeProcess getManagedOfficeProcess() {
    return managedOfficeProcess;
  }

  @Override
  public boolean isRunning() {

    return managedOfficeProcess.isConnected();
  }

  @Override
  public void start() throws OfficeException {

    managedOfficeProcess.startAndWait();
  }

  @Override
  public void stop() throws OfficeException {

    try {
      taskExecutor.setAvailable(false);
      stopping.set(true);
      taskExecutor.shutdownNow();
    } finally {
      managedOfficeProcess.stopAndWait();
    }
  }
}
