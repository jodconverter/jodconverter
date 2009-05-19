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

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.artofsolving.jodconverter.process.ProcessManager;

class ProcessPoolOfficeManager implements OfficeManager {

    private final BlockingQueue<PooledOfficeManager> pool;
    private final PooledOfficeManager[] pooledManagers;
    private final long taskQueueTimeout;

    private volatile boolean running = false;

    private final Logger logger = Logger.getLogger(ProcessPoolOfficeManager.class.getName());

    public ProcessPoolOfficeManager(File officeHome, UnoUrl[] unoUrls, File templateProfileDir,
            long taskQueueTimeout, long taskExecutionTimeout, int maxTasksPerProcess, ProcessManager processManager) {
        this.taskQueueTimeout = taskQueueTimeout;
        pool = new ArrayBlockingQueue<PooledOfficeManager>(unoUrls.length);
        pooledManagers = new PooledOfficeManager[unoUrls.length];
        for (int i = 0; i < unoUrls.length; i++) {
            PooledOfficeManagerSettings settings = new PooledOfficeManagerSettings(unoUrls[i]);
            settings.setTemplateProfileDir(templateProfileDir);
            settings.setOfficeHome(officeHome);
            settings.setTaskExecutionTimeout(taskExecutionTimeout);
            settings.setMaxTasksPerProcess(maxTasksPerProcess);
            settings.setProcessManager(processManager);
            pooledManagers[i] = new PooledOfficeManager(settings);
        }
        logger.info("ProcessManager implementation is " + processManager.getClass().getSimpleName());
    }

    public synchronized void start() throws OfficeException {
        for (int i = 0; i < pooledManagers.length; i++) {
            pooledManagers[i].start();
            releaseManager(pooledManagers[i]);
        }
        running = true;
    }

    public void execute(OfficeTask task) throws IllegalStateException, OfficeException {
        if (!running) {
            throw new IllegalStateException("this OfficeManager is currently stopped");
        }
        PooledOfficeManager manager = null;
        try {
            manager = acquireManager();
            if (manager == null) {
                throw new OfficeException("no office manager available");
            }
            manager.execute(task);
        } finally {
            if (manager != null) {
                releaseManager(manager);
            }
        }
    }

    public synchronized void stop() throws OfficeException {
        running = false;
        logger.info("stopping");
        pool.clear();
        for (int i = 0; i < pooledManagers.length; i++) {
            pooledManagers[i].stop();
        }
        logger.info("stopped");
    }

    private PooledOfficeManager acquireManager() {
        try {
            return pool.poll(taskQueueTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interruptedException) {
            throw new OfficeException("interrupted", interruptedException);
        }
    }

    private void releaseManager(PooledOfficeManager manager) {
        try {
            pool.put(manager);
        } catch (InterruptedException interruptedException) {
            throw new OfficeException("interrupted", interruptedException);
        }
    }

}
