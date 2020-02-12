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

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.sun.star.frame.TerminationVetoException;
import com.sun.star.frame.XTerminateListener;
import com.sun.star.lang.EventObject;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.office.RetryTimeoutException;
import org.jodconverter.core.test.util.TestUtil;
import org.jodconverter.local.process.PureJavaProcessManager;

/** Contains tests for the {@link OfficeProcessManagerPoolEntry} class. */
public class OfficeProcessManagerPoolEntryITest {

  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(OfficeProcessManagerPoolEntryITest.class);

  private static final OfficeUrl CONNECT_URL = new OfficeUrl(2002);
  private static final long RESTART_INITIAL_WAIT = 5_000L; // 5 Seconds.
  private static final long RESTART_WAIT_TIMEOUT = 15_000L; // 10 Seconds.

  private static class VetoTerminateListener implements XTerminateListener {

    @Override
    public void disposing(final EventObject event) {
      // Nothing to do here
    }

    @Override
    public void queryTermination(final EventObject event) throws TerminationVetoException {

      // This will prevent a clean termination
      throw new TerminationVetoException();
    }

    @Override
    public void notifyTermination(final EventObject event) {
      // Nothing to do here
    }
  }

  private static OfficeProcess startOfficeProcess() throws OfficeException {

    // Starts an office process
    final OfficeProcess process =
        new OfficeProcess(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            null);
    process.start();
    TestUtil.sleepQuietly(2_000L);
    final Integer exitCode = process.getExitCode();
    if (exitCode != null && exitCode.equals(81)) {
      process.start(true);
      TestUtil.sleepQuietly(2_000L);
    }
    return process;
  }

  private static void assertRestartedAndReconnected(
      final OfficeProcessManagerPoolEntry officeManager) {

    final long start = System.currentTimeMillis();

    TestUtil.sleepQuietly(RESTART_INITIAL_WAIT);

    final long limit = start + RESTART_WAIT_TIMEOUT;
    while (System.currentTimeMillis() < limit) {
      if (getOfficeProcess(officeManager).isRunning()
          && getConnection(officeManager).isConnected()) {
        return;
      }

      // Wait a sec
      TestUtil.sleepQuietly(1_000L);
    }

    // Times out...
    assertThat(officeManager)
        .extracting(
            "officeProcessManager.process.running", "officeProcessManager.connection.connected")
        .containsExactly(true, true);
  }

  private static OfficeProcess getOfficeProcess(final OfficeProcessManagerPoolEntry manager) {
    return Whitebox.getInternalState(
        Whitebox.getInternalState(manager, "officeProcessManager"), "process");
  }

  private static OfficeConnection getConnection(final OfficeProcessManagerPoolEntry manager) {
    return Whitebox.getInternalState(
        Whitebox.getInternalState(manager, "officeProcessManager"), "connection");
  }

  /** Tests the execution of a task. */
  @Test
  public void executeTask() throws OfficeException, RetryTimeoutException {

    final OfficeProcessManagerPoolEntry manager =
        new OfficeProcessManagerPoolEntry(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    try {
      manager.start();
      assertThat(manager.isRunning()).isTrue();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      final MockOfficeTask task = new MockOfficeTask();
      manager.execute(task);
      assertThat(task.isCompleted()).isTrue();

    } finally {

      manager.stop();
      assertThat(manager.isRunning()).isFalse();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(manager).getExitCode(0, 0)).isEqualTo(0);
    }
  }

  private static class RestartAfterCrashTask implements Callable<Boolean> {

    private final OfficeProcessManagerPoolEntry officeManager;

    private RestartAfterCrashTask(final OfficeProcessManagerPoolEntry officeManager) {
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

  /** Tests that an office process is restarted successfully after a crash. */
  @Test
  public void execute_WhenOfficeProcessCrash_ShouldRestartAfterCrash()
      throws OfficeException, RetryTimeoutException {

    final OfficeProcessManagerPoolEntry manager =
        new OfficeProcessManagerPoolEntry(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    try {
      manager.start();
      assertThat(manager.isRunning()).isTrue();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      // Submit the task to an executor
      final ExecutorService pool = Executors.newFixedThreadPool(1);
      try {
        final Callable<Boolean> task = new RestartAfterCrashTask(manager);
        final Future<Boolean> future = pool.submit(task);

        TestUtil.sleepQuietly(500L);

        // Simulate crash
        final VerboseProcess verboseProcess =
            Whitebox.getInternalState(getOfficeProcess(manager), "process");
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

      assertRestartedAndReconnected(manager);

      final MockOfficeTask goodTask = new MockOfficeTask();
      manager.execute(goodTask);
      assertThat(goodTask.isCompleted()).isTrue();

    } finally {

      manager.stop();
      assertThat(manager.isRunning()).isFalse();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(manager).getExitCode(0, 0)).isEqualTo(0);
    }
  }

  /** Tests that an office process is restarted when the execution of a task times out. */
  @Test
  public void execute_WhenTimeoutExceptionOccured_ShouldRestartAfterTaskTimeout()
      throws OfficeException, RetryTimeoutException {

    final OfficeProcessManagerPoolEntry manager =
        new OfficeProcessManagerPoolEntry(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            null,
            null,
            null,
            1_500L,
            null,
            null);
    try {
      manager.start();
      assertThat(manager.isRunning()).isTrue();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      final MockOfficeTask task = new MockOfficeTask(2_000L);

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> manager.execute(task))
          .withCauseExactlyInstanceOf(TimeoutException.class);

      assertRestartedAndReconnected(manager);

      final MockOfficeTask goodTask = new MockOfficeTask();
      manager.execute(goodTask);
      assertThat(goodTask.isCompleted()).isTrue();

    } finally {

      manager.stop();
      assertThat(manager.isRunning()).isFalse();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(manager).getExitCode(0, 0)).isEqualTo(0);
    }
  }

