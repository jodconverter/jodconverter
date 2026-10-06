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

package org.jodconverter.core.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

/**
 * Contains tests for the {@link OfficeWorkerRunner} class, for what the tests of {@link
 * AbstractOfficeWorkerPool} cannot reach in a deterministic way.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class OfficeWorkerRunnerTest {

  /* default */
  @TempDir File workingDir;

  private OfficeWorkerRunner newRunner(final FakeOfficeWorker worker) {
    final var pool = FakeOfficeWorkerPool.builder().workingDir(workingDir).workers(worker).build();
    return new OfficeWorkerRunner(pool, worker, true);
  }

  @Test
  void new_ShouldBeStoppedAndNotStartedYet() {

    final var runner = newRunner(new FakeOfficeWorker());

    assertThat(runner.getState()).isEqualTo(OfficeWorkerState.STOPPED);
    assertThat(runner.getFirstStart()).isNotDone();
  }

  @Test
  void start_ShouldBeStartingBeforeItsThreadRuns() {

    final var runner = newRunner(new FakeOfficeWorker());

    // A thread that never gets to drive the worker.
    runner.start(runnable -> new Thread(() -> {}));

    assertThat(runner.getState()).isEqualTo(OfficeWorkerState.STARTING);
  }

  @Test
  void abort_WithAJobThatIsNotBeingExecuted_ShouldNotAbortTheWorker() {

    // The job that timed out may have ended, and another one started, in the meantime.
    final var worker = new FakeOfficeWorker();
    final var runner = newRunner(worker);

    runner.abort(new OfficeJob(context -> {}));

    assertThat(worker.calls).isEmpty();
  }

  @Test
  void requestStopAndJoin_WhenNeverStarted_ShouldNotFail() {

    final var worker = new FakeOfficeWorker();
    final var runner = newRunner(worker);

    assertThatCode(
            () -> {
              runner.requestStop();
              runner.join();
            })
        .doesNotThrowAnyException();
    // No thread drives the worker: there is nothing to abort.
    assertThat(worker.calls).isEmpty();
  }

  @Test
  void run_WhenStoppedJustAfterTakingAJob_ShouldLeaveTheJobInTheQueue() throws Exception {

    final var worker = new FakeOfficeWorker();
    final var pool = FakeOfficeWorkerPool.builder().workingDir(workingDir).workers(worker).build();
    // The worker waits for a job without checking that it is still ready.
    pool.setIdleCheckInterval(60_000L);
    final var runner = new OfficeWorkerRunner(pool, worker, true);
    runner.start(new NamedThreadFactory("test-runner"));
    runner.getFirstStart().get(10, TimeUnit.SECONDS);
    while (runner.getState() != OfficeWorkerState.READY) {
      Thread.sleep(5);
    }

    // The stop is requested at the moment the worker, still ready, has taken the job.
    worker.onIsReady =
        () -> {
          runner.requestStop();
          worker.setReady(true);
        };
    final var job = new OfficeJob(context -> {});
    pool.requeueJob(job);
    runner.join();

    // The job was not executed: it is back in the queue, for the stop of the pool to fail it.
    assertThat(worker.executedTasks).hasValue(0);
    assertThat(pool.getQueueSize()).isEqualTo(1);
    assertThat(job.getFuture()).isNotDone();
    assertThat(runner.getState()).isEqualTo(OfficeWorkerState.STOPPED);
    // The worker was idle when the stop was requested: it is stopped without being aborted.
    assertThat(worker.calls).containsExactly("start", "stop");
  }

  @Test
  void run_WhenStoppedBeforeTheWorkerIsReady_ShouldFailTheFirstStart() throws Exception {

    final var worker = new FakeOfficeWorker();
    final var runner = newRunner(worker);
    runner.requestStop();

    runner.run();

    assertThat(runner.getState()).isEqualTo(OfficeWorkerState.STOPPED);
    assertThat(runner.getFirstStart()).isCompletedExceptionally();
    try {
      runner.getFirstStart().get();
    } catch (ExecutionException ex) {
      assertThat(ex.getCause())
          .isInstanceOf(OfficeException.class)
          .hasMessage("The office worker was stopped before it was ready");
    }
    // The worker was never started, and is stopped anyway to release what it may hold.
    assertThat(worker.calls).containsExactly("stop");
  }
}
