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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PooledOfficeManager is responsible to execute tasks submitted through a {@link ProcessPoolOfficeManager}. It will
 * submit tasks to its inner {@link ManagedOfficeProcess} and wait until the task is done or a configured task execution
 * timeout is reached.
 * <p>
 * A PooledOfficeManager is also responsible to restart an office process when the maximum number of tasks per process
 * is reached.
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
     * This connection event listener will be notified when a connection is established or closed/lost to/from an office
     * instance.
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
                managedOfficeProcess.restartDueToLostConnection();
            }

            logger.trace("< OfficeConnectionEventListener.disconnected");
        }
    };

    /**
     * Creates a new instance of the class with the specified settings.
     * 
     * @param settings the settings used to initialize the instance.
     */
    public PooledOfficeManager(PooledOfficeManagerSettings settings) {

        this.settings = settings;
        managedOfficeProcess = new ManagedOfficeProcess(settings);
        taskExecutor = new SuspendableThreadPoolExecutor(new NamedThreadFactory("OfficeTaskThread"));

        // Listen to any connection events to the office instance.
        managedOfficeProcess.getConnection().addConnectionEventListener(connectionEventListener);
    }

    @Override
    public void execute(final OfficeTask task) throws OfficeException {

        // Create the command to be executed
        Callable<Void> command = new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                // First check if the office process must be restarted
                int count = taskCount.getAndIncrement();
                if (settings.getMaxTasksPerProcess() > 0 && count == settings.getMaxTasksPerProcess()) {
                    logger.info("Reached limit of {} maximum tasks per process; restarting...",
                            settings.getMaxTasksPerProcess());

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

                return null;
            }
        };

        // Submit the task to the executor
        currentTask = taskExecutor.submit(command);

        // Wait for completion of the task, (maximum wait time is the
        // configured task execution timeout)
        try {
            logger.debug("Waiting for task to complete...");
            currentTask.get(settings.getTaskExecutionTimeout(), TimeUnit.MILLISECONDS);
            logger.debug("Task executed successfully");

        } catch (TimeoutException timeoutEx) {

            // The task did not complete withing the configured timeout...
            managedOfficeProcess.restartDueToTaskTimeout();
            throw new OfficeException("task did not complete within timeout", timeoutEx);

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
            currentTask = null;
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

	@Override
	public OfficeContext getContext() throws OfficeException {
		return this.managedOfficeProcess.getConnection();
	}

}
