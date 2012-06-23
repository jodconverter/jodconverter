//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
//    -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
//    -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.office;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;


import org.artofsolving.jodconverter.ReflectionUtils;
import org.artofsolving.jodconverter.office.ManagedOfficeProcess;
import org.artofsolving.jodconverter.office.PooledOfficeManager;
import org.artofsolving.jodconverter.office.PooledOfficeManagerSettings;
import org.artofsolving.jodconverter.office.OfficeConnection;
import org.artofsolving.jodconverter.office.UnoUrl;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeProcess;
import org.testng.annotations.Test;

@Test(groups="integration")
public class PooledOfficeManagerTest {

    private static final UnoUrl CONNECTION_MODE = UnoUrl.socket(2002);
    private static final long RESTART_WAIT_TIME = 2 * 1000;

    public void executeTask() throws Exception {
        PooledOfficeManager officeManager = new PooledOfficeManager(CONNECTION_MODE);
        ManagedOfficeProcess managedOfficeProcess = (ManagedOfficeProcess) ReflectionUtils.getPrivateField(officeManager, "managedOfficeProcess");
        OfficeProcess process = (OfficeProcess) ReflectionUtils.getPrivateField(managedOfficeProcess, "process");
        OfficeConnection connection = (OfficeConnection) ReflectionUtils.getPrivateField(managedOfficeProcess, "connection");
        
        officeManager.start();
        assertTrue(process.isRunning());
        assertTrue(connection.isConnected());
        
        MockOfficeTask task = new MockOfficeTask();
        officeManager.execute(task);
        assertTrue(task.isCompleted());
        
        officeManager.stop();
        assertFalse(connection.isConnected());
        assertFalse(process.isRunning());
        assertEquals(process.getExitCode(0, 0), 0);
    }

    public void restartAfterCrash() throws Exception {
        final PooledOfficeManager officeManager = new PooledOfficeManager(CONNECTION_MODE);
        ManagedOfficeProcess managedOfficeProcess = (ManagedOfficeProcess) ReflectionUtils.getPrivateField(officeManager, "managedOfficeProcess");
        OfficeProcess process = (OfficeProcess) ReflectionUtils.getPrivateField(managedOfficeProcess, "process");
        OfficeConnection connection = (OfficeConnection) ReflectionUtils.getPrivateField(managedOfficeProcess, "connection");
        assertNotNull(connection);
        
        officeManager.start();
        assertTrue(process.isRunning());
        assertTrue(connection.isConnected());
        
        new Thread() {
            public void run() {
                MockOfficeTask badTask = new MockOfficeTask(10 * 1000);
                try {
                    officeManager.execute(badTask);
                    fail("task should be cancelled");
                    //FIXME being in a separate thread the test won't actually fail
                } catch (OfficeException officeException) {
                    assertTrue(officeException.getCause() instanceof CancellationException);
                }
            }
        }.start();
        Thread.sleep(500);
        Process underlyingProcess = (Process) ReflectionUtils.getPrivateField(process, "process");
        assertNotNull(underlyingProcess);
        underlyingProcess.destroy();  // simulate crash

        Thread.sleep(RESTART_WAIT_TIME);
        assertTrue(process.isRunning());
        assertTrue(connection.isConnected());

        MockOfficeTask goodTask = new MockOfficeTask();
        officeManager.execute(goodTask);
        assertTrue(goodTask.isCompleted());

        officeManager.stop();
        assertFalse(connection.isConnected());
        assertFalse(process.isRunning());
        assertEquals(process.getExitCode(0, 0), 0);
    }

    public void restartAfterTaskTimeout() throws Exception {
        PooledOfficeManagerSettings configuration = new PooledOfficeManagerSettings(CONNECTION_MODE);
        configuration.setTaskExecutionTimeout(1500L);
        final PooledOfficeManager officeManager = new PooledOfficeManager(configuration);
        
        ManagedOfficeProcess managedOfficeProcess = (ManagedOfficeProcess) ReflectionUtils.getPrivateField(officeManager, "managedOfficeProcess");
        OfficeProcess process = (OfficeProcess) ReflectionUtils.getPrivateField(managedOfficeProcess, "process");
        OfficeConnection connection = (OfficeConnection) ReflectionUtils.getPrivateField(managedOfficeProcess, "connection");
        assertNotNull(connection);
        
        officeManager.start();
        assertTrue(process.isRunning());
        assertTrue(connection.isConnected());
        
        MockOfficeTask longTask = new MockOfficeTask(2000);
        try {
            officeManager.execute(longTask);
            fail("task should be timed out");
        } catch (OfficeException officeException) {
            assertTrue(officeException.getCause() instanceof TimeoutException);
        }

        Thread.sleep(RESTART_WAIT_TIME);
        assertTrue(process.isRunning());
        assertTrue(connection.isConnected());

        MockOfficeTask goodTask = new MockOfficeTask();
        officeManager.execute(goodTask);
        assertTrue(goodTask.isCompleted());

        officeManager.stop();
        assertFalse(connection.isConnected());
        assertFalse(process.isRunning());
        assertEquals(process.getExitCode(0, 0), 0);
    }

    public void restartWhenMaxTasksPerProcessReached() throws Exception {
        PooledOfficeManagerSettings configuration = new PooledOfficeManagerSettings(CONNECTION_MODE);
        configuration.setMaxTasksPerProcess(3);
        final PooledOfficeManager officeManager = new PooledOfficeManager(configuration);
        
        ManagedOfficeProcess managedOfficeProcess = (ManagedOfficeProcess) ReflectionUtils.getPrivateField(officeManager, "managedOfficeProcess");
        OfficeProcess process = (OfficeProcess) ReflectionUtils.getPrivateField(managedOfficeProcess, "process");
        OfficeConnection connection = (OfficeConnection) ReflectionUtils.getPrivateField(managedOfficeProcess, "connection");
        assertNotNull(connection);
        
        officeManager.start();
        assertTrue(process.isRunning());
        assertTrue(connection.isConnected());
        
        for (int i = 0; i < 3; i++) {
            MockOfficeTask task = new MockOfficeTask();
            officeManager.execute(task);
            assertTrue(task.isCompleted());
            int taskCount = (Integer) ReflectionUtils.getPrivateField(officeManager, "taskCount");
            assertEquals(taskCount, i + 1);
        }

        MockOfficeTask task = new MockOfficeTask();
        officeManager.execute(task);
        assertTrue(task.isCompleted());
        int taskCount = (Integer) ReflectionUtils.getPrivateField(officeManager, "taskCount");
        assertEquals(taskCount, 0);  //FIXME should be 1 to be precise

        officeManager.stop();
        assertFalse(connection.isConnected());
        assertFalse(process.isRunning());
        assertEquals(process.getExitCode(0, 0), 0);
    }

}
