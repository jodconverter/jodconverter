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

package org.jodconverter.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
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

import com.sun.star.frame.TerminationVetoException;
import com.sun.star.frame.XTerminateListener;
import com.sun.star.lang.EventObject;

import org.jodconverter.process.PureJavaProcessManager;

public class OfficeProcessManagerPoolEntryITest {

  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(OfficeProcessManagerPoolEntryITest.class);

  private static final OfficeUrl CONNECT_URL = new OfficeUrl(2002);
  private static final long RESTART_INITIAL_WAIT = 5000; // 5 Seconds.
  private static final long RESTART_WAIT_TIMEOUT = 10000; // 10 Seconds.

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

  private static OfficeProcess startOfficeProcess(final OfficeUrl officeUrl) throws Exception {

    // Starts an office process
    final OfficeProcess officeProcess = new OfficeProcess(officeUrl);
    officeProcess.start();
    Thread.sleep(1000); // NOSONAR
    final Integer exitCode = officeProcess.getExitCode();
    if (exitCode != null && exitCode.equals(Integer.valueOf(81))) {
      officeProcess.start(true);
      Thread.sleep(1000); // NOSONAR
    }
    return officeProcess;
  }

  private static void assertRestartedAndReconnected(
      final OfficeProcessManagerPoolEntry officeManager, final long initialWait, final long timeout)
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

  private static OfficeProcess getOfficeProcess(final OfficeProcessManagerPoolEntry manager)
      throws IllegalAccessException {

    final OfficeProcessManager processManager =
        (OfficeProcessManager) FieldUtils.readField(manager, "officeProcessManager", true);
    return (OfficeProcess) FieldUtils.readField(processManager, "process", true);
  }

  private static OfficeConnection getConnection(final OfficeProcessManagerPoolEntry manager)
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

