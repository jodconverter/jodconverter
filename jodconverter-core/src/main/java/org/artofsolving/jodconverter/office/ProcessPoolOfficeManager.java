package org.artofsolving.jodconverter.office;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.artofsolving.jodconverter.process.ProcessManager;

class ProcessPoolOfficeManager implements OfficeManager {

    private final BlockingQueue<ProcessOfficeManager> pool;
    private final ProcessOfficeManager[] pooledManagers;
    private final long taskQueueTimeout;

    public ProcessPoolOfficeManager(File officeHome, UnoUrl[] unoUrls, File templateProfileDir,
            long taskQueueTimeout, long taskExecutionTimeout, int maxTasksPerProcess, ProcessManager processManager) {
        this.taskQueueTimeout = taskQueueTimeout;
        pool = new ArrayBlockingQueue<ProcessOfficeManager>(unoUrls.length);
        pooledManagers = new ProcessOfficeManager[unoUrls.length];
        for (int i = 0; i < unoUrls.length; i++) {
            ProcessOfficeManagerSettings settings = new ProcessOfficeManagerSettings(unoUrls[i]);
            settings.setTemplateProfileDir(templateProfileDir);
            settings.setOfficeHome(officeHome);
            settings.setTaskExecutionTimeout(taskExecutionTimeout);
            settings.setMaxTasksPerProcess(maxTasksPerProcess);
            settings.setProcessManager(processManager);
            pooledManagers[i] = new ProcessOfficeManager(settings);
        }
    }

    public void start() throws OfficeException {
        for (int i = 0; i < pooledManagers.length; i++) {
            pooledManagers[i].start();
            releaseManager(pooledManagers[i]);
        }
    }

    public void execute(OfficeTask task) throws OfficeException {
        ProcessOfficeManager manager = null;
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

    public void stop() throws OfficeException {
        pool.clear();
        for (int i = 0; i < pooledManagers.length; i++) {
            pooledManagers[i].stop();
        }
    }

    private ProcessOfficeManager acquireManager() {
        try {
            return pool.poll(taskQueueTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interruptedException) {
            throw new OfficeException("interrupted", interruptedException);
        }
    }

    private void releaseManager(ProcessOfficeManager manager) {
        try {
            pool.put(manager);
        } catch (InterruptedException interruptedException) {
            throw new OfficeException("interrupted", interruptedException);
        }
    }

}
