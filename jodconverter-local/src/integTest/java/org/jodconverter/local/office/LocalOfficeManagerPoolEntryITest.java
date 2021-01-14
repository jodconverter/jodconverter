/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

package org.jodconverter.local.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.local.office.LocalOfficeManager.*;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.test.util.TestUtil;

/** Contains tests for the {@link LocalOfficeManagerPoolEntry} class. */
class LocalOfficeManagerPoolEntryITest {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LocalOfficeManagerPoolEntryITest.class);

  private static final OfficeUrl CONNECT_URL = new OfficeUrl(2002);
  private static final long START_INITIAL_WAIT = 2_000L; // 2 Seconds.
  private static final long START_WAIT_TIMEOUT = 15_000L; // 30 Seconds.
  private static final long STOP_INITIAL_WAIT = 2_000L; // 2 Seconds.
  private static final long STOP_WAIT_TIMEOUT = 15_000L; // 30 Seconds.

  @Nested
  class Execute {

    @Test
    void whenEverythingWorksFine_ShouldSucceed() throws OfficeException {

      final LocalOfficeManagerPoolEntry poolEntry =
          new LocalOfficeManagerPoolEntry(
              DEFAULT_MAX_TASKS_PER_PROCESS,
              DEFAULT_TASK_EXECUTION_TIMEOUT,
              new LocalOfficeProcessManager(
                  CONNECT_URL,
                  LocalOfficeUtils.getDefaultOfficeHome(),
                  OfficeUtils.getDefaultWorkingDir(),
                  LocalOfficeUtils.findBestProcessManager(),
                  new ArrayList<>(),
                  null,
                  DEFAULT_PROCESS_TIMEOUT,
                  DEFAULT_PROCESS_RETRY_INTERVAL,
                  DEFAULT_AFTER_START_PROCESS_DELAY,
                  DEFAULT_EXISTING_PROCESS_ACTION,
                  DEFAULT_START_FAIL_FAST,
                  DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
                  DEFAULT_DISABLE_OPENGL,
                  new OfficeConnection(CONNECT_URL)));
      try {
        poolEntry.start();
        assertStartedAndConnected(poolEntry);

        final MockOfficeTask task = new MockOfficeTask();
        poolEntry.execute(task);
        assertThat(task.isCompleted()).isTrue();

      } finally {

        poolEntry.stop();
        assertStoppedAndDisconnected(poolEntry);
      }
    }

    /** Tests that an office process is restarted successfully after a crash. */
    @Test
    void whenOfficeProcessCrash_ShouldRestartAfterCrash() throws OfficeException {

      final OfficeConnection connection = new OfficeConnection(CONNECT_URL);
      final LocalOfficeProcessManager processManager =
          new LocalOfficeProcessManager(
              CONNECT_URL,
              LocalOfficeUtils.getDefaultOfficeHome(),
              OfficeUtils.getDefaultWorkingDir(),
              LocalOfficeUtils.findBestProcessManager(),
              new ArrayList<>(),
              null,
              DEFAULT_PROCESS_TIMEOUT,
              DEFAULT_PROCESS_RETRY_INTERVAL,
              DEFAULT_AFTER_START_PROCESS_DELAY,
              DEFAULT_EXISTING_PROCESS_ACTION,
              DEFAULT_START_FAIL_FAST,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              DEFAULT_DISABLE_OPENGL,
              connection);
      final LocalOfficeManagerPoolEntry poolEntry =
          new LocalOfficeManagerPoolEntry(
              DEFAULT_MAX_TASKS_PER_PROCESS, DEFAULT_TASK_EXECUTION_TIMEOUT, processManager);
      try {
        poolEntry.start();
        assertStartedAndConnected(poolEntry);

        // Submit the task to an executor
        final ExecutorService pool = Executors.newFixedThreadPool(1);
        try {
          final Callable<Boolean> task = new RestartAfterCrashTask(poolEntry);
          final Future<Boolean> future = pool.submit(task);

          TestUtil.sleepQuietly(500L);

          // Simulate crash
          final VerboseProcess verboseProcess =
              Whitebox.getInternalState(processManager, "process");
          final Process underlyingProcess = verboseProcess.getProcess();
          assertThat(underlyingProcess).isNotNull();
          LOGGER.debug("Simulating the crash");
          underlyingProcess.destroy();

          // Wait until the task is completed
          assertThatExceptionOfType(ExecutionException.class)
              .isThrownBy(future::get)
              .satisfies(
                  e -> {
                    assertThat(e.getCause()).isInstanceOf(OfficeException.class);
                    assertThat(e.getCause().getCause()).isInstanceOf(CancellationException.class);
                  });

        } finally {
          pool.shutdownNow();
        }

        assertStartedAndConnected(poolEntry);

        final MockOfficeTask goodTask = new MockOfficeTask();
        poolEntry.execute(goodTask);
        assertThat(goodTask.isCompleted()).isTrue();

      } finally {

        poolEntry.stop();
        assertStoppedAndDisconnected(poolEntry);
      }
    }

    /** Tests that an office process is restarted when the execution of a task times out. */
    @Test
    void whenTimeoutExceptionOccured_ShouldRestartAfterTaskTimeout() throws OfficeException {

      final LocalOfficeManagerPoolEntry poolEntry =
          new LocalOfficeManagerPoolEntry(
              DEFAULT_MAX_TASKS_PER_PROCESS,
              1_500L,
              new LocalOfficeProcessManager(
                  CONNECT_URL,
                  LocalOfficeUtils.getDefaultOfficeHome(),
                  OfficeUtils.getDefaultWorkingDir(),
                  LocalOfficeUtils.findBestProcessManager(),
                  new ArrayList<>(),
                  null,
                  DEFAULT_PROCESS_TIMEOUT,
                  DEFAULT_PROCESS_RETRY_INTERVAL,
                  DEFAULT_AFTER_START_PROCESS_DELAY,
                  DEFAULT_EXISTING_PROCESS_ACTION,
                  DEFAULT_START_FAIL_FAST,
                  DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
                  DEFAULT_DISABLE_OPENGL,
                  new OfficeConnection(CONNECT_URL)));
      try {
        poolEntry.start();
        assertStartedAndConnected(poolEntry);

        final MockOfficeTask task = new MockOfficeTask(2_000L);

        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(() -> poolEntry.execute(task))
            .withCauseExactlyInstanceOf(TimeoutException.class);

        // TODO: How to validate that the process has been restarted ?

        assertStartedAndConnected(poolEntry);

        final MockOfficeTask goodTask = new MockOfficeTask();
        poolEntry.execute(goodTask);
        assertThat(goodTask.isCompleted()).isTrue();

      } finally {

        poolEntry.stop();
        assertStoppedAndDisconnected(poolEntry);
      }
    }

    /**
     * Tests that an office process is restarted when it reached the maximum number of executed
     * tasks.
     */
    @Test
    void whenMaxTasksPerProcessReached_ShouldRestart() throws OfficeException {

      final LocalOfficeManagerPoolEntry poolEntry =
          new LocalOfficeManagerPoolEntry(
              3,
              DEFAULT_TASK_EXECUTION_TIMEOUT,
              new LocalOfficeProcessManager(
                  CONNECT_URL,
                  LocalOfficeUtils.getDefaultOfficeHome(),
                  OfficeUtils.getDefaultWorkingDir(),
                  LocalOfficeUtils.findBestProcessManager(),
                  new ArrayList<>(),
                  null,
                  DEFAULT_PROCESS_TIMEOUT,
                  DEFAULT_PROCESS_RETRY_INTERVAL,
                  DEFAULT_AFTER_START_PROCESS_DELAY,
                  DEFAULT_EXISTING_PROCESS_ACTION,
                  DEFAULT_START_FAIL_FAST,
                  DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
                  DEFAULT_DISABLE_OPENGL,
                  new OfficeConnection(CONNECT_URL)));
      try {
        poolEntry.start();
        assertStartedAndConnected(poolEntry);

        for (int i = 0; i < 3; i++) {
          final MockOfficeTask task = new MockOfficeTask();
          poolEntry.execute(task);
          assertThat(task.isCompleted()).isTrue();
          assertThat(poolEntry).extracting("taskCount.value").isEqualTo(i + 1);
        }

        final MockOfficeTask task = new MockOfficeTask();
        poolEntry.execute(task);
        assertThat(task.isCompleted()).isTrue();
        assertThat(poolEntry).extracting("taskCount.value").isEqualTo(1);

      } finally {

        poolEntry.stop();
        assertStoppedAndDisconnected(poolEntry);
      }
    }
  }

  private static void assertStartedAndConnected(final LocalOfficeManagerPoolEntry manager) {

    final long start = System.currentTimeMillis();

    TestUtil.sleepQuietly(STOP_INITIAL_WAIT);

    final long limit = start + STOP_WAIT_TIMEOUT;
    while (System.currentTimeMillis() < limit) {
      if (manager.isRunning()) {
        return;
      }

      // Wait a sec
      TestUtil.sleepQuietly(1_000L);
    }

    // Times out or connected...
    assertThat(manager).extracting("officeProcessManager.connection.connected").isEqualTo(true);
    assertThat(manager).extracting("officeProcessManager.process.exitCode").isNull();
  }

  private static void assertStoppedAndDisconnected(final LocalOfficeManagerPoolEntry manager) {

    final long start = System.currentTimeMillis();

    TestUtil.sleepQuietly(START_INITIAL_WAIT);

    final long limit = start + START_WAIT_TIMEOUT;
    while (System.currentTimeMillis() < limit) {
      if (!manager.isRunning()) {
        return;
      }

      // Wait a sec
      TestUtil.sleepQuietly(1_000L);
    }

    // Times out or disconnected...
    assertThat(manager.isRunning()).isFalse();
    assertThat(manager).extracting("officeProcessManager.connection.connected").isEqualTo(false);
    assertThat(manager).extracting("officeProcessManager.process.exitCode").isEqualTo(0);
  }

  private static class RestartAfterCrashTask implements Callable<Boolean> {

    private final LocalOfficeManagerPoolEntry officeManager;

    private RestartAfterCrashTask(final LocalOfficeManagerPoolEntry officeManager) {
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
}