    final OfficeProcessManagerPoolEntry officeManager =
        new OfficeProcessManagerPoolEntry(CONNECT_URL);

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      final MockOfficeTask task = new MockOfficeTask();
      officeManager.execute(task);
      assertThat(task.isCompleted()).isTrue();

    } finally {

      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(officeManager).getExitCode(0, 0)).isEqualTo(0);
    }
  }

  private class RestartAfterCrashTask implements Callable<Boolean> {

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

  /**
   * Tests that an office process is restarted successfully after a crash.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void execute_WhenOfficeProcessCrash_ShouldRestartAfterCrash() throws Exception {

    final OfficeProcessManagerPoolEntry officeManager =
        new OfficeProcessManagerPoolEntry(CONNECT_URL);

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      // Submit the task to an executor
      final ExecutorService pool = Executors.newFixedThreadPool(1);
      try {
        final Callable<Boolean> task = new RestartAfterCrashTask(officeManager);
        final Future<Boolean> future = pool.submit(task);

        Thread.sleep(500); // NOSONAR

        // Simulate crash
        final VerboseProcess verboseProcess =
            (VerboseProcess) FieldUtils.readField(getOfficeProcess(officeManager), "process", true);
        final Process underlyingProcess = verboseProcess.getProcess();
        assertThat(underlyingProcess).isNotNull();
        LOGGER.debug("Simulating the crash");
        underlyingProcess.destroy();

        // Wait until the task is completed
        try {
          future.get();
          fail("Exception expected");
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
      assertThat(officeManager.isRunning()).isFalse();
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
  public void execute_WhenTimeoutExceptionOccured_ShouldRestartAfterTaskTimeout() throws Exception {

    final OfficeProcessManagerPoolEntryConfig config = new OfficeProcessManagerPoolEntryConfig();
    config.setTaskExecutionTimeout(1500L);
    final OfficeProcessManagerPoolEntry officeManager =
        new OfficeProcessManagerPoolEntry(CONNECT_URL, config);

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      final MockOfficeTask task = new MockOfficeTask(2000);
      try {
        officeManager.execute(task);
        fail("Exception expected");
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
      assertThat(officeManager.isRunning()).isFalse();
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
  public void execute_WhenMaxTasksPerProcessReached_ShouldRestart() throws Exception {

    final OfficeProcessManagerPoolEntryConfig config = new OfficeProcessManagerPoolEntryConfig();
    config.setMaxTasksPerProcess(3);
    final OfficeProcessManagerPoolEntry officeManager =
        new OfficeProcessManagerPoolEntry(CONNECT_URL, config);

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      for (int i = 0; i < 3; i++) {
        final MockOfficeTask task = new MockOfficeTask();
        officeManager.execute(task);
        assertThat(task.isCompleted()).isTrue();
        assertThat(officeManager).extracting("taskCount.value").isEqualTo(i + 1);
      }

      final MockOfficeTask task = new MockOfficeTask();
      officeManager.execute(task);
      assertThat(task.isCompleted()).isTrue();
      assertThat(officeManager).extracting("taskCount.value").isEqualTo(1);

    } finally {

      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(officeManager).getExitCode(0, 0)).isEqualTo(0);
    }
  }

  @Test
  public void start_WhenProcessAlreadyExistsAndKillExistingProcessOn_ShouldKillExistingProcess()
      throws Exception {

    // Starts an office process
    final OfficeProcess officeProcess = startOfficeProcess(CONNECT_URL);

    final OfficeProcessManagerPoolEntry officeManager =
        new OfficeProcessManagerPoolEntry(CONNECT_URL);
    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

    } finally {

      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(officeManager).getExitCode(0, 0)).isEqualTo(0);

      officeProcess.forciblyTerminate(1000L, 5000L);
      officeProcess.deleteInstanceProfileDir();
    }
  }

  @Test
  public void start_WhenProcessAlreadyExistsAndKillExistingProcessOff_ShouldKillExistingProcess()
      throws Exception {

    // Starts an office process
    final OfficeProcess officeProcess = startOfficeProcess(CONNECT_URL);

    final OfficeProcessManagerPoolEntryConfig config = new OfficeProcessManagerPoolEntryConfig();
    config.setKillExistingProcess(false);
    final OfficeProcessManagerPoolEntry officeManager =
        new OfficeProcessManagerPoolEntry(CONNECT_URL, config);
    try {
      officeManager.start();
      fail("Exception expected");

    } catch (Exception ex) {

      assertThat(ex)
          .isExactlyInstanceOf(OfficeException.class)
          .hasMessageContaining("A process with acceptString")
          .hasMessageContaining("is already running");

    } finally {

      assertThat(officeManager.isRunning()).isFalse();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);

      officeProcess.forciblyTerminate(1000L, 5000L);
      officeProcess.deleteInstanceProfileDir();
    }
  }

  @Test
  public void start_WithCustomProfileDir_ShouldCopyProfileDirToWorkingDir() throws Exception {

    final OfficeProcessManagerPoolEntryConfig config = new OfficeProcessManagerPoolEntryConfig();
    config.setOfficeHome(LocalOfficeUtils.getDefaultOfficeHome());
    config.setWorkingDir(new File(System.getProperty("java.io.tmpdir")));
    config.setOfficeHome(LocalOfficeUtils.getDefaultOfficeHome());
    config.setTemplateProfileDir(new File("src/integTest/resources/templateProfileDir"));

    final OfficeProcessManagerPoolEntry officeManager =
        new OfficeProcessManagerPoolEntry(CONNECT_URL, config);
    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      // Check the profile dir existence
      final File instanceProfileDir =
          (File) FieldUtils.readField(getOfficeProcess(officeManager), "instanceProfileDir", true);
      assertThat(new File(instanceProfileDir, "user/customFile")).isFile();

    } finally {

      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(false, false);
      assertThat(getOfficeProcess(officeManager).getExitCode(0, 0)).isEqualTo(0);
    }
  }

  @Test
  public void stop_WhenCouldNotTerminateDueToRetryTimeout_ThrowsOfficeException() throws Exception {

    final OfficeProcessManagerPoolEntryConfig config = new OfficeProcessManagerPoolEntryConfig();
    config.setProcessManager(
        new PureJavaProcessManager() {

          private boolean firstAttempt = true;

          @Override
          public void kill(final Process process, final long pid) {
            if (firstAttempt) {
              firstAttempt = false;
              try {
                Thread.sleep(500); // NOSONAR
              } catch (InterruptedException e) {
                // Swallow
              }
            } else {
              super.kill(process, pid);
            }
          }
        });

    final VetoTerminateListener terminateListener = new VetoTerminateListener();

    final OfficeProcessManagerPoolEntry officeManager =
        new OfficeProcessManagerPoolEntry(CONNECT_URL, config);
    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();
      assertThat(officeManager)
          .extracting(
              "officeProcessManager.process.running", "officeProcessManager.connection.connected")
          .containsExactly(true, true);

      // Listen to termination and change the timeout
      config.setProcessTimeout(1L);
      getConnection(officeManager).getDesktop().addTerminateListener(terminateListener);

    } finally {

      try {
        officeManager.stop();
        fail("Exception expected");

      } catch (Exception ex) {

        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasCauseExactlyInstanceOf(RetryTimeoutException.class);

      } finally {

        // Ensure that after the test, the office instance is terminated.
        config.setProcessTimeout(30000L);
        getConnection(officeManager).getDesktop().removeTerminateListener(terminateListener);

        officeManager.stop();
        assertThat(officeManager.isRunning()).isFalse();
        assertThat(officeManager)
            .extracting(
                "officeProcessManager.process.running", "officeProcessManager.connection.connected")
            .containsExactly(false, false);
        assertThat(getOfficeProcess(officeManager).getExitCode(0, 0)).isEqualTo(0);
      }
    }
  }

  @Test
  public void isRunning_WhenNotStarted_ReturnsFalse() throws Exception {

    final OfficeProcessManagerPoolEntry officeManager =
        new OfficeProcessManagerPoolEntry(CONNECT_URL);

    assertThat(officeManager.isRunning()).isFalse();
  }
}
