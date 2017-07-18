/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jodconverter.office;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class OfficeManagerPoolEntryITest {

  private static final org.slf4j.Logger logger =
      LoggerFactory.getLogger(OfficeManagerPoolEntryITest.class);

  private static final OfficeUrl CONNECT_URL = new OfficeUrl(2002);
  private static final long RESTART_INITIAL_WAIT = 1000; // 1 Second.
  private static final long RESTART_WAIT_TIMEOUT = 10000; // 10 Seconds.

  private static void assertRestartedAndReconnected(
      final OfficeProcess process,
      final OfficeConnection connection,
      final long initialWait,
      final long timeout)
      throws InterruptedException {

    final long start = System.currentTimeMillis();

    if (initialWait > 0) {
      Thread.sleep(initialWait); // NOSONAR
    }

    final long limit = start + timeout;
    while (System.currentTimeMillis() < limit) {
      if (process.isRunning() && connection.isConnected()) {
        return;
      }

      // Wait a sec
      Thread.sleep(1000); // NOSONAR
    }

    // Times out...
    assertTrue(process.isRunning());
    assertTrue(connection.isConnected());
  }

  private OfficeProcessManager getProcessManager(OfficeManagerPoolEntry manager)
      throws IllegalAccessException {

    return (OfficeProcessManager) FieldUtils.readField(manager, "officeProcessManager", true);
  }

  private int getCurrentTaskCount(OfficeManagerPoolEntry manager) throws IllegalAccessException {

    return ((AtomicInteger) FieldUtils.readField(manager, "taskCount", true)).get();
  }

  private OfficeConnection getConnection(OfficeProcessManager manager)
      throws IllegalAccessException {

    return (OfficeConnection) FieldUtils.readField(manager, "connection", true);
  }

  private OfficeProcess getOfficeProcess(OfficeProcessManager manager)
      throws IllegalAccessException {

    return (OfficeProcess) FieldUtils.readField(manager, "process", true);
  }

  /**
   * Tests the execution of a task.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void executeTask() throws Exception {

    final OfficeManagerPoolEntry officeManager = new OfficeManagerPoolEntry(CONNECT_URL);
    final OfficeProcessManager processManager = getProcessManager(officeManager);

    try {
      officeManager.start();
      assertTrue(getOfficeProcess(processManager).isRunning());
      assertTrue(getConnection(processManager).isConnected());

      final MockOfficeTask task = new MockOfficeTask();
      officeManager.execute(task);
      assertTrue(task.isCompleted());

    } finally {

      officeManager.stop();
      assertFalse(getConnection(processManager).isConnected());
      assertFalse(getOfficeProcess(processManager).isRunning());
      assertEquals(getOfficeProcess(processManager).getExitCode(0, 0), 0);
    }
  }

  private class RestartAfterCrashTask implements Callable<Boolean> {

    private final OfficeManagerPoolEntry officeManager;

    private RestartAfterCrashTask(final OfficeManagerPoolEntry officeManager) {
      super();

      this.officeManager = officeManager;
    }

    @Override
    public Boolean call() throws Exception {
      final MockOfficeTask badTask = new MockOfficeTask(10 * 1000);

      officeManager.execute(badTask);
      return true;
    }
  }

  /**
   * Tests that an office process is restarted successfully after a crash.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void restartAfterCrash() throws Exception {

    final OfficeManagerPoolEntry officeManager = new OfficeManagerPoolEntry(CONNECT_URL);
    final OfficeProcessManager processManager = getProcessManager(officeManager);

    assertNotNull(getConnection(processManager));

    try {
      officeManager.start();
      assertTrue(getOfficeProcess(processManager).isRunning());
      assertTrue(getConnection(processManager).isConnected());

      // Submit the task to an executor
      ExecutorService pool = Executors.newFixedThreadPool(1);
      try {
        Callable<Boolean> task = new RestartAfterCrashTask(officeManager);
        Future<Boolean> future = pool.submit(task);

        Thread.sleep(500); // NOSONAR

        // Simulate crash
        final Process underlyingProcess =
            (Process) FieldUtils.readField(getOfficeProcess(processManager), "process", true);
        assertNotNull(underlyingProcess);
        logger.debug("Simulating the crash");
        underlyingProcess.destroy();

        // Wait until the task is completed
        try {
          future.get();
          fail("Task should be cancelled");
        } catch (ExecutionException ex) {
          assertTrue(ex.getCause() instanceof OfficeException);
          assertTrue(ex.getCause().getCause() instanceof CancellationException);
        }

      } finally {
        pool.shutdownNow();
      }

      assertRestartedAndReconnected(
          getOfficeProcess(processManager),
          getConnection(processManager),
          RESTART_INITIAL_WAIT,
          RESTART_WAIT_TIMEOUT);

      final MockOfficeTask goodTask = new MockOfficeTask();
      officeManager.execute(goodTask);
      assertTrue(goodTask.isCompleted());

    } finally {

      officeManager.stop();
      assertFalse(getConnection(processManager).isConnected());
      assertFalse(getOfficeProcess(processManager).isRunning());
      assertEquals(getOfficeProcess(processManager).getExitCode(0, 0), 0);
    }
  }

  /**
   * Tests that an office process is restarted when the execution of a task times out.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void restartAfterTaskTimeout() throws Exception {
    final OfficeManagerPoolEntryConfig config = new OfficeManagerPoolEntryConfig();
    config.setTaskExecutionTimeout(1500L);
    final OfficeManagerPoolEntry officeManager = new OfficeManagerPoolEntry(CONNECT_URL, config);
    final OfficeProcessManager processManager = getProcessManager(officeManager);

    assertNotNull(getConnection(processManager));

    try {
      officeManager.start();
      assertTrue(getOfficeProcess(processManager).isRunning());
      assertTrue(getConnection(processManager).isConnected());

      final MockOfficeTask task = new MockOfficeTask(2000);
      try {
        officeManager.execute(task);
        fail("task should be timed out");
      } catch (OfficeException officeEx) {
        assertTrue(officeEx.getCause() instanceof TimeoutException);
      }

      assertRestartedAndReconnected(
          getOfficeProcess(processManager),
          getConnection(processManager),
          RESTART_INITIAL_WAIT,
          RESTART_WAIT_TIMEOUT);

      assertTrue(getOfficeProcess(processManager).isRunning());
      assertTrue(getConnection(processManager).isConnected());

      final MockOfficeTask goodTask = new MockOfficeTask();
      officeManager.execute(goodTask);
      assertTrue(goodTask.isCompleted());

    } finally {

      officeManager.stop();
      assertFalse(getConnection(processManager).isConnected());
      assertFalse(getOfficeProcess(processManager).isRunning());
      assertEquals(getOfficeProcess(processManager).getExitCode(0, 0), 0);
    }
  }

  /**
   * Tests that an office process is restarted when it reached the maximum number of executed tasks.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void restartWhenMaxTasksPerProcessReached() throws Exception {
    final OfficeManagerPoolEntryConfig config = new OfficeManagerPoolEntryConfig();
    config.setMaxTasksPerProcess(3);
    final OfficeManagerPoolEntry officeManager = new OfficeManagerPoolEntry(CONNECT_URL, config);
    final OfficeProcessManager processManager = getProcessManager(officeManager);

    assertNotNull(getConnection(processManager));

    try {
      officeManager.start();
      assertTrue(getOfficeProcess(processManager).isRunning());
      assertTrue(getConnection(processManager).isConnected());

      for (int i = 0; i < 3; i++) {
        final MockOfficeTask task = new MockOfficeTask();
        officeManager.execute(task);
        assertTrue(task.isCompleted());
        final int taskCount = getCurrentTaskCount(officeManager);
        assertEquals(taskCount, i + 1);
      }

      final MockOfficeTask task = new MockOfficeTask();
      officeManager.execute(task);
      assertTrue(task.isCompleted());
      final int taskCount = getCurrentTaskCount(officeManager);
      assertEquals(taskCount, 1);

    } finally {

      officeManager.stop();
      assertFalse(getConnection(processManager).isConnected());
      assertFalse(getOfficeProcess(processManager).isRunning());
      assertEquals(getOfficeProcess(processManager).getExitCode(0, 0), 0);
    }
  }
}
