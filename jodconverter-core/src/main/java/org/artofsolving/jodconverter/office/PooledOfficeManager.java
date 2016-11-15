//
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PooledOfficeManager is responsible to execute tasks submitted through a
 * {@link ProcessPoolOfficeManager}. It will submit tasks to its inner {@link ManagedOfficeProcess}
 * and wait until the task is done or a configured task execution timeout is reached.
 * <p>
 * A PooledOfficeManager is also responsible to restart an office process when the maximum number of
 * tasks per process is reached.
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
    private AtomicBoolean stopping = new AtomicBoolean(false);
    private AtomicInteger taskCount = new AtomicInteger(0);

    /**
     * This connection event listener will be notified when a connection is established or
     * closed/lost to/from an office instance.
     */
    private OfficeConnectionEventListener connectionEventListener = new OfficeConnectionEventListener() {

        // A connection is established.
        @Override
        public void connected(OfficeConnectionEvent event) {
            logger.trace("> OfficeConnectionEventListener.connected");

            // Reset the task count and make the task executor available.
            taskCount.set(0);
            taskExecutor.setAvailable(true);

            logger.trace("< OfficeConnectionEventListener.connected");
        }

        // A connection is closed/lost.
        @Override
        public void disconnected(OfficeConnectionEvent event) {
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
                //managedOfficeProcess.restartAndWait();
                managedOfficeProcess.restartDueToLostConnection();
            }

            logger.trace("< OfficeConnectionEventListener.disconnected");
        }
    };

    /**
     * Creates a new instance of the class with the specified settings.
     * 
     * @param settings
     *            the settings used to initialize the instance.
     */
    public PooledOfficeManager(PooledOfficeManagerSettings settings) {

        this.settings = settings;
        managedOfficeProcess = new ManagedOfficeProcess(settings);
        taskExecutor = new SuspendableThreadPoolExecutor(new NamedThreadFactory("OfficeTaskThread"));

        // Listen to any connection events to the office instance.
        managedOfficeProcess.getConnection().addConnectionEventListener(connectionEventListener);
    }

    /**
     * Executes a task.
     */
    public void execute(final OfficeTask task) throws OfficeException {

        Future<?> futureTask = taskExecutor.submit(new Runnable() {

            @Override
            public void run() {

                // First check if the office process must be restarted
                int count = taskCount.getAndIncrement();
                if (settings.getMaxTasksPerProcess() > 0 && count == settings.getMaxTasksPerProcess()) {
                    logger.info("Reached limit of {} maximum tasks per process; restarting...", settings.getMaxTasksPerProcess());

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
            }
        });

        currentTask = futureTask;
        try {
            // Try to get the result of the task, waiting the configured task execution timeout
            logger.debug("Waiting for task to complete...");
            futureTask.get(settings.getTaskExecutionTimeout(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException timeoutException) {

            // The task did not complete withing the configured timeout...
            // Restart the office instance.
            //managedOfficeProcess.restartAndWait();
            managedOfficeProcess.restartDueToTaskTimeout();
            throw new OfficeException("task did not complete within timeout", timeoutException);

        } catch (ExecutionException executionException) {

            if (executionException.getCause() instanceof OfficeException) {
                throw (OfficeException) executionException.getCause();
            } else {
                throw new OfficeException("Task failed", executionException.getCause());
            }

        } catch (Exception exception) {

            // Unexcpected exception
            throw new OfficeException("Task failed", exception);
        }
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
