package org.artofsolving.jodconverter.office;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

class PoolingOfficeManager implements OfficeManager {

    private final BlockingQueue<ManagedProcessOfficeManager> pool;
    private final ManagedProcessOfficeManager[] pooledManagers;

    public PoolingOfficeManager(File officeHome, UnoUrl[] unoUrls) {
        pool = new ArrayBlockingQueue<ManagedProcessOfficeManager>(unoUrls.length);
        pooledManagers = new ManagedProcessOfficeManager[unoUrls.length];
        for (int i = 0; i < unoUrls.length; i++) {
            ManagedProcessOfficeManagerConfiguration configuration = new ManagedProcessOfficeManagerConfiguration(unoUrls[i]);
            configuration.setOfficeHome(officeHome);
            configuration.setTaskQueueTimeout(0L);
            pooledManagers[i] = new ManagedProcessOfficeManager(configuration);
        }
    }

    public void start() throws OfficeException {
        for (int i = 0; i < pooledManagers.length; i++) {
            pooledManagers[i].start();
            releaseManager(pooledManagers[i]);
        }
    }

    public void execute(OfficeTask task) throws OfficeException {
        ManagedProcessOfficeManager manager = null;
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

    private ManagedProcessOfficeManager acquireManager() {
        try {
            return pool.poll(30, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
            throw new OfficeException("interrupted", interruptedException);
        }
    }

    private void releaseManager(ManagedProcessOfficeManager manager) {
        try {
            pool.put(manager);
        } catch (InterruptedException interruptedException) {
            throw new OfficeException("interrupted", interruptedException);
        }
    }

}
