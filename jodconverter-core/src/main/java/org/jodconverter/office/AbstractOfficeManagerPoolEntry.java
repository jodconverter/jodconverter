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

package org.jodconverter.office;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.task.OfficeTask;

/**
 * Base class for all office manager pool entry implementations.
 *
 * <p>An important note here is to keep in mind that sub classes are responsible to manage the
 * availability of the task executor. This abstract class never set the availability to true. Only
 * when the manager is stopped that the availability is set to false.
 *
 * @see OfficeManager
 * @see AbstractOfficeManagerPool
 */
abstract class AbstractOfficeManagerPoolEntry implements OfficeManager {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractOfficeManagerPoolEntry.class);

  protected final OfficeManagerPoolEntryConfig config;
  protected final SuspendableThreadPoolExecutor taskExecutor;
  protected Future<?> currentFuture;

  /**
   * Initializes a new pool entry with the specified configuration.
   *
   * @param config The entry configuration.
   */
  public AbstractOfficeManagerPoolEntry(final OfficeManagerPoolEntryConfig config) {

    this.config = config;
    taskExecutor =
        new SuspendableThreadPoolExecutor(new NamedThreadFactory("OfficeManagerPoolEntry"));
  }

  @Override
  public final void execute(final OfficeTask task) throws OfficeException {

    // No need to check if the manager if running here.
    // This check is already done in the pool

    // Submit the task to the executor
    currentFuture =
        taskExecutor.submit(
            (Callable<Void>)
                () -> {
                  doExecute(task);
                  return null;
                });

    // Wait for completion of the task, (maximum wait time is the
    // configured task execution timeout)
    try {
      LOGGER.debug("Waiting for task to complete...");
      currentFuture.get(config.getTaskExecutionTimeout(), TimeUnit.MILLISECONDS);
      LOGGER.debug("Task executed successfully");

    } catch (TimeoutException timeoutEx) {

      // The task did not complete within the configured timeout...
      handleExecuteTimeoutException(timeoutEx);
      throw new OfficeException("Task did not complete within timeout", timeoutEx);

    } catch (ExecutionException executionEx) {

      // Rethrow the original (cause) exception
      if (executionEx.getCause() instanceof OfficeException) {
        throw (OfficeException) executionEx.getCause();
      }
      throw new OfficeException("Task failed", executionEx.getCause());

    } catch (Exception ex) {

      // Unexpected exception
      throw new OfficeException("Task failed", ex);

    } finally {
      currentFuture = null;
    }
  }

  /**
   * Performs the execution of a task.
   *
   * @throws Exception If any errors occurs during the conversion.
   */
  protected abstract void doExecute(final OfficeTask task) throws Exception;

  /**
   * Handles a timeout exception raised while executing a task.
   *
   * @param timeoutEx the exception thrown.
   */
  protected void handleExecuteTimeoutException(final TimeoutException timeoutEx) {

    // The default behavior is to do nothing
    LOGGER.debug("Handleling task execution timeout...");
  }

  @Override
  public boolean isRunning() {
    return !taskExecutor.isShutdown();
  }

  @Override
  public final void start() throws OfficeException {

    // We cannot reuse an executor that has been shutdown.
    if (taskExecutor.isShutdown()) {
      throw new IllegalStateException("This office manager (pool entry) has been shutdown.");
    }

    doStart();
  }

  /**
   * Allow subclasses to perform operation when the office manager is started.
   *
   * @throws OfficeException If an error occurs while starting the manager.
   */
  protected abstract void doStart() throws OfficeException;

  @Override
  public final void stop() throws OfficeException {

    // While stopping, the executor should not be available to any
    // new task that could be submitted.
    taskExecutor.setAvailable(false);

    // Shutdown the executor. Is a task is running, it will be interrupted.
    taskExecutor.shutdownNow();

    // Execute the subclass implementation
    doStop();
  }

  /**
   * Allow subclasses to perform operation when the office manager is stopped.
   *
   * @throws OfficeException If an error occurs while stopping the manager.
   */
  protected abstract void doStop() throws OfficeException;
}
