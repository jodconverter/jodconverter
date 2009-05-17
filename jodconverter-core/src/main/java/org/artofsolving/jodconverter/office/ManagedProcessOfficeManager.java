//
// JODConverter - Java OpenDocument Converter
// Copyright 2009 Art of Solving Ltd
// Copyright 2004-2009 Mirko Nasato
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter.  If not, see
// <http://www.gnu.org/licenses/>.
//
package org.artofsolving.jodconverter.office;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.artofsolving.jodconverter.util.NamedThreadFactory;
import org.artofsolving.jodconverter.util.SuspendableThreadPoolExecutor;

class ManagedProcessOfficeManager implements OfficeManager {

    private static final ThreadFactory THREAD_FACTORY = new NamedThreadFactory("OfficeManagerThread");

    private final ManagedProcessOfficeManagerConfiguration configuration;
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

    public ManagedProcessOfficeManager(UnoUrl unoUrl) {
        this(new ManagedProcessOfficeManagerConfiguration(unoUrl));
    }

    public ManagedProcessOfficeManager(ManagedProcessOfficeManagerConfiguration configuration) {
        this.configuration = configuration;
        managedOfficeProcess = new ManagedOfficeProcess(configuration);
        managedOfficeProcess.getConnection().addConnectionEventListener(connectionEventListener);
        taskExecutor = new SuspendableThreadPoolExecutor(THREAD_FACTORY, configuration.getTaskQueueTimeout(), TimeUnit.MILLISECONDS);
    }

    public void execute(final OfficeTask task) throws OfficeException {
        Future<?> futureTask = taskExecutor.submit(new Runnable() {
            public void run() {
                if (configuration.getMaxTasksPerProcess() > 0 && ++taskCount == configuration.getMaxTasksPerProcess() + 1) {
                    logger.info(String.format("reached limit of %d maxTasksPerProcess: restarting", configuration.getMaxTasksPerProcess()));
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
             futureTask.get(configuration.getTaskExecutionTimeout(), TimeUnit.MILLISECONDS);
         } catch (TimeoutException timeoutException) {
             managedOfficeProcess.restartDueToTaskTimeout();
             throw new OfficeException("task did not complete within timeout", timeoutException);
         } catch (ExecutionException executionException) {
             if (executionException.getCause() instanceof OfficeException) {
                 throw (OfficeException) executionException.getCause();
             } else {
                 throw new OfficeException("task failed", executionException.getCause());
             }
         } catch (Exception exception) {
             throw new OfficeException("task failed", exception);
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
