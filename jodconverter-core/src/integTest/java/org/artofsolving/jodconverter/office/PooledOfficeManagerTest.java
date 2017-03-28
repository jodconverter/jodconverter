/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.office;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.sun.star.lib.uno.helper.UnoUrl;

public class PooledOfficeManagerTest {

  private static final org.slf4j.Logger logger =
      LoggerFactory.getLogger(PooledOfficeManagerTest.class);

  private static final UnoUrl CONNECTION_MODE = UnoUrlUtils.socket(2002);
  //private static final long RESTART_WAIT_TIME = 2 * 1000;
  private static final long RESTART_WAIT_TIME = 5 * 1000; // 2 seconds is not enough...

  /**
   * Tests the execution of a task.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void executeTask() throws Exception {

    final PooledOfficeManagerSettings settings = new PooledOfficeManagerSettings(CONNECTION_MODE);
    settings.setProcessManager(OfficeUtils.findBestProcessManager());
    final PooledOfficeManager officeManager = new PooledOfficeManager(settings);
    final ManagedOfficeProcess managedOfficeProcess = officeManager.getManagedOfficeProcess();
    final OfficeProcess process = managedOfficeProcess.getOfficeProcess();
    final OfficeConnection connection = managedOfficeProcess.getConnection();

    try {
      officeManager.start();
      assertTrue(process.isRunning());
      assertTrue(connection.isConnected());

      final MockOfficeTask task = new MockOfficeTask();
      officeManager.execute(task);
      assertTrue(task.isCompleted());

    } finally {

      officeManager.stop();
      assertFalse(connection.isConnected());
      assertFalse(process.isRunning());
      assertEquals(process.getExitCode(0, 0), 0);
    }
  }

  /**
   * Tests that an office process is restarted successfully after a crash.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void restartAfterCrash() throws Exception {

    final PooledOfficeManagerSettings settings = new PooledOfficeManagerSettings(CONNECTION_MODE);
    settings.setProcessManager(OfficeUtils.findBestProcessManager());
    final PooledOfficeManager officeManager = new PooledOfficeManager(settings);
    final ManagedOfficeProcess managedOfficeProcess = officeManager.getManagedOfficeProcess();
    final OfficeProcess process = managedOfficeProcess.getOfficeProcess();
    final OfficeConnection connection = managedOfficeProcess.getConnection();
    assertNotNull(connection);

    try {
      officeManager.start();
      assertTrue(process.isRunning());
      assertTrue(connection.isConnected());

      new Thread() {
        public void run() {
          final MockOfficeTask badTask = new MockOfficeTask(10 * 1000);
          try {
            officeManager.execute(badTask);
            fail("task should be cancelled");
            //FIXME being in a separate thread the test won't actually fail
          } catch (OfficeException officeEx) {
            assertTrue(officeEx.getCause() instanceof CancellationException);
          }
        }
      }.start();
      Thread.sleep(500); // NOSONAR
      final Process underlyingProcess =
          (Process) FieldUtils.readDeclaredField(process, "process", true);
      assertNotNull(underlyingProcess);
      logger.debug("Simulating the crash");
      underlyingProcess.destroy(); // simulate crash

      Thread.sleep(RESTART_WAIT_TIME); // NOSONAR
      assertTrue(process.isRunning());
      assertTrue(connection.isConnected());

      final MockOfficeTask goodTask = new MockOfficeTask();
      officeManager.execute(goodTask);
      assertTrue(goodTask.isCompleted());

    } finally {

      officeManager.stop();
      assertFalse(connection.isConnected());
      assertFalse(process.isRunning());
      assertEquals(process.getExitCode(0, 0), 0);
    }
  }

  /**
   * Tests that an office process is restarted when the execution of a task times out.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void restartAfterTaskTimeout() throws Exception {
    final PooledOfficeManagerSettings settings = new PooledOfficeManagerSettings(CONNECTION_MODE);
    settings.setProcessManager(OfficeUtils.findBestProcessManager());
    settings.setTaskExecutionTimeout(1500L);
    final PooledOfficeManager officeManager = new PooledOfficeManager(settings);
    final ManagedOfficeProcess managedOfficeProcess = officeManager.getManagedOfficeProcess();
    final OfficeProcess process = managedOfficeProcess.getOfficeProcess();
    final OfficeConnection connection = managedOfficeProcess.getConnection();
    assertNotNull(connection);

    try {
      officeManager.start();
      assertTrue(process.isRunning());
      assertTrue(connection.isConnected());

      final MockOfficeTask task = new MockOfficeTask(2000);
      try {
        officeManager.execute(task);
        fail("task should be timed out");
      } catch (OfficeException officeEx) {
        assertTrue(officeEx.getCause() instanceof TimeoutException);
      }

      Thread.sleep(RESTART_WAIT_TIME); // NOSONAR

      assertTrue(process.isRunning());
      assertTrue(connection.isConnected());

      final MockOfficeTask goodTask = new MockOfficeTask();
      officeManager.execute(goodTask);
      assertTrue(goodTask.isCompleted());

    } finally {

      officeManager.stop();
      assertFalse(connection.isConnected());
      assertFalse(process.isRunning());
      assertEquals(process.getExitCode(0, 0), 0);
    }
  }

  /**
   * Tests that an office process is restarted when it reached the maximum number of executed tasks.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void restartWhenMaxTasksPerProcessReached() throws Exception {
    final PooledOfficeManagerSettings configuration =
        new PooledOfficeManagerSettings(CONNECTION_MODE);
    configuration.setMaxTasksPerProcess(3);
    final PooledOfficeManager officeManager = new PooledOfficeManager(configuration);
    final ManagedOfficeProcess managedOfficeProcess = officeManager.getManagedOfficeProcess();
    final OfficeProcess process = managedOfficeProcess.getOfficeProcess();
    final OfficeConnection connection = managedOfficeProcess.getConnection();
    assertNotNull(connection);

    try {
      officeManager.start();
      assertTrue(process.isRunning());
      assertTrue(connection.isConnected());

      for (int i = 0; i < 3; i++) {
        final MockOfficeTask task = new MockOfficeTask();
        officeManager.execute(task);
        assertTrue(task.isCompleted());
        final int taskCount = officeManager.getCurrentTaskCount();
        assertEquals(taskCount, i + 1);
      }

      final MockOfficeTask task = new MockOfficeTask();
      officeManager.execute(task);
      assertTrue(task.isCompleted());
      final int taskCount = officeManager.getCurrentTaskCount();
      assertEquals(taskCount, 1);

    } finally {

      officeManager.stop();
      assertFalse(connection.isConnected());
      assertFalse(process.isRunning());
      assertEquals(process.getExitCode(0, 0), 0);
    }
  }
}