  /**
   * Tests that an office process is restarted when it reached the maximum number of executed tasks.
   */
  @Test
  public void execute_WhenMaxTasksPerProcessReached_ShouldRestart()
      throws OfficeException, RetryTimeoutException {

    final OfficeProcessManagerPoolEntry manager =
        new OfficeProcessManagerPoolEntry(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            null,
            null,
            null,
            null,
            3,
            null);
    try {
      manager.start();
      assertThat(manager.isRunning()).isTrue();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      for (int i = 0; i < 3; i++) {
        final MockOfficeTask task = new MockOfficeTask();
        manager.execute(task);
        assertThat(task.isCompleted()).isTrue();
        assertThat(manager).extracting("taskCount.value").isEqualTo(i + 1);
      }

      final MockOfficeTask task = new MockOfficeTask();
      manager.execute(task);
      assertThat(task.isCompleted()).isTrue();
      assertThat(manager).extracting("taskCount.value").isEqualTo(1);

    } finally {

      manager.stop();
      assertThat(manager.isRunning()).isFalse();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(manager).getExitCode(0, 0)).isEqualTo(0);
    }
  }

  @Test
  public void start_WhenProcessAlreadyExistsAndKillExistingProcessOn_ShouldKillExistingProcess()
      throws OfficeException, RetryTimeoutException {

    // Starts an office process
    final OfficeProcess process = startOfficeProcess();

    final OfficeProcessManagerPoolEntry manager =
        new OfficeProcessManagerPoolEntry(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    try {
      manager.start();
      assertThat(manager.isRunning()).isTrue();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

    } finally {

      manager.stop();
      assertThat(manager.isRunning()).isFalse();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(manager).getExitCode(0, 0)).isEqualTo(0);

      process.forciblyTerminate(1_000L, 5_000L);
      process.deleteInstanceProfileDir();
    }
  }

  @Test
  public void start_WhenProcessAlreadyExistsAndKillExistingProcessOff_ShouldThrowOfficeException()
      throws OfficeException, RetryTimeoutException {

    // Starts an office process
    final OfficeProcess process = startOfficeProcess();

    final OfficeProcessManagerPoolEntry manager =
        new OfficeProcessManagerPoolEntry(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null);
    try {

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(manager::start)
          .withMessageMatching("A process with --accept.*is already running.*");

    } finally {

      assertThat(manager.isRunning()).isFalse();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);

      process.forciblyTerminate(1_000L, 5_000L);
      process.deleteInstanceProfileDir();
    }
  }

  @Test
  public void start_WithCustomProfileDir_ShouldCopyProfileDirToWorkingDir()
      throws OfficeException, RetryTimeoutException {

    final OfficeProcessManagerPoolEntry manager =
        new OfficeProcessManagerPoolEntry(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            null,
            new File("src/integTest/resources/templateProfileDir"),
            null,
            null,
            null,
            null,
            null,
            null);
    try {
      manager.start();
      assertThat(manager.isRunning()).isTrue();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      // Check the profile dir existence
      final File instanceProfileDir =
          Whitebox.getInternalState(getOfficeProcess(manager), "instanceProfileDir");
      assertThat(new File(instanceProfileDir, "user/customFile")).isFile();

    } finally {

      manager.stop();
      assertThat(manager.isRunning()).isFalse();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(manager).getExitCode(0, 0)).isEqualTo(0);
    }
  }

  @Test
  public void stop_WhenCouldNotTerminateDueToRetryTimeout_ThrowsOfficeException() throws Exception {

    final VetoTerminateListener terminateListener = new VetoTerminateListener();

    final OfficeProcessManagerPoolEntry manager =
        new OfficeProcessManagerPoolEntry(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            new PureJavaProcessManager() {

              private boolean firstAttempt = true;

              @Override
              public void kill(@Nullable final Process process, final long pid) {
                if (firstAttempt) {
                  firstAttempt = false;
                  TestUtil.sleepQuietly(500L);
                } else {
                  super.kill(process, pid);
                }
              }
            },
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    try {
      manager.start();
      assertThat(manager.isRunning()).isTrue();
      assertThat(manager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      // Listen to termination and change the timeout
      Whitebox.setInternalState(
          Whitebox.getInternalState(manager, "officeProcessManager"), "processTimeout", 1L);
      getConnection(manager).getDesktop().addTerminateListener(terminateListener);

    } finally {

      try {
        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(manager::stop)
            .withCauseExactlyInstanceOf(RetryTimeoutException.class);
      } finally {

        // Ensure that after the test, the office instance is terminated.
        Whitebox.setInternalState(
            Whitebox.getInternalState(manager, "officeProcessManager"), "processTimeout", 30_000L);
        getConnection(manager).getDesktop().removeTerminateListener(terminateListener);

        manager.stop();
        assertThat(manager.isRunning()).isFalse();
        assertThat(manager)
            .extracting(
                "officeProcessManager.process.running", "officeProcessManager.connection.connected")
            .containsExactly(false, false);
        assertThat(getOfficeProcess(manager).getExitCode(0, 0)).isEqualTo(0);
      }
    }
  }

  @Test
  public void isRunning_WhenNotStarted_ReturnsFalse() {

    final OfficeProcessManagerPoolEntry manager =
        new OfficeProcessManagerPoolEntry(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    assertThat(manager.isRunning()).isFalse();
  }
}
