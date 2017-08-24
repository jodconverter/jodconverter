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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.task.OfficeTask;

/**
 * A OfficeManagerPool is responsible to maintain a pool of {@link OfficeProcessManagerPoolEntry}
 * that will be used to execute {@link OfficeTask}. The pool will use the first {@link
 * OfficeProcessManagerPoolEntry} to execute a given task when the {@link #execute(OfficeTask)}
 * function is called.
 */
class OfficeManagerPool implements OfficeManager, TemporaryFileMaker {

  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeManagerPool.class);

  private static final int POOL_STOPPED = 0;
  private static final int POOL_STARTED = 1;
  private static final int POOL_SHUTDOWN = 2;

  private final AtomicInteger poolState = new AtomicInteger(POOL_STOPPED);

  private final BlockingQueue<OfficeManager> pool;
  private final OfficeManager[] entries;
  private final OfficeManagerPoolConfig config;
  private final File tempDir;
  private final AtomicLong tempFileCounter;

  private static File makeTempDir(final File workingDir) {

    final File tempDir = new File(workingDir, "jodconverter_" + UUID.randomUUID().toString());
    tempDir.mkdir();
    if (!tempDir.isDirectory()) {
      throw new IllegalStateException(String.format("Cannot create temp directory: %s", tempDir));
    }
    return tempDir;
  }

  /** Constructs a new instance of the class with the specified settings. */
  protected OfficeManagerPool(
      final OfficeUrl[] officeUrls, final OfficeProcessManagerPoolConfig config) {
    super();

    this.config = config;

    // Create the pool and all its entries
    pool = new ArrayBlockingQueue<>(officeUrls.length);
    entries = new OfficeProcessManagerPoolEntry[officeUrls.length];
    for (int i = 0; i < officeUrls.length; i++) {
      entries[i] = new OfficeProcessManagerPoolEntry(officeUrls[i], config);
    }

    // Create the temporary dir
    tempDir = makeTempDir(config.getWorkingDir());
    tempFileCounter = new AtomicLong(0);
  }

  /** Constructs a new instance of the class with the specified settings. */
  protected OfficeManagerPool(final int poolSize, final SimpleOfficeManagerPoolConfig config) {
    super();

    this.config = config;

    // Create the pool and all its entries
    pool = new ArrayBlockingQueue<>(poolSize);
    entries = new SimpleOfficeManagerPoolEntry[poolSize];
    for (int i = 0; i < poolSize; i++) {
      entries[i] = new SimpleOfficeManagerPoolEntry(config);
    }

    // Create the temporary dir
    tempDir = makeTempDir(config.getWorkingDir());
    tempFileCounter = new AtomicLong(0);
  }

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
      doStart();
    }
  }

  @Override
  public void stop() throws OfficeException {

    synchronized (this) {
      try {
        doStop();
      } finally {
        deleteTempDir();
      }
    }
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

  private void doStart() throws OfficeException {

    if (poolState.get() == POOL_SHUTDOWN) {
      throw new IllegalStateException("This office manager has been shutdown.");
    }

    if (poolState.get() == POOL_STARTED) {
      throw new IllegalStateException("This office manager is already running.");
    }

    // Start all PooledOfficeManager and make them available to execute tasks.
    for (int i = 0; i < entries.length; i++) {
      entries[i].start();
      releaseManager(entries[i]);
    }

    poolState.set(POOL_STARTED);
  }

  private void doStop() throws OfficeException {

    if (poolState.get() == POOL_SHUTDOWN) {
      // Already shutdown, just exit
      return;
    }

    poolState.set(POOL_SHUTDOWN);

    LOGGER.info("Stopping the office manager pool...");
    pool.clear();

    OfficeException firstException = null;
    for (int i = 0; i < entries.length; i++) {
      try {
        entries[i].stop();
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
}
