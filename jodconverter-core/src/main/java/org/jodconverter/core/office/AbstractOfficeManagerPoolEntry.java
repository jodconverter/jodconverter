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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.task.OfficeTask;

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
public abstract class AbstractOfficeManagerPoolEntry implements OfficeManager {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractOfficeManagerPoolEntry.class);

  // The default timeout when processing
  private static final long DEFAULT_TASK_EXECUTION_TIMEOUT = 120_000L; // 2 minutes

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
  public AbstractOfficeManagerPoolEntry(@Nullable final Long taskExecutionTimeout) {

    this.taskExecutionTimeout =
        taskExecutionTimeout == null ? DEFAULT_TASK_EXECUTION_TIMEOUT : taskExecutionTimeout;
    taskExecutor =
        new SuspendableThreadPoolExecutor(new NamedThreadFactory("jodconverter-poolentry"));
  }

  @Override
  public final void execute(@NonNull final OfficeTask task) throws OfficeException {

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
      LOGGER.debug("Waiting for task to complete: {}", task);
      currentFuture.get(taskExecutionTimeout, TimeUnit.MILLISECONDS);
      LOGGER.debug("Task executed successfully: {}", task);

    } catch (TimeoutException timeoutEx) {

      // The task did not complete within the configured timeout...
      handleExecuteTimeoutException(timeoutEx);
      throw new OfficeException("Task did not complete within timeout: " + task, timeoutEx);

    } catch (ExecutionException executionEx) {

      // Rethrow the original (cause) exception
      if (executionEx.getCause() instanceof OfficeException) {
        throw (OfficeException) executionEx.getCause();
      }
      throw new OfficeException( // NOPMD - Only cause is relevant
          "Task failed: " + task, executionEx.getCause());

    } catch (Exception ex) {

      // Unexpected exception
      throw new OfficeException("Task failed: " + task, ex);

    } finally {
      currentFuture = null;
    }
  }

  /**
   * Performs the execution of a task.
   *
   * @param task The task to execute.
   * @throws Exception If any errors occurs during the conversion.
   */
  protected abstract void doExecute(@NonNull final OfficeTask task) throws Exception;

  /**
   * Handles a timeout exception raised while executing a task.
   *
   * @param timeoutEx the exception thrown.
   */
  protected void handleExecuteTimeoutException(@NonNull final TimeoutException timeoutEx) {

    // The default behavior is to do nothing
    LOGGER.debug("Handling task execution timeout.", timeoutEx);
  }

  @Override
  public boolean isRunning() {
    return !taskExecutor.isShutdown();
  }

  @Override
  public final void start() {

    // We cannot reuse an executor that has been shutdown.
    if (taskExecutor.isShutdown()) {
      throw new IllegalStateException("This office manager (pool entry) has been shutdown.");
    }

    doStart();
  }

  @Override
  public final void stop() {

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

  /** Allow subclasses to perform operation when the office manager is started. */
  protected abstract void doStart();

  /** Allow subclasses to perform operation when the office manager is stopped. */
  protected abstract void doStop();
}
