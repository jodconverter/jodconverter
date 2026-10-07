/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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
import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.office.OfficeWorkerState;
import org.jodconverter.core.office.OfficeWorkerStatus;
import org.jodconverter.core.test.util.TestUtil;

/** Contains tests for the {@link LocalOfficeManager} class, with real office processes. */
class LocalOfficeManagerITest {

  private static final int PORT_1 = 2021;
  private static final int PORT_2 = 2022;
  private static final long WAIT_TIMEOUT = 120_000L; // 2 minutes.

  private LocalOfficeManager manager;

  @AfterEach
  void stopManager() {
    OfficeUtils.stopQuietly(manager);
  }

  /** A task that remembers which office process executed it. */
  private static class RecordingTask extends MockOfficeTask {

    /* default */ volatile OfficeContext context;

    @Override
    public void execute(final OfficeContext context) throws OfficeException {
      this.context = context;
      super.execute(context);
    }
  }

  private static void await(final BooleanSupplier condition) {

    final var deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(WAIT_TIMEOUT);
    while (!condition.getAsBoolean()) {
      if (System.nanoTime() > deadline) {
        fail("The expected condition was not met in time");
      }
      TestUtil.sleepQuietly(50L);
    }
  }

  private LocalOfficeProcessManager processManager(final int index) {
    final var workers = (List<?>) ReflectionTestUtils.getField(manager, "workers");
    assertThat(workers).isNotNull();
    return (LocalOfficeProcessManager)
        ReflectionTestUtils.getField(workers.get(index), "officeProcessManager");
  }

  private VerboseProcess process(final int index) {
    return (VerboseProcess) ReflectionTestUtils.getField(processManager(index), "process");
  }

  /**
   * Tests that the tasks are not given to an office process that is restarting while another one is
   * ready (issue 451).
   */
  // The state of each worker of the manager, in the order of the workers.
  private static List<OfficeWorkerState> states(final LocalOfficeManager manager) {
    return manager.getStatus().workers().stream().map(OfficeWorkerStatus::state).toList();
  }

  @Test
  void whenAProcessIsRestarting_ShouldGiveTheTasksToTheProcessThatIsReady() throws OfficeException {

    // The delay makes a restart longer than the tasks executed in the meantime.
    manager =
        LocalOfficeManager.builder()
            .portNumbers(PORT_1, PORT_2)
            .afterStartProcessDelay(5_000L)
            .startFailFast(true)
            .build();
    manager.start();
    assertThat(states(manager)).containsExactly(OfficeWorkerState.READY, OfficeWorkerState.READY);

    // The first office process crashes.
    processManager(0).kill();
    await(() -> states(manager).get(0) == OfficeWorkerState.RESTARTING);

    for (int i = 0; i < 3; i++) {
      final var task = new RecordingTask();
      manager.execute(task);
      assertThat(task.isCompleted()).isTrue();
      assertThat(task.context).isSameAs(processManager(1).getConnection());
    }
    assertThat(states(manager).get(0)).isEqualTo(OfficeWorkerState.RESTARTING);

    // The first office process comes back, and executes tasks again.
    await(() -> states(manager).get(0) == OfficeWorkerState.READY);
    final var blocking = manager.submit(new MockOfficeTask(3_000L));
    await(() -> states(manager).contains(OfficeWorkerState.BUSY));
    final var task = new RecordingTask();
    manager.execute(task);
    assertThat(task.isCompleted()).isTrue();
    assertThat(blocking.join()).isNull();
  }

  /**
   * Tests that a task submitted while the office process is starting waits in the queue, instead of
   * failing with the task execution timeout and killing the process that is starting (issue 451).
   */
  @Test
  void whenTheProcessIsStarting_ShouldNotCountTheStartInTheTaskExecutionTimeout()
      throws OfficeException {

    // The start takes longer than the task execution timeout.
    manager =
        LocalOfficeManager.builder()
            .portNumbers(PORT_1)
            .afterStartProcessDelay(6_000L)
            .taskExecutionTimeout(5_000L)
            .taskQueueTimeout(WAIT_TIMEOUT)
            .startFailFast(false)
            .build();
    manager.start();
    assertThat(states(manager)).containsExactly(OfficeWorkerState.STARTING);

    final var task = new MockOfficeTask();
    manager.execute(task);

    assertThat(task.isCompleted()).isTrue();
    assertThat(states(manager)).containsExactly(OfficeWorkerState.READY);
  }

  /** Tests that an office process is restarted when the execution of a task times out. */
  @Test
  void whenATaskTimesOut_ShouldFailTheTaskAndRestartTheProcess() throws OfficeException {

    manager =
        LocalOfficeManager.builder()
            .portNumbers(PORT_1)
            .taskExecutionTimeout(3_000L)
            .taskQueueTimeout(WAIT_TIMEOUT)
            .startFailFast(true)
            .build();
    manager.start();
    final var process = process(0);

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(() -> manager.execute(new MockOfficeTask(20_000L)))
        .withMessageStartingWith("Task did not complete within timeout (3000 ms)");

    // The next task waits for the new office process.
    final var goodTask = new MockOfficeTask();
    manager.execute(goodTask);
    assertThat(goodTask.isCompleted()).isTrue();
    assertThat(process(0)).isNotSameAs(process);
    assertThat(process.getExitCode()).isNotNull();
  }

  /** Tests that an office process restarts after its maximum number of tasks. */
  @Test
  void whenMaxTasksPerProcessReached_ShouldRestartTheProcessBetweenTheTasks()
      throws OfficeException {

    manager =
        LocalOfficeManager.builder()
            .portNumbers(PORT_1)
            .maxTasksPerProcess(2)
            .taskQueueTimeout(WAIT_TIMEOUT)
            .startFailFast(true)
            .build();
    manager.start();
    final var process = process(0);

    for (int i = 0; i < 5; i++) {
      final var task = new MockOfficeTask();
      manager.execute(task);
      assertThat(task.isCompleted()).isTrue();
    }

    // The third and the fifth tasks were executed by new office processes.
    assertThat(process(0)).isNotSameAs(process);
    assertThat(process.getExitCode()).isEqualTo(0);
  }

  /**
   * Tests that a manager can be started right after another one was stopped, on the same port, and
   * that the office processes are asked to terminate, not killed.
   */
  @Test
  void whenStartedRightAfterAStopOnTheSamePort_ShouldStart() throws OfficeException {

    for (int i = 0; i < 4; i++) {
      manager = LocalOfficeManager.builder().portNumbers(PORT_1).startFailFast(true).build();
      manager.start();
      final var process = process(0);
      final var task = new MockOfficeTask();
      manager.execute(task);
      assertThat(task.isCompleted()).isTrue();

      manager.stop();

      assertThat(manager.isRunning()).isFalse();
      assertThat(process.getExitCode()).isEqualTo(0);
    }
  }
}
