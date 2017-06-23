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

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.sun.star.lib.uno.helper.UnoUrl;

public class OfficeManagerPoolEntryTest {

  private static final org.slf4j.Logger logger =
      LoggerFactory.getLogger(OfficeManagerPoolEntryTest.class);

  private static final UnoUrl CONNECT_URL = UnoUrlUtils.socket(2002);
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

  /**
   * Tests the execution of a task.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void executeTask() throws Exception {

    final OfficeManagerPoolEntryConfig config = new OfficeManagerPoolEntryConfig(CONNECT_URL);
    final OfficeManagerPoolEntry officeManager = new OfficeManagerPoolEntry(config);
    final OfficeProcessManager processManager = officeManager.getOfficeProcessManager();

    try {
      officeManager.start();
      assertTrue(processManager.getOfficeProcess().isRunning());
      assertTrue(processManager.getConnection().isConnected());

      final MockOfficeTask task = new MockOfficeTask();
      officeManager.execute(task);
      assertTrue(task.isCompleted());

    } finally {

      officeManager.stop();
      assertFalse(processManager.getConnection().isConnected());
      assertFalse(processManager.getOfficeProcess().isRunning());
      assertEquals(processManager.getOfficeProcess().getExitCode(0, 0), 0);
    }
  }

  /**
   * Tests that an office process is restarted successfully after a crash.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void restartAfterCrash() throws Exception {

    final OfficeManagerPoolEntryConfig config = new OfficeManagerPoolEntryConfig(CONNECT_URL);
    final OfficeManagerPoolEntry officeManager = new OfficeManagerPoolEntry(config);
    final OfficeProcessManager processManager = officeManager.getOfficeProcessManager();

    assertNotNull(processManager.getConnection());

    try {
      officeManager.start();
      assertTrue(processManager.getOfficeProcess().isRunning());
      assertTrue(processManager.getConnection().isConnected());

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
          (Process)
              FieldUtils.readDeclaredField(processManager.getOfficeProcess(), "process", true);
      assertNotNull(underlyingProcess);
      logger.debug("Simulating the crash");
      underlyingProcess.destroy(); // simulate crash

      assertRestartedAndReconnected(
          processManager.getOfficeProcess(),
          processManager.getConnection(),
          RESTART_INITIAL_WAIT,
          RESTART_WAIT_TIMEOUT);

      final MockOfficeTask goodTask = new MockOfficeTask();
      officeManager.execute(goodTask);
      assertTrue(goodTask.isCompleted());

    } finally {

      officeManager.stop();
      assertFalse(processManager.getConnection().isConnected());
      assertFalse(processManager.getOfficeProcess().isRunning());
      assertEquals(processManager.getOfficeProcess().getExitCode(0, 0), 0);
    }
  }

  /**
   * Tests that an office process is restarted when the execution of a task times out.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void restartAfterTaskTimeout() throws Exception {
    final OfficeManagerPoolEntryConfig config = new OfficeManagerPoolEntryConfig(CONNECT_URL);
    config.setTaskExecutionTimeout(1500L);
    final OfficeManagerPoolEntry officeManager = new OfficeManagerPoolEntry(config);
    final OfficeProcessManager processManager = officeManager.getOfficeProcessManager();

    assertNotNull(processManager.getConnection());

    try {
      officeManager.start();
      assertTrue(processManager.getOfficeProcess().isRunning());
      assertTrue(processManager.getConnection().isConnected());

      final MockOfficeTask task = new MockOfficeTask(2000);
      try {
        officeManager.execute(task);
        fail("task should be timed out");
      } catch (OfficeException officeEx) {
        assertTrue(officeEx.getCause() instanceof TimeoutException);
      }

      assertRestartedAndReconnected(
          processManager.getOfficeProcess(),
          processManager.getConnection(),
          RESTART_INITIAL_WAIT,
          RESTART_WAIT_TIMEOUT);

      assertTrue(processManager.getOfficeProcess().isRunning());
      assertTrue(processManager.getConnection().isConnected());

      final MockOfficeTask goodTask = new MockOfficeTask();
      officeManager.execute(goodTask);
      assertTrue(goodTask.isCompleted());

    } finally {

      officeManager.stop();
      assertFalse(processManager.getConnection().isConnected());
      assertFalse(processManager.getOfficeProcess().isRunning());
      assertEquals(processManager.getOfficeProcess().getExitCode(0, 0), 0);
    }
  }

  /**
   * Tests that an office process is restarted when it reached the maximum number of executed tasks.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void restartWhenMaxTasksPerProcessReached() throws Exception {
    final OfficeManagerPoolEntryConfig config = new OfficeManagerPoolEntryConfig(CONNECT_URL);
    config.setMaxTasksPerProcess(3);
    final OfficeManagerPoolEntry officeManager = new OfficeManagerPoolEntry(config);
    final OfficeProcessManager processManager = officeManager.getOfficeProcessManager();

    assertNotNull(processManager.getConnection());

    try {
      officeManager.start();
      assertTrue(processManager.getOfficeProcess().isRunning());
      assertTrue(processManager.getConnection().isConnected());

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
      assertFalse(processManager.getConnection().isConnected());
      assertFalse(processManager.getOfficeProcess().isRunning());
      assertEquals(processManager.getOfficeProcess().getExitCode(0, 0), 0);
    }
  }
}
