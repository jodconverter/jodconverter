//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
// -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
// -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.office;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.artofsolving.jodconverter.process.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lib.uno.helper.UnoUrl;

/**
 * A ProcessPoolOfficeManager is responsible to maintain a pool of {@link PooledOfficeManager} that
 * will be used to execute {@link OfficeTask}. The manager will use the first
 * {@link PooledOfficeManager} to execute a given task when the {@link #execute(OfficeTask)}
 * function is called.
 */
class ProcessPoolOfficeManager implements OfficeManager {

    private static final Logger logger = LoggerFactory.getLogger(ProcessPoolOfficeManager.class);

    private final BlockingQueue<PooledOfficeManager> pool;
    private final PooledOfficeManager[] pooledManagers;
    private final long taskQueueTimeout;

    private volatile boolean running = false;

    /**
     * Constructs a new instance of the class with the specified settings.
     */
    public ProcessPoolOfficeManager(File officeHome, UnoUrl[] unoUrls, String[] runAsArgs, File templateProfileDir, File workDir, long retryTimeout, long taskQueueTimeout, long taskExecutionTimeout, int maxTasksPerProcess, ProcessManager processManager, boolean killExistingProcess) {

        this.taskQueueTimeout = taskQueueTimeout;
        pool = new ArrayBlockingQueue<PooledOfficeManager>(unoUrls.length);
        pooledManagers = new PooledOfficeManager[unoUrls.length];
        for (int i = 0; i < unoUrls.length; i++) {
            PooledOfficeManagerSettings settings = new PooledOfficeManagerSettings(unoUrls[i]);
            settings.setRunAsArgs(runAsArgs);
            settings.setTemplateProfileDir(templateProfileDir);
            settings.setWorkingDir(workDir);
            settings.setOfficeHome(officeHome);
            settings.setRetryTimeout(retryTimeout);
            settings.setTaskExecutionTimeout(taskExecutionTimeout);
            settings.setMaxTasksPerProcess(maxTasksPerProcess);
            settings.setProcessManager(processManager);
            settings.setKillExistingProcess(killExistingProcess);
            pooledManagers[i] = new PooledOfficeManager(settings);
        }
        logger.debug("ProcessManager implementation is '{}'", processManager.getClass().getSimpleName());
    }

    /**
     * Acquires a {@link PooledOfficeManager}, waiting the configured timeout for a manager to
     * become available.
     * 
     * @return A {@link PooledOfficeManager} that was available.
     */
    private PooledOfficeManager acquireManager() {

        try {
            return pool.poll(taskQueueTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interruptedException) {
            throw new OfficeException("interrupted", interruptedException);
        }
    }

    /**
     * Executes the given task.
     */
    public void execute(OfficeTask task) throws IllegalStateException, OfficeException {

        if (!running) {
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

    /**
     * Gets whether this office manager is running.
     * 
     * @return {@code true} if the manager is running, {@code false} otherwise.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Make the given {@link PooledOfficeManager} available to executes tasks.
     * 
     * @param manager
     *            A {@link PooledOfficeManager} to put into the pool.
     */
    private void releaseManager(PooledOfficeManager manager) {

        try {
            pool.put(manager);
        } catch (InterruptedException interruptedException) {
            // Not supposed to happened
            throw new OfficeException("interrupted", interruptedException);
        }
    }

    /**
     * Starts the manager.
     */
    public synchronized void start() throws OfficeException {

        // Start all PooledOfficeManager and make them available to execute tasks.
        for (int i = 0; i < pooledManagers.length; i++) {
            pooledManagers[i].start();
            releaseManager(pooledManagers[i]);
        }
        running = true;
    }

    /**
     * Stops the manager.
     */
    public synchronized void stop() throws OfficeException {

        running = false;
        logger.info("Stopping the office manager");
        pool.clear();
        for (int i = 0; i < pooledManagers.length; i++) {
            pooledManagers[i].stop();
        }
        logger.info("Office manager stopped");
    }

}
