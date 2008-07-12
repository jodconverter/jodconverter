//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2008 - Mirko Nasato <mirko@artofsolving.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, you can find it online
// at http://www.gnu.org/licenses/lgpl-2.1.html.
//
package net.sf.jodconverter.office;

import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import net.sf.jodconverter.util.NamedThreadFactory;
import net.sf.jodconverter.util.SuspendableThreadPoolExecutor;

public class SingleOfficeManager implements OfficeManager {

    private static final String DEFAULT_ACCEPT_STRING = "socket,host=127.0.0.1,port=8100";
    private static final File DEFAULT_PROFILE_DIR = new File(System.getProperty("java.io.tmpdir"), "jodconverter");
    private static final long DEFAULT_TASK_QUEUE_TIMEOUT = 30 * 1000;
    private static final long DEFAULT_TASK_EXECUTION_TIMEOUT = 120 * 1000;
    private static final int DEFAULT_MAX_TASKS_PER_PROCESS = 200;

    private static final ThreadFactory THREAD_FACTORY = new NamedThreadFactory("SingleOfficeManagerThread");

    private long taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;
    private int maxTasksPerProcess = DEFAULT_MAX_TASKS_PER_PROCESS;

    private final ManagedOfficeProcess managedOfficeProcess;
    private final SuspendableThreadPoolExecutor taskExecutor;

    private volatile boolean stopping = false;
    private int taskCount;
    private Future<?> currentTask;

    private final Logger logger = Logger.getLogger(getClass().getName());

    private OfficeConnectionEventListener connectionEventListener = new OfficeConnectionEventListener() {
        public void connected(OfficeConnectionEvent event) {
            taskCount = 0;
            taskExecutor.setAvailable(true);
        }
        public void disconnected(OfficeConnectionEvent event) {
            taskExecutor.setAvailable(false);
            if (stopping) {
                // expected
                stopping = false;
            } else {
                logger.warning("connection lost unexpectedly; attempting restart");
                if (currentTask != null) {
                    currentTask.cancel(true);
                }
                managedOfficeProcess.restartDueToLostConnection();
            }
        }
    };

    public SingleOfficeManager(File officeHome) {
        this(officeHome, DEFAULT_ACCEPT_STRING, DEFAULT_PROFILE_DIR);
    }

    public SingleOfficeManager(File officeHome, String acceptString) {
        this(officeHome, acceptString, DEFAULT_PROFILE_DIR);
    }

    public SingleOfficeManager(File officeHome, String acceptString, File profileDir) {
        this(officeHome, acceptString, profileDir, DEFAULT_TASK_QUEUE_TIMEOUT);
    }

    public SingleOfficeManager(File officeHome, String acceptString, File profileDir, long taskQueueTimeout) {
        managedOfficeProcess = new ManagedOfficeProcess(officeHome, acceptString, profileDir);
        managedOfficeProcess.getConnection().addConnectionEventListener(connectionEventListener);
        taskExecutor = new SuspendableThreadPoolExecutor(THREAD_FACTORY, taskQueueTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * @param taskExecutionTimeout defaults to 2 minutes
     */
    public void setTaskExecutionTimeout(long taskExecutionTimeout) {
        this.taskExecutionTimeout = taskExecutionTimeout;
    }

    /**
     * @param maxTasksPerProcess defaults to 200; 0 means unlimited
     */
    public void setMaxTasksPerProcess(int maxTasksPerProcess) {
        this.maxTasksPerProcess = maxTasksPerProcess;
    }

    public void execute(final OfficeTask task) throws OfficeException {
        Future<?> futureTask = taskExecutor.submit(new Runnable() {
            public void run() {
                if (maxTasksPerProcess > 0 && ++taskCount == maxTasksPerProcess + 1) {
                    logger.info(String.format("reached limit of %d maxTasksPerProcess: restarting", maxTasksPerProcess));
                    taskExecutor.setAvailable(false);
                    stopping = true;
                    managedOfficeProcess.restartAndWait();
                    //FIXME taskCount will be 0 rather than 1 at this point
                }
                task.execute(managedOfficeProcess.getConnection());
             }
         });
         currentTask = futureTask;
         try {
             futureTask.get(taskExecutionTimeout, TimeUnit.MILLISECONDS);
         } catch (TimeoutException timeoutException) {
             managedOfficeProcess.restartDueToTaskTimeout();
             throw new OfficeException("task did not complete within timeout", timeoutException);
         } catch (Exception exception) {
             throw new OfficeException("could not complete task", exception);
         }
    }

    public void start() throws OfficeException {
        managedOfficeProcess.startAndWait();
    }

    public void stop() throws OfficeException {
        taskExecutor.setAvailable(false);
        stopping = true;
        managedOfficeProcess.stopAndWait();
        taskExecutor.shutdownNow();
    }

}
