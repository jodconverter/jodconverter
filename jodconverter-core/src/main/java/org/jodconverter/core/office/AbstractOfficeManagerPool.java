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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.task.OfficeTask;

/**
 * An AbstractOfficeManagerPool is responsible to maintain a pool of {@link
 * org.jodconverter.core.office.AbstractOfficeManagerPoolEntry} that will be used to execute {@link
 * org.jodconverter.core.task.OfficeTask}. The pool will use the first available {@link
 * org.jodconverter.core.office.AbstractOfficeManagerPoolEntry} to execute a given task when the
 * {@link #execute(org.jodconverter.core.task.OfficeTask)} function is called.
 */
public abstract class AbstractOfficeManagerPool extends AbstractOfficeManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOfficeManagerPool.class);

  private static final int POOL_STOPPED = 0;
  private static final int POOL_STARTED = 1;
  private static final int POOL_SHUTDOWN = 2;
  protected static final int DEFAULT_POOL_SIZE = 1;
  // The default maximum living time of a task in the conversion queue.
  private static final long DEFAULT_TASK_QUEUE_TIMEOUT = 30_000L; // 30 seconds

  private final AtomicInteger poolState = new AtomicInteger(POOL_STOPPED);

  private final long taskQueueTimeout;
  private final BlockingQueue<OfficeManager> pool;
  private List<OfficeManager> entries;

  /**
   * Constructs a new instance of the class with the specified settings.
   *
   * @param workingDir The directory where temporary files and directories are created.
   * @param poolSize The pool size.
   * @param taskQueueTimeout The maximum living time of a task in the conversion queue. The task
   *     will be removed from the queue if the waiting time is longer than this timeout.
   */
  protected AbstractOfficeManagerPool(
      @NonNull final File workingDir,
      @Nullable final Integer poolSize,
      @Nullable final Long taskQueueTimeout) {
    super(workingDir);

    this.taskQueueTimeout =
        taskQueueTimeout == null ? DEFAULT_TASK_QUEUE_TIMEOUT : taskQueueTimeout;

    // Create the pool
    pool = new ArrayBlockingQueue<>(poolSize == null ? DEFAULT_POOL_SIZE : poolSize);
  }

  /**
   * Sets the manager entries.
   *
   * @param entries The entries.
   */
  protected void setEntries(@NonNull final List<@NonNull OfficeManager> entries) {
    this.entries = Collections.unmodifiableList(entries);
  }

  @Override
  public final void execute(@NonNull final OfficeTask task) throws OfficeException {

    if (!isRunning()) {
      throw new IllegalStateException("This office manager is not running.");
    }

    // Try to acquire a manager entry, waiting the configured timeout for a
    // manager to become available. If we succeed, the acquired manager will
    // then execute the given task. Once the task is done, return the manager
    // to the pool.
    OfficeManager entry = null;
    try {
      entry = acquireManager();
      entry.execute(task);
    } finally {
      if (entry != null) {
        releaseManager(entry);
      }
    }
  }

  @Override
  public final boolean isRunning() {
    return poolState.get() == POOL_STARTED;
  }

  @Override
  public final void start() throws OfficeException {

    synchronized (this) {
      if (poolState.get() == POOL_SHUTDOWN) {
        throw new IllegalStateException("This office manager has been shutdown.");
      }

      if (poolState.get() == POOL_STARTED) {
        throw new IllegalStateException("This office manager is already running.");
      }

      // Start all entries and make them available to execute tasks.
      for (final OfficeManager manager : entries) {
        manager.start();
        releaseManager(manager);
      }

      // Create the temporary dir if the pool has successfully started
      makeTempDir();

      poolState.set(POOL_STARTED);
    }
  }

  @Override
  public final void stop() throws OfficeException {

    synchronized (this) {
      if (poolState.get() == POOL_SHUTDOWN) {
        // Already shutdown, just exit
        return;
      }

      poolState.set(POOL_SHUTDOWN);

      try {
        LOGGER.info("Stopping the office manager pool...");
        pool.clear();

        OfficeException firstException = null;
        for (final OfficeManager manager : entries) {
          try {
            manager.stop();
          } catch (OfficeException ex) {
            if (firstException == null) {
              firstException = ex;
            }
          }
        }

        if (firstException != null) {
          throw firstException;
        }

        LOGGER.info("Office manager stopped");
      } finally {
        deleteTempDir();
      }
    }
  }

  /**
   * Acquires a manager, waiting the configured timeout for an entry to become available.
   *
   * @return A manager that was available.
   * @throws OfficeException If we are unable to acquire a manager.
   */
  private OfficeManager acquireManager() throws OfficeException {

    try {
      final OfficeManager manager = pool.poll(taskQueueTimeout, TimeUnit.MILLISECONDS);
      if (manager == null) {
        throw new OfficeException(
            "No office manager available after " + taskQueueTimeout + " millisec.");
      }
      return manager;
    } catch (InterruptedException interruptedEx) {
      throw new OfficeException(
          "Thread has been interrupted while waiting for a manager to become available.",
          interruptedEx);
    }
  }

  /**
   * Make the given manager available to executes tasks.
   *
   * @param manager A manager to return to the pool.
   * @throws OfficeException If we are unable to release the manager.
   */
  private void releaseManager(final OfficeManager manager) throws OfficeException {

    try {
      pool.put(manager);
    } catch (InterruptedException interruptedEx) {
      // Not supposed to happened
      throw new OfficeException("interrupted", interruptedEx);
    }
  }

  /**
   * A builder for constructing an {@link AbstractOfficeManagerPool}.
   *
   * @see AbstractOfficeManagerPool
   */
  @SuppressWarnings("unchecked")
  public abstract static class AbstractOfficeManagerPoolBuilder<
          B extends AbstractOfficeManagerPoolBuilder<B>>
      extends AbstractOfficeManagerBuilder<B> {

    protected Long taskExecutionTimeout;
    protected Long taskQueueTimeout;

    // Protected constructor so only subclasses can initialize an instance of this builder.
    protected AbstractOfficeManagerPoolBuilder() {
      super();
    }

    /**
     * Specifies the maximum time allowed to process a task. If the processing time of a task is
     * longer than this timeout, this task will be aborted and the next task is processed.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
     *
     * @param taskExecutionTimeout The task execution timeout, in milliseconds.
     * @return This builder instance.
     */
    @NonNull
    public B taskExecutionTimeout(@Nullable final Long taskExecutionTimeout) {

      if (taskExecutionTimeout != null) {
        Validate.inclusiveBetween(
            0,
            Long.MAX_VALUE,
            taskExecutionTimeout,
            String.format(
                "taskExecutionTimeout %s must greater than or equal to 0", taskExecutionTimeout));
      }
      this.taskExecutionTimeout = taskExecutionTimeout;
      return (B) this;
    }

    /**
     * Specifies the maximum living time of a task in the conversion queue. The task will be removed
     * from the queue if the waiting time is longer than this timeout.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 30000 (30 seconds)
     *
     * @param taskQueueTimeout The task queue timeout, in milliseconds.
     * @return This builder instance.
     */
    @NonNull
    public B taskQueueTimeout(@Nullable final Long taskQueueTimeout) {

      if (taskQueueTimeout != null) {
        Validate.inclusiveBetween(
            0,
            Long.MAX_VALUE,
            taskQueueTimeout,
            String.format("taskQueueTimeout %s must greater than or equal to 0", taskQueueTimeout));
      }
      this.taskQueueTimeout = taskQueueTimeout;
      return (B) this;
    }
  }
}
