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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

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
      final OfficeManagerPoolEntry officeManager, final long initialWait, final long timeout)
      throws Exception {

    final long start = System.currentTimeMillis();

    if (initialWait > 0) {
      Thread.sleep(initialWait); // NOSONAR
    }

    final long limit = start + timeout;
    while (System.currentTimeMillis() < limit) {
      if (getOfficeProcess(officeManager).isRunning()
          && getConnection(officeManager).isConnected()) {
        return;
      }

      // Wait a sec
      Thread.sleep(1000); // NOSONAR
    }

    // Times out...
    assertThat(officeManager)
        .extracting(
            "officeProcessManager.process.running", "officeProcessManager.connection.connected")
        .containsExactly(true, true);
  }

  private static OfficeProcess getOfficeProcess(final OfficeManagerPoolEntry manager)
      throws IllegalAccessException {

    final OfficeProcessManager processManager =
        (OfficeProcessManager) FieldUtils.readField(manager, "officeProcessManager", true);
    return (OfficeProcess) FieldUtils.readField(processManager, "process", true);
  }

  private static OfficeConnection getConnection(final OfficeManagerPoolEntry manager)
      throws IllegalAccessException {

    final OfficeProcessManager processManager =
        (OfficeProcessManager) FieldUtils.readField(manager, "officeProcessManager", true);
    return (OfficeConnection) FieldUtils.readField(processManager, "connection", true);
  }

  /**
   * Tests the execution of a task.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void executeTask() throws Exception {

    final OfficeManagerPoolEntry officeManager = new OfficeManagerPoolEntry(CONNECT_URL);

    try {
      officeManager.start();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      final MockOfficeTask task = new MockOfficeTask();
      officeManager.execute(task);
      assertThat(task.isCompleted()).isTrue();

    } finally {

      officeManager.stop();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(officeManager).getExitCode(0, 0)).isEqualTo(0);
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

    try {
      officeManager.start();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      // Submit the task to an executor
      ExecutorService pool = Executors.newFixedThreadPool(1);
      try {
        Callable<Boolean> task = new RestartAfterCrashTask(officeManager);
        Future<Boolean> future = pool.submit(task);

        Thread.sleep(500); // NOSONAR

        // Simulate crash
        final Process underlyingProcess =
            (Process) FieldUtils.readField(getOfficeProcess(officeManager), "process", true);
        assertThat(underlyingProcess).isNotNull();
        logger.debug("Simulating the crash");
        underlyingProcess.destroy();

        // Wait until the task is completed
        try {
          future.get();
          fail("Task should be cancelled");
        } catch (ExecutionException ex) {
          assertThat(ex.getCause()).isInstanceOf(OfficeException.class);
          assertThat(ex.getCause().getCause()).isInstanceOf(CancellationException.class);
        }

      } finally {
        pool.shutdownNow();
      }

      assertRestartedAndReconnected(officeManager, RESTART_INITIAL_WAIT, RESTART_WAIT_TIMEOUT);

      final MockOfficeTask goodTask = new MockOfficeTask();
      officeManager.execute(goodTask);
      assertThat(goodTask.isCompleted()).isTrue();

    } finally {

      officeManager.stop();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(officeManager).getExitCode(0, 0)).isEqualTo(0);
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

    try {
      officeManager.start();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      final MockOfficeTask task = new MockOfficeTask(2000);
      try {
        officeManager.execute(task);
        fail("task should be timed out");
      } catch (OfficeException officeEx) {
        assertThat(officeEx.getCause()).isInstanceOf(TimeoutException.class);
      }

      assertRestartedAndReconnected(officeManager, RESTART_INITIAL_WAIT, RESTART_WAIT_TIMEOUT);

      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      final MockOfficeTask goodTask = new MockOfficeTask();
      officeManager.execute(goodTask);
      assertThat(goodTask.isCompleted()).isTrue();

    } finally {

      officeManager.stop();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(officeManager).getExitCode(0, 0)).isEqualTo(0);
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

    try {
      officeManager.start();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      for (int i = 0; i < 3; i++) {
        final MockOfficeTask task = new MockOfficeTask();
        officeManager.execute(task);
        assertThat(task.isCompleted()).isTrue();
        assertThat(officeManager).extracting("taskCount.value").containsExactly(i + 1);
      }

      final MockOfficeTask task = new MockOfficeTask();
      officeManager.execute(task);
      assertThat(task.isCompleted()).isTrue();
      assertThat(officeManager).extracting("taskCount.value").containsExactly(1);

    } finally {

      officeManager.stop();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(officeManager).getExitCode(0, 0)).isEqualTo(0);
    }
  }
}
