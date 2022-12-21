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

package org.jodconverter.core.office;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.task.OfficeTask;

/**
 * Base class for all office manager pool entry implementations.
 *
 * <p>An important note here is to keep in mind that subclasses are responsible to manage the
 * availability of the task executor. This abstract class never set the availability to true. Only
 * when the manager is stopped that the availability is set to false.
 *
 * @see OfficeManager
 * @see AbstractOfficeManagerPool
 */
public abstract class AbstractOfficeManagerPoolEntry implements OfficeManager {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractOfficeManagerPoolEntry.class);

  private final long taskExecutionTimeout;
  private final SuspendableThreadPoolExecutor taskExecutor;
  private Future<?> currentFuture;

  /**
   * Initializes a new pool entry with the specified configuration.
   *
   * @param taskExecutionTimeout The maximum time allowed to process a task. If the processing time
   *     of a task is longer than this timeout, this task will be aborted and the next task is
   *     processed.
   */
  protected AbstractOfficeManagerPoolEntry(final long taskExecutionTimeout) {

    this.taskExecutionTimeout = taskExecutionTimeout;
    taskExecutor =
        new SuspendableThreadPoolExecutor(new NamedThreadFactory("jodconverter-poolentry"));
  }

  @Override
  public final void execute(final @NonNull OfficeTask task) throws OfficeException {

    // No need to check if the manager is running here.
    // This check is already done in the pool.

    // TODO: Maybe we should check if the taskExecutor was made available
    // at least once, meaning that the entry has been started.

    // Submit the task to the executor
    currentFuture =
        taskExecutor.submit(
            () -> {
              doExecute(task);
              return null;
            });

    // Wait for completion of the task.
    waitTaskCompletion(task);
  }

  private void waitTaskCompletion(final OfficeTask task) throws OfficeException {

    // Wait for completion of the task, (maximum wait time is the configured task execution
    // timeout).
    try {
      LOGGER.debug("Waiting {} ms for task to complete: {}", taskExecutionTimeout, task);
      currentFuture.get(taskExecutionTimeout, TimeUnit.MILLISECONDS);
      LOGGER.debug("Task executed successfully: {}", task);

    } catch (CancellationException ex) {

      // The task was cancelled...
      throw new OfficeException(String.format("Task was cancelled: %s", task), ex);

    } catch (ExecutionException ex) {

      // An error occurred while executing the task...
      throw handleTaskExecutionException(task, ex);

    } catch (InterruptedException ex) {

      // The task was interrupted...
      Thread.currentThread().interrupt();
      throw new OfficeException(
          String.format("Task was interrupted while executing: %s", task), ex);

    } catch (TimeoutException ex) {

      // The task did not complete within the configured timeout...
      handleExecuteTimeoutException(ex);
      throw new OfficeException(
          String.format(
              "Task did not complete within timeout (%s ms): %s", taskExecutionTimeout, task),
          ex);

    } finally {
      currentFuture = null;
    }
  }

  private OfficeException handleTaskExecutionException(
      final OfficeTask task, final ExecutionException executionException) {

    // Rethrow the original (cause) exception
    if (executionException.getCause() instanceof OfficeException) {
      return (OfficeException) executionException.getCause();
    }

    return new OfficeException(
        String.format("Task did not complete: %s", task), executionException.getCause());
  }

  /**
   * Performs the execution of a task.
   *
   * @param task The task to execute.
   * @throws OfficeException If any errors occurs during the conversion.
   */
  protected abstract void doExecute(@NonNull OfficeTask task) throws OfficeException;

  /**
   * Handles a timeout exception raised while executing a task.
   *
   * @param timeoutEx the exception thrown.
   */
  protected void handleExecuteTimeoutException(final @NonNull TimeoutException timeoutEx) {

    // The default behavior is to do nothing
    LOGGER.debug("Handling task execution timeout.", timeoutEx);
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

  /** Cancels the current running task, if any. Do nothing if there is no current running task. */
  protected void cancelTask() {
    if (currentFuture != null) {
      LOGGER.debug("Cancelling current task...");
      currentFuture.cancel(true);
    }
  }

  /**
   * Sets the availability of this manager entry.
   *
   * @param available {@code true} if the manager is available to execute tasks, {@code false}
   *     otherwise.
   */
  protected void setAvailable(final boolean available) {
    taskExecutor.setAvailable(available);
  }

  /**
   * Allow subclasses to perform operation when the office manager is started.
   *
   * @throws OfficeException If an error occurred while starting the manager.
   */
  protected abstract void doStart() throws OfficeException;

  /**
   * Allow subclasses to perform operation when the office manager is stopped.
   *
   * @throws OfficeException If an error occurred while stopping the manager.
   */
  protected abstract void doStop() throws OfficeException;
}
