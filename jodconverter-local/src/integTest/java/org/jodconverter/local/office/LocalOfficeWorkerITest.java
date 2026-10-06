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
import static org.jodconverter.local.office.LocalOfficeManager.*;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.test.util.TestUtil;

/** Contains tests for the {@link LocalOfficeWorker} class, with a real office process. */
class LocalOfficeWorkerITest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalOfficeWorkerITest.class);

  private static final OfficeUrl CONNECT_URL = new OfficeUrl(2002);
  private static final long LONG_TASK_DELAY = 20_000L; // 20 Seconds.

  private LocalOfficeProcessManager processManager;
  private LocalOfficeWorker worker;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private LocalOfficeWorker newWorker(final int maxTasksPerProcess) {

    processManager =
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
            DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
            new OfficeConnection(CONNECT_URL));
    worker = new LocalOfficeWorker(maxTasksPerProcess, processManager);
    return worker;
  }

  @AfterEach
  void stopWorker() {
    executor.shutdownNow();
    if (worker != null) {
      // Clear the interrupted status a test may have left: the stop must not be disturbed.
      final var interrupted = Thread.interrupted();
      LOGGER.debug("Stopping the worker (interrupted: {})", interrupted);
      worker.stop();
    }
  }

  private VerboseProcess process() {
    return (VerboseProcess) ReflectionTestUtils.getField(processManager, "process");
  }

  private long pid() {
    final var pid = (Long) ReflectionTestUtils.getField(processManager, "pid");
    assertThat(pid).isNotNull();
    return pid;
  }

  // Executes a task that lasts long enough to be still running when something happens to it.
  private Future<Void> executeLongTask() {

    final Future<Void> future =
        executor.submit(
            () -> {
              worker.execute(new MockOfficeTask(LONG_TASK_DELAY));
              return null;
            });
    // Let the task open its document.
    TestUtil.sleepQuietly(1_000L);
    return future;
  }

  private static void assertFailsBeforeItsEnd(final Future<Void> future) {

    final var start = System.nanoTime();
    assertThatExceptionOfType(ExecutionException.class)
        .isThrownBy(() -> future.get(LONG_TASK_DELAY, TimeUnit.MILLISECONDS))
        .withCauseInstanceOf(OfficeException.class);
    assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start))
        .isLessThan(LONG_TASK_DELAY / 2);
  }

  @Test
  void whenEverythingWorksFine_ShouldSucceedAndStopGracefully() throws OfficeException {

    newWorker(0).start();
    assertThat(worker.isReady()).isTrue();
    final var process = process();
    final var instanceProfileDir =
        (File) ReflectionTestUtils.getField(processManager, "instanceProfileDir");

    final var task = new MockOfficeTask();
    worker.execute(task);
    assertThat(task.isCompleted()).isTrue();
    assertThat(worker.isReady()).isTrue();

    worker.stop();

    // The office process was asked to terminate, not killed, and nothing is left on disk.
    assertThat(worker.isReady()).isFalse();
    assertThat(process.getExitCode()).isEqualTo(0);
    assertThat(instanceProfileDir).doesNotExist();
  }

  /** Tests that an office process is restarted successfully after a crash. */
  @Test
  void whenOfficeProcessCrash_ShouldFailTheTaskAndRestartAfterCrash() throws OfficeException {

    newWorker(0).start();
    final var pid = pid();
    final var future = executeLongTask();

    // Simulate crash
    LOGGER.debug("Simulating the crash");
    processManager.kill();

    // The task does not wait for its end: it is interrupted when the connection is lost.
    assertFailsBeforeItsEnd(future);
    assertThat(worker.isReady()).isFalse();

    worker.restart();

    assertThat(worker.isReady()).isTrue();
    assertThat(pid()).isNotEqualTo(pid);
    final var goodTask = new MockOfficeTask();
    worker.execute(goodTask);
    assertThat(goodTask.isCompleted()).isTrue();
  }

  /** Tests that a task that must not go on (timeout, cancellation) can be ended. */
  @Test
  void whenAborted_ShouldFailTheTaskAndRestart() throws OfficeException {

    newWorker(0).start();
    final var pid = pid();
    final var future = executeLongTask();

    worker.abort();

    assertFailsBeforeItsEnd(future);

    worker.restart();

    assertThat(worker.isReady()).isTrue();
    assertThat(pid()).isNotEqualTo(pid);
    final var goodTask = new MockOfficeTask();
    worker.execute(goodTask);
    assertThat(goodTask.isCompleted()).isTrue();
  }

  /**
   * Tests that an office process is restarted when it reached the maximum number of executed tasks.
   */
  @Test
  void whenMaxTasksPerProcessReached_ShouldRestartWithTheSameProfile() throws OfficeException {

    newWorker(3).start();
    final var pid = pid();
    final var process = process();
    final var instanceProfileDir =
        (File) ReflectionTestUtils.getField(processManager, "instanceProfileDir");
    assertThat(instanceProfileDir).isNotNull();
    final var marker = new File(instanceProfileDir, "jodconverter-test-marker");

    for (int i = 0; i < 3; i++) {
      assertThat(worker.isReady()).isTrue();
      final var task = new MockOfficeTask();
      worker.execute(task);
      assertThat(task.isCompleted()).isTrue();
    }
    assertThat(worker.isReady()).isFalse();
    assertThat(marker.mkdir()).isTrue();

    worker.restart();

    // Another office process, asked to terminate and not killed, with the same profile.
    assertThat(worker.isReady()).isTrue();
    assertThat(pid()).isNotEqualTo(pid);
    assertThat(process.getExitCode()).isEqualTo(0);
    assertThat(marker).isDirectory();
    for (int i = 0; i < 3; i++) {
      assertThat(worker.isReady()).isTrue();
      final var task = new MockOfficeTask();
      worker.execute(task);
      assertThat(task.isCompleted()).isTrue();
    }
    assertThat(worker.isReady()).isFalse();
  }
}
