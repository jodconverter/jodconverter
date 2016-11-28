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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.sun.star.lib.uno.helper.UnoUrl;

@Test(groups = "integration")
public class PooledOfficeManagerTest {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PooledOfficeManagerTest.class);

    private static final UnoUrl CONNECTION_MODE = UnoUrlUtils.socket(2002);
    //private static final long RESTART_WAIT_TIME = 2 * 1000;
    private static final long RESTART_WAIT_TIME = 5 * 1000; // 2 seconds is not enough...

    public void executeTask() throws Exception {

        final PooledOfficeManagerSettings settings = new PooledOfficeManagerSettings(CONNECTION_MODE);
        settings.setProcessManager(OfficeUtils.findBestProcessManager());
        final PooledOfficeManager officeManager = new PooledOfficeManager(settings);
        ManagedOfficeProcess managedOfficeProcess = (ManagedOfficeProcess) FieldUtils.readDeclaredField(officeManager, "managedOfficeProcess", true);
        OfficeProcess process = (OfficeProcess) FieldUtils.readDeclaredField(managedOfficeProcess, "process", true);
        OfficeConnection connection = (OfficeConnection) FieldUtils.readDeclaredField(managedOfficeProcess, "connection", true);

        try {
            officeManager.start();
            assertTrue(process.isRunning());
            assertTrue(connection.isConnected());

            MockOfficeTask task = new MockOfficeTask();
            officeManager.execute(task);
            assertTrue(task.isCompleted());

        } finally {
            officeManager.stop();
            assertFalse(connection.isConnected());
            assertFalse(process.isRunning());
            assertEquals(process.getExitCode(0, 0), 0);
        }
    }

    public void restartAfterCrash() throws Exception {

        final PooledOfficeManagerSettings settings = new PooledOfficeManagerSettings(CONNECTION_MODE);
        settings.setProcessManager(OfficeUtils.findBestProcessManager());
        final PooledOfficeManager officeManager = new PooledOfficeManager(settings);
        ManagedOfficeProcess managedOfficeProcess = (ManagedOfficeProcess) FieldUtils.readDeclaredField(officeManager, "managedOfficeProcess", true);
        OfficeProcess process = (OfficeProcess) FieldUtils.readDeclaredField(managedOfficeProcess, "process", true);
        OfficeConnection connection = (OfficeConnection) FieldUtils.readDeclaredField(managedOfficeProcess, "connection", true);
        assertNotNull(connection);

        try {
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
                    } catch (OfficeException officeEx) {
                        assertTrue(officeEx.getCause() instanceof CancellationException);
                    }
                }
            }.start();
            Thread.sleep(500);
            Process underlyingProcess = (Process) FieldUtils.readDeclaredField(process, "process", true);
            assertNotNull(underlyingProcess);
            logger.debug("Simulating the crash");
            underlyingProcess.destroy(); // simulate crash

            Thread.sleep(RESTART_WAIT_TIME);
            assertTrue(process.isRunning());
            assertTrue(connection.isConnected());

            MockOfficeTask goodTask = new MockOfficeTask();
            officeManager.execute(goodTask);
            assertTrue(goodTask.isCompleted());

        } finally {
            officeManager.stop();
            assertFalse(connection.isConnected());
            assertFalse(process.isRunning());
            assertEquals(process.getExitCode(0, 0), 0);
        }
    }

    public void restartAfterTaskTimeout() throws Exception {
        final PooledOfficeManagerSettings settings = new PooledOfficeManagerSettings(CONNECTION_MODE);
        settings.setProcessManager(OfficeUtils.findBestProcessManager());
        settings.setTaskExecutionTimeout(1500L);
        final PooledOfficeManager officeManager = new PooledOfficeManager(settings);

        ManagedOfficeProcess managedOfficeProcess = (ManagedOfficeProcess) FieldUtils.readDeclaredField(officeManager, "managedOfficeProcess", true);
        OfficeProcess process = (OfficeProcess) FieldUtils.readDeclaredField(managedOfficeProcess, "process", true);
        OfficeConnection connection = (OfficeConnection) FieldUtils.readDeclaredField(managedOfficeProcess, "connection", true);
        assertNotNull(connection);

        try {
            officeManager.start();
            assertTrue(process.isRunning());
            assertTrue(connection.isConnected());

            MockOfficeTask task = new MockOfficeTask(2000);
            try {
                officeManager.execute(task);
                fail("task should be timed out");
            } catch (OfficeException officeEx) {
                assertTrue(officeEx.getCause() instanceof TimeoutException);
            }

            Thread.sleep(RESTART_WAIT_TIME);

            assertTrue(process.isRunning());
            assertTrue(connection.isConnected());

            MockOfficeTask goodTask = new MockOfficeTask();
            officeManager.execute(goodTask);
            assertTrue(goodTask.isCompleted());
        } finally {

            officeManager.stop();
            assertFalse(connection.isConnected());
            assertFalse(process.isRunning());
            assertEquals(process.getExitCode(0, 0), 0);
        }
    }

    public void restartWhenMaxTasksPerProcessReached() throws Exception {
        PooledOfficeManagerSettings configuration = new PooledOfficeManagerSettings(CONNECTION_MODE);
        configuration.setMaxTasksPerProcess(3);
        final PooledOfficeManager officeManager = new PooledOfficeManager(configuration);

        ManagedOfficeProcess managedOfficeProcess = (ManagedOfficeProcess) FieldUtils.readDeclaredField(officeManager, "managedOfficeProcess", true);
        OfficeProcess process = (OfficeProcess) FieldUtils.readDeclaredField(managedOfficeProcess, "process", true);
        OfficeConnection connection = (OfficeConnection) FieldUtils.readDeclaredField(managedOfficeProcess, "connection", true);
        assertNotNull(connection);

        try {
            officeManager.start();
            assertTrue(process.isRunning());
            assertTrue(connection.isConnected());

            for (int i = 0; i < 3; i++) {
                MockOfficeTask task = new MockOfficeTask();
                officeManager.execute(task);
                assertTrue(task.isCompleted());
                int taskCount = ((AtomicInteger) FieldUtils.readDeclaredField(officeManager, "taskCount", true)).get();
                assertEquals(taskCount, i + 1);
            }

            MockOfficeTask task = new MockOfficeTask();
            officeManager.execute(task);
            assertTrue(task.isCompleted());
            int taskCount = ((AtomicInteger) FieldUtils.readDeclaredField(officeManager, "taskCount", true)).get();
            assertEquals(taskCount, 1);

        } finally {
            officeManager.stop();
            assertFalse(connection.isConnected());
            assertFalse(process.isRunning());
            assertEquals(process.getExitCode(0, 0), 0);
        }
    }

}
