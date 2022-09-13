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
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.core.util.StringUtils;

/**
 * An AbstractOfficeManagerPool is responsible to maintain a pool of {@link
 * org.jodconverter.core.office.AbstractOfficeManagerPoolEntry} that will be used to execute {@link
 * org.jodconverter.core.task.OfficeTask}. The pool will use the first available {@link
 * org.jodconverter.core.office.AbstractOfficeManagerPoolEntry} to execute a given task when the
 * {@link #execute(org.jodconverter.core.task.OfficeTask)} function is called.
 */
public abstract class AbstractOfficeManagerPool<E extends AbstractOfficeManagerPoolEntry>
    implements OfficeManager, TemporaryFileMaker {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOfficeManagerPool.class);

  private static final int POOL_STOPPED = 0;
  private static final int POOL_STARTED = 1;
  private static final int POOL_SHUTDOWN = 2;

  // The default maximum living time of a task in the conversion queue.
  public static final long DEFAULT_TASK_QUEUE_TIMEOUT = 30_000L; // 30 seconds
  // The default timeout when executing a task
  public static final long DEFAULT_TASK_EXECUTION_TIMEOUT = 120_000L; // 2 minutes

  private final AtomicInteger poolState = new AtomicInteger(POOL_STOPPED);
  private final File tempDir;
  private final AtomicLong tempFileCounter;
  private final long taskQueueTimeout;
  private final BlockingQueue<E> pool;
  private List<E> entries;

  /**
   * Constructs a new instance of the class with the specified settings.
   *
   * @param poolSize The pool size.
   * @param workingDir The directory where temporary files and directories are created.
   * @param taskQueueTimeout The maximum living time of a task in the conversion queue. The task
   *     will be removed from the queue if the waiting time is longer than this timeout.
   */
  protected AbstractOfficeManagerPool(
      final int poolSize, final @NonNull File workingDir, final long taskQueueTimeout) {
    super();
    AssertUtils.notNull(workingDir, "workingDir must not be null");

    this.taskQueueTimeout = taskQueueTimeout;

    // Initialize the temp directory
    tempDir = new File(workingDir, ".jodconverter_" + UUID.randomUUID().toString());

    // Initialize the temp file counter
    tempFileCounter = new AtomicLong(0);

    // Create the pool
    pool = new ArrayBlockingQueue<>(poolSize);
  }

  /**
   * Sets the manager entries.
   *
   * @param entries The entries.
   */
  protected void setEntries(final @NonNull List<@NonNull E> entries) {
    this.entries = Collections.unmodifiableList(entries);
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
      for (final E manager : entries) {
        manager.start();
        releaseManager(manager);
      }

      // Create the temporary dir if the pool has successfully started
      prepareTempDir();

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

        // Stop all the managers.
        for (final E manager : entries) {
          manager.stop();
        }

      } finally {
        deleteTempDir();
      }
    }
  }

  @Override
  public final boolean isRunning() {
    return poolState.get() == POOL_STARTED;
  }

  /**
   * Acquires a manager, waiting the configured timeout for an entry to become available.
   *
   * @return A manager that was available.
   * @throws OfficeException If we are unable to acquire a manager.
   */
  private E acquireManager() throws OfficeException {
    LOGGER.debug("Acquiring an office manager from the pool...");

    E manager;
    try {
      manager = pool.poll(taskQueueTimeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new OfficeException("Interruption while acquiring manager", ex);
    }

    if (manager == null) {
      throw new OfficeException(
          String.format("No office manager available after %d millisec", taskQueueTimeout));
    }
    LOGGER.debug("Office manager acquired successfully from the pool.");
    return manager;
  }

  /**
   * Make the given manager available to executes tasks.
   *
   * @param manager A manager to return to the pool.
   * @throws OfficeException If we are unable to release a manager.
   */
  private void releaseManager(final E manager) throws OfficeException {
    LOGGER.debug("Returning office manager to the pool...");

    try {
      pool.put(manager);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new OfficeException("Interruption while releasing manager", ex);
    }
  }

  @Override
  public final void execute(final @NonNull OfficeTask task) throws OfficeException {

    if (!isRunning()) {
      throw new IllegalStateException("This office manager is not running.");
    }

    // Try to acquire a manager entry, waiting the configured timeout for a
    // manager to become available. If we succeed, the acquired manager will
    // then execute the given task. Once the task is done, return the manager
    // to the pool.
    E entry = null;
    try {
      entry = acquireManager();
      entry.execute(task);
    } finally {
      if (entry != null) {
        releaseManager(entry);
      }
    }
  }

  /**
   * Prepares directory of this manager, under the working directory.
   *
   * @throws OfficeException If the temporary directory cannot be created.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void prepareTempDir() throws OfficeException {

    if (tempDir.exists()) {
      LOGGER.warn("Temporary directory '{}' already exists; deleting", tempDir);
      deleteTempDir();
    }

    tempDir.mkdirs();
    if (!tempDir.isDirectory()) {
      throw new OfficeException(String.format("Cannot create temporary directory: %s", tempDir));
    }
  }

  /** Deletes the profile directory of the office process. */
  private void deleteTempDir() {

    LOGGER.debug("Deleting temporary directory '{}'", tempDir);
    try {
      FileUtils.delete(tempDir);
    } catch (IOException ex) {
      final File oldDir =
          new File(
              tempDir.getParentFile(), tempDir.getName() + ".old." + System.currentTimeMillis());
      if (tempDir.renameTo(oldDir)) {
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn(
              String.format("Could not delete temporary directory; renamed it to '%s'", oldDir),
              ex);
        }
      } else {
        LOGGER.error("Could not delete temporary", ex);
      }
    }
  }

  @Override
  public @NonNull File makeTemporaryFile() {
    return makeTemporaryFile(null);
  }

  @Override
  public @NonNull File makeTemporaryFile(final @Nullable String extension) {
    return new File(
        tempDir,
        "tempfile_"
            + tempFileCounter.getAndIncrement()
            + (StringUtils.isBlank(extension) ? "" : "." + extension));
  }

  /**
   * A builder for constructing an {@link AbstractOfficeManagerPool}.
   *
   * @see AbstractOfficeManagerPool
   */
  @SuppressWarnings("unchecked")
  public abstract static class AbstractOfficeManagerPoolBuilder<
      B extends AbstractOfficeManagerPoolBuilder<B>> {

    protected boolean install;
    protected File workingDir = OfficeUtils.getDefaultWorkingDir();
    protected long taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;
    protected long taskQueueTimeout = DEFAULT_TASK_QUEUE_TIMEOUT;

    // Protected constructor so only subclasses can initialize an instance of this builder.
    protected AbstractOfficeManagerPoolBuilder() {
      super();
    }

    /**
     * Creates the manager that is specified by this builder.
     *
     * @return The manager that is specified by this builder.
     */
    protected abstract @NonNull AbstractOfficeManagerPool build();

    /**
     * Specifies whether the office manager that will be created by this builder will then set the
     * unique instance of the {@link InstalledOfficeManagerHolder} class. Note that if the {@code
     * InstalledOfficeManagerHolder} class already holds an {@code OfficeManager} instance, the
     * owner of this existing manager is responsible to stopped it.
     *
     * <p>&nbsp; <b><i>Default</i></b>: false
     *
     * @return This builder instance.
     */
    public @NonNull B install() {

      this.install = true;
      return (B) this;
    }

    /**
     * Specifies the directory where temporary files and directories are created.
     *
     * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
     * java.io.tmpdir</code> system property.
     *
     * @param workingDir The new working directory to set.
     * @return This builder instance.
     */
    public @NonNull B workingDir(final @Nullable File workingDir) {

      if (workingDir != null) {
        this.workingDir = workingDir;
      }
      return (B) this;
    }

    /**
     * Specifies the directory where temporary files and directories are created.
     *
     * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
     * java.io.tmpdir</code> system property.
     *
     * @param workingDir The new working directory to set.
     * @return This builder instance.
     */
    public @NonNull B workingDir(final @Nullable String workingDir) {

      return StringUtils.isBlank(workingDir) ? (B) this : workingDir(new File(workingDir));
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
    public B taskExecutionTimeout(final @Nullable Long taskExecutionTimeout) {

      if (taskExecutionTimeout != null) {
        AssertUtils.isTrue(
            taskExecutionTimeout >= 0,
            String.format(
                "taskExecutionTimeout %s must greater than or equal to 0", taskExecutionTimeout));
        this.taskExecutionTimeout = taskExecutionTimeout;
      }
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
    public @NonNull B taskQueueTimeout(final @Nullable Long taskQueueTimeout) {

      if (taskQueueTimeout != null) {
        AssertUtils.isTrue(
            taskQueueTimeout >= 0,
            String.format("taskQueueTimeout %s must greater than or equal to 0", taskQueueTimeout));
        this.taskQueueTimeout = taskQueueTimeout;
      }
      return (B) this;
    }
  }
}
