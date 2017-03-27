/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.office;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lib.uno.helper.UnoUrl;

import org.artofsolving.jodconverter.process.ProcessManager;

/**
 * A ProcessPoolOfficeManager is responsible to maintain a pool of {@link PooledOfficeManager} that
 * will be used to execute {@link OfficeTask}. The manager will use the first {@link
 * PooledOfficeManager} to execute a given task when the {@link #execute(OfficeTask)} function is
 * called.
 */
class ProcessPoolOfficeManager implements OfficeManager {

  private static final Logger logger = LoggerFactory.getLogger(ProcessPoolOfficeManager.class);

  private final BlockingQueue<PooledOfficeManager> pool;
  private final PooledOfficeManager[] pooledManagers;
  private final long taskQueueTimeout;
  private final AtomicBoolean running = new AtomicBoolean();

  /** Constructs a new instance of the class with the specified settings. */
  public ProcessPoolOfficeManager(
      final UnoUrl[] unoUrls,
      final File officeHome,
      final File workingDir,
      final ProcessManager processManager,
      final String[] runAsArgs,
      final File templateProfileDir,
      final long retryTimeout,
      final long retryInterval,
      final boolean killExistingProcess,
      final long taskQueueTimeout,
      final long taskExecutionTimeout,
      final int maxTasksPerProcess) {

    this.taskQueueTimeout = taskQueueTimeout;
    pool = new ArrayBlockingQueue<PooledOfficeManager>(unoUrls.length);
    pooledManagers = new PooledOfficeManager[unoUrls.length];
    for (int i = 0; i < unoUrls.length; i++) {
      final PooledOfficeManagerSettings settings =
          new PooledOfficeManagerSettings(unoUrls[i], officeHome, workingDir, processManager);
      settings.setRunAsArgs(runAsArgs);
      settings.setTemplateProfileDir(templateProfileDir);
      settings.setRetryTimeout(retryTimeout);
      settings.setRetryInterval(retryInterval);
      settings.setKillExistingProcess(killExistingProcess);
      settings.setTaskExecutionTimeout(taskExecutionTimeout);
      settings.setMaxTasksPerProcess(maxTasksPerProcess);
      pooledManagers[i] = new PooledOfficeManager(settings);
    }
    logger.debug(
        "ProcessManager implementation is '{}'", processManager.getClass().getSimpleName());
  }

  /**
   * Acquires a {@link PooledOfficeManager}, waiting the configured timeout for a manager to become
   * available.
   *
   * @return A {@link PooledOfficeManager} that was available.
   * @throws OfficeException if we are unable to acquire a manager.
   */
  private PooledOfficeManager acquireManager() throws OfficeException {

    try {
      return pool.poll(taskQueueTimeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException interruptedEx) {
      throw new OfficeException("interrupted", interruptedEx);
    }
  }

  @Override
  public void execute(final OfficeTask task) throws OfficeException {

    if (!running.get()) {
      throw new IllegalStateException("This office manager is currently stopped.");
    }

    // Try to acquire a PooledOfficeManager, waiting the configured timeout for
    // a PooledOfficeManager to become available. If we succeed, the acquired
    // PooledOfficeManager will execute the given task. Once the task is done,
    // return the PooledOfficeManager to the pool.
    PooledOfficeManager manager = null;
    try {
      manager = acquireManager();
      if (manager == null) {
        throw new OfficeException("No office manager available.");
      }
      manager.execute(task);
    } finally {
      if (manager != null) {
        releaseManager(manager);
      }
    }
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  /**
   * Make the given {@link PooledOfficeManager} available to executes tasks.
   *
   * @param manager A {@link PooledOfficeManager} to put into the pool.
   * @throws OfficeException if we are unable to release the manager.
   */
  private void releaseManager(final PooledOfficeManager manager) throws OfficeException {

    try {
      pool.put(manager);
    } catch (InterruptedException interruptedEx) {
      // Not supposed to happened
      throw new OfficeException("interrupted", interruptedEx);
    }
  }

  @Override
  public synchronized void start() throws OfficeException {

    // Start all PooledOfficeManager and make them available to execute tasks.
    for (int i = 0; i < pooledManagers.length; i++) {
      pooledManagers[i].start();
      releaseManager(pooledManagers[i]);
    }
    running.set(true);
  }

  @Override
  public synchronized void stop() throws OfficeException {

    running.set(false);
    logger.info("Stopping the office manager");
    pool.clear();
    for (int i = 0; i < pooledManagers.length; i++) {
      pooledManagers[i].stop();
    }
    logger.info("Office manager stopped");
  }
}
