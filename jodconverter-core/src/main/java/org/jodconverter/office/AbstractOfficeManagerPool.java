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

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.task.OfficeTask;

/**
 * A OfficeManagerPool is responsible to maintain a pool of {@link OfficeProcessManagerPoolEntry}
 * that will be used to execute {@link OfficeTask}. The pool will use the first {@link
 * OfficeProcessManagerPoolEntry} to execute a given task when the {@link #execute(OfficeTask)}
 * function is called.
 */
abstract class AbstractOfficeManagerPool implements OfficeManager, TemporaryFileMaker {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOfficeManagerPool.class);

  private static final int POOL_STOPPED = 0;
  private static final int POOL_STARTED = 1;
  private static final int POOL_SHUTDOWN = 2;

  private final AtomicInteger poolState = new AtomicInteger(POOL_STOPPED);

  protected final OfficeManagerPoolConfig config;
  private final BlockingQueue<OfficeManager> pool;
  private final AtomicLong tempFileCounter;
  private OfficeManager[] entries;
  private File tempDir;

  private static File makeTempDir(final File workingDir) {

    final File tempDir = new File(workingDir, "jodconverter_" + UUID.randomUUID().toString());
    tempDir.mkdir();
    if (!tempDir.isDirectory()) {
      throw new IllegalStateException(String.format("Cannot create temp directory: %s", tempDir));
    }
    return tempDir;
  }

  /** Constructs a new instance of the class with the specified settings. */
  protected AbstractOfficeManagerPool(final int poolSize, final OfficeManagerPoolConfig config) {
    super();

    this.config = config;

    // Create the pool
    pool = new ArrayBlockingQueue<>(poolSize);

    // Initialize the temp file counter
    tempFileCounter = new AtomicLong(0);
  }

  /**
   * Creates the pool entries when the pool is started.
   *
   * @return an array of pool entries.
   */
  protected abstract OfficeManager[] createPoolEntries();

  @Override
  public void execute(final OfficeTask task) throws OfficeException {

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
  public boolean isRunning() {
    return poolState.get() == POOL_STARTED;
  }

  @Override
  public void start() throws OfficeException {

    synchronized (this) {
      if (poolState.get() == POOL_SHUTDOWN) {
        throw new IllegalStateException("This office manager has been shutdown.");
      }

      if (poolState.get() == POOL_STARTED) {
        throw new IllegalStateException("This office manager is already running.");
      }

      // Create the poll entries...
      entries = createPoolEntries();

      // then start them.
      doStart();

      // Create the temporary dir is the pool has successfully started
      tempDir = makeTempDir(config.getWorkingDir());

      poolState.set(POOL_STARTED);
    }
  }

  @Override
  public void stop() throws OfficeException {

    synchronized (this) {
      if (poolState.get() == POOL_SHUTDOWN) {
        // Already shutdown, just exit
        return;
      }

      poolState.set(POOL_SHUTDOWN);

      try {
        doStop();
      } finally {
        deleteTempDir();
      }
    }
  }

  @Override
  public File makeTemporaryFile() {
    return new File(tempDir, "tempfile_" + tempFileCounter.getAndIncrement());
  }

  @Override
  public File makeTemporaryFile(final String extension) {
    return new File(tempDir, "tempfile_" + tempFileCounter.getAndIncrement() + "." + extension);
  }

  /**
   * Acquires a manager, waiting the configured timeout for an entry to become available.
   *
   * @return A manager that was available.
   * @throws OfficeException If we are unable to acquire a manager.
   */
  private OfficeManager acquireManager() throws OfficeException {

    try {
      final OfficeManager manager = pool.poll(config.getTaskQueueTimeout(), TimeUnit.MILLISECONDS);
      if (manager == null) {
        throw new OfficeException(
            "No office manager available after " + config.getTaskQueueTimeout() + " millisec.");
      }
      return manager;
    } catch (InterruptedException interruptedEx) { // NOSONAR
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
    } catch (InterruptedException interruptedEx) { // NOSONAR
      // Not supposed to happened
      throw new OfficeException("interrupted", interruptedEx);
    }
  }

  /**
   * Allow base class to perform operation when the pool starts.
   *
   * @throws OfficeException If an error occurs.
   */
  protected void doStart() throws OfficeException {

    // Start all PooledOfficeManager and make them available to execute tasks.
    for (final OfficeManager manager : entries) {
      manager.start();
      releaseManager(manager);
    }
  }

  private void doStop() throws OfficeException {

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
  }

  private void deleteTempDir() {

    if (tempDir != null) {
      LOGGER.debug("Deleting temporary directory '{}'", tempDir);
      try {
        FileUtils.deleteDirectory(tempDir);
      } catch (IOException ioEx) { // NOSONAR
        LOGGER.error("Could not temporary profileDir: {}", ioEx.getMessage());
      }
    }
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
    protected File workingDir;
    protected long taskExecutionTimeout =
        OfficeManagerPoolEntryConfig.DEFAULT_TASK_EXECUTION_TIMEOUT;
    protected long taskQueueTimeout = OfficeManagerPoolConfig.DEFAULT_TASK_QUEUE_TIMEOUT;

    // Protected ctor so only subclasses can initialize an instance of this builder.
    protected AbstractOfficeManagerPoolBuilder() {
      super();
    }

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
    public B install() {

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
    public B workingDir(final File workingDir) {

      this.workingDir = workingDir;
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
    public B workingDir(final String workingDir) {

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
    public B taskExecutionTimeout(final long taskExecutionTimeout) {

      Validate.inclusiveBetween(
          0,
          Long.MAX_VALUE,
          taskExecutionTimeout,
          String.format(
              "The taskExecutionTimeout %s must greater than or equal to 0", taskExecutionTimeout));
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
    public B taskQueueTimeout(final long taskQueueTimeout) {

      Validate.inclusiveBetween(
          0,
          Long.MAX_VALUE,
          taskQueueTimeout,
          String.format(
              "The taskQueueTimeout %s must greater than or equal to 0", taskQueueTimeout));
      this.taskQueueTimeout = taskQueueTimeout;
      return (B) this;
    }

    /**
     * Creates the manager that is specified by this builder.
     *
     * @return The manager that is specified by this builder.
     */
    protected abstract AbstractOfficeManagerPool build();
  }
}
