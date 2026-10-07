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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.task.OfficeTask;

/** Contains tests for the {@link AbstractOfficeWorkerPool} class. */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class AbstractOfficeWorkerPoolTest {

  private static final OfficeTask NOOP = context -> {};

  /* default */
  @TempDir File workingDir;

  private FakeOfficeWorkerPool pool;

  @AfterEach
  void stopPool() throws OfficeException {
    // Never leave a thread interrupted for the next test.
    Thread.interrupted();
    if (pool != null) {
      pool.stop();
    }
  }

  private FakeOfficeWorkerPool.Builder builder(final FakeOfficeWorker... workers) {
    return FakeOfficeWorkerPool.builder().workingDir(workingDir).workers(workers);
  }

  private FakeOfficeWorkerPool started(final FakeOfficeWorker... workers) throws OfficeException {
    pool = builder(workers).build();
    pool.start();
    return pool;
  }

  private static void await(final BooleanSupplier condition) {
    final var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
    while (!condition.getAsBoolean()) {
      if (System.nanoTime() > deadline) {
        fail("The expected condition was not met in time");
      }
      try {
        Thread.sleep(5);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        fail("Interrupted while waiting for a condition");
      }
    }
  }

  private static OfficeException failureOf(final CompletableFuture<Void> future) {
    try {
      future.get(10, TimeUnit.SECONDS);
      return fail("The task was expected to fail");
    } catch (ExecutionException ex) {
      assertThat(ex.getCause()).isInstanceOf(OfficeException.class);
      return (OfficeException) ex.getCause();
    } catch (Exception ex) {
      return fail("Unexpected exception", ex);
    }
  }

  /** A task that blocks until it is released, or interrupted. */
  private static final class BlockingTask implements OfficeTask {

    private final CountDownLatch started = new CountDownLatch(1);
    private final CountDownLatch release = new CountDownLatch(1);
    private final AtomicBoolean interrupted = new AtomicBoolean();
    private final AtomicBoolean completed = new AtomicBoolean();

    @Override
    public void execute(final OfficeContext context) throws OfficeException {
      started.countDown();
      try {
        release.await();
        completed.set(true);
      } catch (InterruptedException ex) {
        interrupted.set(true);
        throw new OfficeException("The task was aborted", ex);
      }
    }

    private void awaitStarted() {
      try {
        assertThat(started.await(10, TimeUnit.SECONDS)).isTrue();
      } catch (InterruptedException ex) {
        fail("Interrupted while waiting for the task to start");
      }
    }
  }

  // The state of each worker of the pool, in the order of the workers.
  private static List<OfficeWorkerState> states(final AbstractOfficeWorkerPool pool) {
    return pool.getStatus().workers().stream().map(OfficeWorkerStatus::state).toList();
  }

  @Nested
  class Start {

    @Test
    void withFailFast_ShouldWaitForAllTheWorkersToBeReady() throws OfficeException {

      final var worker1 = new FakeOfficeWorker();
      final var worker2 = new FakeOfficeWorker();

      pool = builder(worker1, worker2).build();
      assertThat(states(pool)).isEmpty();
      assertThat(pool.isRunning()).isFalse();

      pool.start();

      assertThat(worker1.calls).containsExactly("start");
      assertThat(worker2.calls).containsExactly("start");
      assertThat(pool.isRunning()).isTrue();
      assertThat(pool.getTempDir()).isDirectory();
      await(() -> states(pool).equals(List.of(OfficeWorkerState.READY, OfficeWorkerState.READY)));
    }

    @Test
    void whenAlreadyStarted_ShouldThrowIllegalStateException() throws OfficeException {

      started(new FakeOfficeWorker());

      assertThatIllegalStateException()
          .isThrownBy(pool::start)
          .withMessage("This office manager is already running.");
    }

    @Test
    void whenStopped_ShouldThrowIllegalStateException() throws OfficeException {

      started(new FakeOfficeWorker());
      pool.stop();

      assertThatIllegalStateException()
          .isThrownBy(pool::start)
          .withMessage("This office manager has been shutdown.");
    }

    @Test
    void withoutWorker_ShouldThrowIllegalArgumentException() {

      pool = FakeOfficeWorkerPool.builder().workingDir(workingDir).build();

      assertThatIllegalArgumentException()
          .isThrownBy(pool::start)
          .withMessage("This office manager has no worker");
      assertThatIllegalArgumentException().isThrownBy(() -> builder().build());
    }

    @Test
    void withFailFastAndWorkerThatCannotStart_ShouldThrowAndShutdown() {

      final var good = new FakeOfficeWorker();
      final var bad = new FakeOfficeWorker();
      bad.failingStarts.set(1);
      pool = builder(good, bad).build();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(pool::start)
          .withMessage("The start failed");

      // The worker that started is stopped, and the pool cannot be used anymore.
      assertThat(good.calls).contains("start", "stop");
      assertThat(bad.calls).contains("start", "stop").doesNotContain("restart");
      assertThat(pool.isRunning()).isFalse();
      assertThat(pool.getTempDir()).doesNotExist();
      assertThat(states(pool)).containsOnly(OfficeWorkerState.STOPPED);
      assertThatIllegalStateException().isThrownBy(pool::start);
      assertThatIllegalStateException().isThrownBy(() -> pool.submit(NOOP));
    }

    @Test
    void withFailFastAndUnexpectedStartFailure_ShouldThrowOfficeException() {

      final var worker = new FakeOfficeWorker();
      worker.failingStarts.set(1);
      worker.failWithRuntimeException = true;
      pool = builder(worker).build();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(pool::start)
          .withMessage("Could not start the office manager")
          .withCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void withFailFastWhenInterrupted_ShouldThrowAndShutdown() throws Exception {

      final var worker = new FakeOfficeWorker();
      worker.startGate = new CountDownLatch(1);
      pool = builder(worker).build();

      final var thrown = new AtomicReference<Throwable>();
      final var starter =
          new Thread(
              () -> {
                try {
                  pool.start();
                } catch (OfficeException | RuntimeException ex) {
                  thrown.set(ex);
                }
              });
      starter.start();
      assertThat(worker.startBegun.await(10, TimeUnit.SECONDS)).isTrue();
      starter.interrupt();
      starter.join(10_000);

      assertThat(thrown.get())
          .isInstanceOf(OfficeException.class)
          .hasMessage("Interruption while starting the office manager");
      // The start of the worker was aborted, and the worker stopped.
      await(() -> worker.calls.equals(List.of("start", "abort", "stop")));
      assertThat(pool.isRunning()).isFalse();
    }

    @Test
    void withFailFastWhenStoppedMeanwhile_ShouldAbortTheStartsAndThrow() throws Exception {

      // The start of the worker does not end until it is aborted.
      final var worker = new FakeOfficeWorker();
      worker.startGate = new CountDownLatch(1);
      pool = builder(worker).build();

      final var thrown = new AtomicReference<Throwable>();
      final var starter =
          new Thread(
              () -> {
                try {
                  pool.start();
                } catch (OfficeException | RuntimeException ex) {
                  thrown.set(ex);
                }
              });
      starter.start();
      assertThat(worker.startBegun.await(10, TimeUnit.SECONDS)).isTrue();

      // The stop must not wait for the start to end: it aborts it.
      final var stopper = new Thread(() -> assertThatCode(pool::stop).doesNotThrowAnyException());
      stopper.start();
      stopper.join(10_000);
      assertThat(stopper.isAlive()).as("stop() must not wait for the start").isFalse();
      starter.join(10_000);

      assertThat(thrown.get())
          .isInstanceOf(OfficeException.class)
          .hasMessage("The office worker was stopped before it was ready");
      assertThat(worker.calls).containsExactly("start", "abort", "stop");
      assertThat(pool.isRunning()).isFalse();
      assertThat(pool.getTempDir()).doesNotExist();
      assertThatIllegalStateException()
          .isThrownBy(pool::start)
          .withMessage("This office manager has been shutdown.");
    }

    @Test
    void withoutFailFast_ShouldReturnAtOnceAndExecuteTheTasksWhenReady() throws Exception {

      // The start of the worker takes longer than the execution timeout: the task must wait in
      // the queue, and not fail with an execution timeout.
      final var worker = new FakeOfficeWorker();
      worker.startGate = new CountDownLatch(1);
      pool = builder(worker).startFailFast(false).taskExecutionTimeout(50L).build();

      pool.start();
      assertThat(worker.startBegun.await(10, TimeUnit.SECONDS)).isTrue();
      assertThat(states(pool)).containsExactly(OfficeWorkerState.STARTING);
      assertThat(pool.isRunning()).isFalse();

      final var future = pool.submit(NOOP);
      Thread.sleep(150);
      assertThat(future).isNotDone();
      assertThat(pool.getStatus().queueSize()).isEqualTo(1);

      worker.startGate.countDown();

      future.get(10, TimeUnit.SECONDS);
      assertThat(worker.executedTasks).hasValue(1);
      assertThat(worker.count("abort")).isZero();
      assertThat(pool.isRunning()).isTrue();
    }

    @Test
    void withoutFailFastAndFailingStarts_ShouldRetryUntilReady() throws Exception {

      final var worker = new FakeOfficeWorker();
      worker.failingStarts.set(3);
      worker.failWithRuntimeException = true;
      pool = builder(worker).startFailFast(false).build();

      pool.start();
      pool.execute(NOOP);

      // The first attempt is a start, the following ones are restarts.
      assertThat(worker.calls).containsExactly("start", "restart", "restart", "restart", "execute");
      // The worker is ready again once its thread is back at the queue.
      await(() -> states(pool).equals(List.of(OfficeWorkerState.READY)));
    }

    @Test
    void whenTempDirExists_ShouldReplaceIt() throws Exception {

      pool = builder(new FakeOfficeWorker()).build();
      final var leftover = new File(pool.getTempDir(), "leftover.txt");
      assertThat(pool.getTempDir().mkdirs()).isTrue();
      Files.writeString(leftover.toPath(), "old");

      pool.start();

      assertThat(pool.getTempDir()).isDirectory();
      assertThat(leftover).doesNotExist();
    }

    @Test
    void whenTempDirCannotBeCreated_ShouldThrowOfficeException() throws IOException {

      final var notADirectory = new File(workingDir, "file.txt");
      Files.writeString(notADirectory.toPath(), "content");
      final var worker = new FakeOfficeWorker();
      pool = FakeOfficeWorkerPool.builder().workingDir(notADirectory).workers(worker).build();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(pool::start)
          .withMessageStartingWith("Cannot create temporary directory");
      assertThat(worker.calls).isEmpty();
    }
  }

  @Nested
  class Execute {

    @Test
    void shouldExecuteTheTaskOnAWorker() throws OfficeException {

      final var worker = new FakeOfficeWorker();
      started(worker);
      final var done = new AtomicBoolean();

      pool.execute(context -> done.set(true));

      assertThat(done).isTrue();
      assertThat(worker.executedTasks).hasValue(1);
    }

    @Test
    void whenTaskFails_ShouldThrowItsOfficeException() throws OfficeException {

      started(new FakeOfficeWorker());
      final var failure = new OfficeException("The task failed");

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(
              () ->
                  pool.execute(
                      context -> {
                        throw failure;
                      }))
          .isSameAs(failure);

      // The worker goes on with the next task.
      pool.execute(NOOP);
    }

    @Test
    void whenTaskFailsUnexpectedly_ShouldThrowOfficeExceptionWithTheCause() throws OfficeException {

      started(new FakeOfficeWorker());

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(
              () ->
                  pool.execute(
                      context -> {
                        throw new IllegalStateException("Unexpected");
                      }))
          .withMessageStartingWith("Task did not complete: ")
          .withCauseInstanceOf(IllegalStateException.class);

      pool.execute(NOOP);
    }

    @Test
    void whenNotRunning_ShouldThrowIllegalStateException() throws OfficeException {

      pool = builder(new FakeOfficeWorker()).build();
      assertThatIllegalStateException()
          .isThrownBy(() -> pool.execute(NOOP))
          .withMessage("This office manager is not running.");

      pool.start();
      pool.stop();
      assertThatIllegalStateException()
          .isThrownBy(() -> pool.execute(NOOP))
          .withMessage("This office manager is not running.");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNullTask_ShouldThrowNullPointerException() throws OfficeException {

      started(new FakeOfficeWorker());
      assertThatNullPointerException().isThrownBy(() -> pool.submit(null));
    }

    @Test
    void whenCallerIsInterrupted_ShouldAbortTheTask() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);
      final var task = new BlockingTask();

      final var thrown = new AtomicReference<Throwable>();
      final var interruptedAfter = new AtomicBoolean();
      final var caller =
          new Thread(
              () -> {
                try {
                  pool.execute(task);
                } catch (OfficeException ex) {
                  thrown.set(ex);
                }
                interruptedAfter.set(Thread.currentThread().isInterrupted());
              });
      caller.start();
      task.awaitStarted();
      caller.interrupt();
      caller.join(10_000);

      assertThat(thrown.get())
          .isInstanceOf(OfficeException.class)
          .hasMessageStartingWith("Task was interrupted while executing: ");
      assertThat(interruptedAfter).isTrue();
      // The task does not keep running for nobody: its worker is aborted, then restarted.
      await(task.interrupted::get);
      await(() -> worker.count("restart") == 1);
      pool.execute(NOOP);
    }
  }

  @Nested
  class Submit {

    @Test
    void shouldReturnAFutureCompletedWhenTheTaskIsDone() throws Exception {

      started(new FakeOfficeWorker());
      final var task = new BlockingTask();

      final var future = pool.submit(task);
      task.awaitStarted();
      assertThat(future).isNotDone();
      assertThat(states(pool)).containsExactly(OfficeWorkerState.BUSY);
      assertThat(pool.isRunning()).isTrue();

      task.release.countDown();

      future.get(10, TimeUnit.SECONDS);
      assertThat(task.completed).isTrue();
    }

    @Test
    void whenQueueIsFull_ShouldFailAtOnce() throws Exception {

      final var worker = new FakeOfficeWorker();
      pool = builder(worker).taskQueueCapacity(1).build();
      pool.start();
      final var running = new BlockingTask();
      final var first = pool.submit(running);
      running.awaitStarted();

      final var waiting = pool.submit(NOOP);
      final var rejected = pool.submit(NOOP);

      assertThat(rejected).isCompletedExceptionally();
      assertThat(failureOf(rejected))
          .hasMessageStartingWith("The task queue is full (1 tasks waiting)");
      assertThat(pool.getStatus().queueSize()).isEqualTo(1);

      running.release.countDown();
      first.get(10, TimeUnit.SECONDS);
      waiting.get(10, TimeUnit.SECONDS);
      assertThat(worker.executedTasks).hasValue(2);
    }

    @Test
    void whenCancelledWhileWaiting_ShouldNeverExecuteTheTask() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);
      final var running = new BlockingTask();
      final var first = pool.submit(running);
      running.awaitStarted();
      final var executed = new AtomicBoolean();
      final var waiting = pool.submit(context -> executed.set(true));
      assertThat(pool.getStatus().queueSize()).isEqualTo(1);

      assertThat(waiting.cancel(true)).isTrue();

      assertThat(pool.getStatus().queueSize()).isZero();
      running.release.countDown();
      first.get(10, TimeUnit.SECONDS);
      pool.execute(NOOP);
      assertThat(executed).isFalse();
      assertThat(worker.count("abort")).isZero();
    }

    @Test
    void whenCancelledWhileRunning_ShouldAbortAndRestartTheWorker() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);
      final var task = new BlockingTask();
      final var future = pool.submit(task);
      task.awaitStarted();

      assertThat(future.cancel(true)).isTrue();

      await(task.interrupted::get);
      await(() -> worker.count("restart") == 1);
      assertThat(worker.count("abort")).isEqualTo(1);
      pool.execute(NOOP);
      assertThat(worker.executedTasks).hasValue(1);
    }

    @Test
    void whenCancelledAfterCompletion_ShouldDoNothing() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);
      final var future = pool.submit(NOOP);
      future.get(10, TimeUnit.SECONDS);

      assertThat(future.cancel(true)).isFalse();
      assertThat(worker.count("abort")).isZero();
    }
  }

  @Nested
  class Dispatch {

    @Test
    void whenAWorkerIsRestarting_ShouldGiveTheTasksToTheReadyWorkers() throws Exception {

      final var restarting = new FakeOfficeWorker();
      final var ready = new FakeOfficeWorker();
      started(restarting, ready);

      // The first worker loses its office process, and its restart takes a long time.
      restarting.startGate = new CountDownLatch(1);
      restarting.setReady(false);
      await(() -> states(pool).get(0) == OfficeWorkerState.RESTARTING);

      for (var i = 0; i < 5; i++) {
        pool.execute(NOOP);
      }

      assertThat(ready.executedTasks).hasValue(5);
      assertThat(restarting.executedTasks).hasValue(0);
      assertThat(pool.isRunning()).isTrue();

      restarting.startGate.countDown();
      await(() -> states(pool).get(0) == OfficeWorkerState.READY);
    }

    @Test
    void whenAWorkerIsNoLongerReadyWhenItTakesATask_ShouldRestartFirst() throws Exception {

      final var worker = new FakeOfficeWorker();
      pool = builder(worker).build();
      // The worker waits for a task without checking that it is still ready.
      pool.setIdleCheckInterval(60_000L);
      pool.start();
      await(() -> states(pool).get(0) == OfficeWorkerState.READY);

      // The office process is lost at the moment the worker takes the task.
      final var lost = new AtomicBoolean();
      worker.onIsReady =
          () -> {
            if (lost.compareAndSet(false, true)) {
              worker.setReady(false);
            }
          };

      pool.execute(NOOP);

      assertThat(worker.calls).containsExactly("start", "restart", "execute");
    }

    @Test
    void whenAnIdleWorkerIsNoLongerReady_ShouldRestartIt() throws OfficeException {

      final var worker = new FakeOfficeWorker();
      started(worker);

      worker.setReady(false);

      await(() -> worker.count("restart") == 1);
      await(() -> states(pool).get(0) == OfficeWorkerState.READY);
    }

    @Test
    void whenAWorkerReachesItsTaskLimit_ShouldRestartItBetweenTheTasks() throws OfficeException {

      final var worker = new FakeOfficeWorker();
      worker.maxTasks = 2;
      started(worker);

      for (var i = 0; i < 5; i++) {
        pool.execute(NOOP);
      }

      assertThat(worker.calls)
          .containsExactly(
              "start", "execute", "execute", "restart", "execute", "execute", "restart", "execute");
    }

    @Test
    void whenARestartFails_ShouldRetryAndKeepTheTasksWaiting() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);

      worker.failingStarts.set(2);
      worker.setReady(false);

      pool.execute(NOOP);

      assertThat(worker.count("restart")).isEqualTo(3);
      assertThat(worker.executedTasks).hasValue(1);
    }

    @Test
    void whenATaskWasEndedWhileOutOfTheQueue_ShouldSkipIt() throws OfficeException {

      // A worker can put back a task that timed out or was cancelled in the meantime.
      final var worker = new FakeOfficeWorker();
      started(worker);
      final var ended = new OfficeJob(NOOP);
      assertThat(ended.tryEndWaiting()).isTrue();

      pool.requeueJob(ended);

      await(() -> pool.getStatus().queueSize() == 0);
      pool.execute(NOOP);
      assertThat(worker.executedTasks).hasValue(1);
      assertThat(ended.getFuture()).isNotDone();
    }
  }

  @Nested
  class Timeouts {

    @Test
    void whenNoWorkerTakesTheTaskInTime_ShouldFailWithoutExecutingIt() throws Exception {

      final var worker = new FakeOfficeWorker();
      pool = builder(worker).taskQueueTimeout(100L).build();
      pool.start();
      final var running = new BlockingTask();
      final var first = pool.submit(running);
      running.awaitStarted();
      final var executed = new AtomicBoolean();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> pool.execute(context -> executed.set(true)))
          .withMessage("No office manager available after 100 millisec");

      assertThat(pool.getStatus().queueSize()).isZero();
      running.release.countDown();
      first.get(10, TimeUnit.SECONDS);
      pool.execute(NOOP);
      assertThat(executed).isFalse();
      // The task that was running is not disturbed.
      assertThat(running.completed).isTrue();
      assertThat(worker.count("abort")).isZero();
    }

    @Test
    void whenTheWorkerIsNotReadyInTime_ShouldFailWithTheQueueTimeout() throws Exception {

      // Waiting for a worker to start is queue time: the task fails with the queue timeout, and
      // the worker that is starting is left alone.
      final var worker = new FakeOfficeWorker();
      worker.startGate = new CountDownLatch(1);
      pool = builder(worker).startFailFast(false).taskQueueTimeout(100L).build();
      pool.start();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> pool.execute(NOOP))
          .withMessage("No office manager available after 100 millisec");

      assertThat(worker.calls).containsExactly("start");
      worker.startGate.countDown();
      pool.execute(NOOP);
    }

    @Test
    void whenTheExecutionTakesTooLong_ShouldFailAbortAndRestartTheWorker() throws Exception {

      final var worker = new FakeOfficeWorker();
      pool = builder(worker).taskExecutionTimeout(100L).build();
      pool.start();
      final var task = new BlockingTask();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> pool.execute(task))
          .withMessageStartingWith("Task did not complete within timeout (100 ms): ")
          .withCauseExactlyInstanceOf(TimeoutException.class);

      await(task.interrupted::get);
      await(() -> worker.count("restart") == 1);
      assertThat(worker.count("abort")).isEqualTo(1);

      // The worker is usable again.
      pool.execute(NOOP);
      assertThat(worker.executedTasks).hasValue(1);
    }

    @Test
    void whenATimedOutTaskEndsLater_ShouldDiscardItsResultAndRestartTheWorker() throws Exception {

      final var worker = new FakeOfficeWorker();
      pool = builder(worker).taskExecutionTimeout(100L).build();
      pool.start();

      // A task that ignores the interruption, and ends normally after its timeout.
      final var release = new AtomicBoolean();
      final var ended = new AtomicBoolean();
      final var future =
          pool.submit(
              context -> {
                while (!release.get()) {
                  try {
                    Thread.sleep(5);
                  } catch (InterruptedException ignored) {
                    // Ignored on purpose.
                  }
                }
                ended.set(true);
              });

      assertThat(failureOf(future))
          .hasMessageStartingWith("Task did not complete within timeout (100 ms): ");
      assertThat(states(pool)).containsExactly(OfficeWorkerState.BUSY);
      release.set(true);
      await(ended::get);

      // The future keeps its failure, and the worker is restarted although its task ended well.
      await(() -> worker.count("restart") == 1);
      assertThat(future).isCompletedExceptionally();
      pool.execute(NOOP);
    }

    @Test
    void whenTheJobIsAlreadyDone_ShouldDoNothing() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);
      final var job = new OfficeJob(NOOP);
      pool.enqueue(job);
      job.getFuture().get(10, TimeUnit.SECONDS);

      // The timeouts and a cancellation that come too late have no effect.
      pool.onQueueTimeout(job);
      pool.onExecutionTimeout(job);
      pool.cancel(job);

      assertThat(job.getFuture()).isCompleted();
      assertThat(job.getFuture()).isNotCompletedExceptionally();
      assertThat(worker.count("abort")).isZero();
    }
  }

  @Nested
  class Status {

    @Test
    void whenNeverStarted_ShouldHaveNoWorker() {

      pool = builder(new FakeOfficeWorker()).build();

      final var status = pool.getStatus();

      assertThat(status.workers()).isEmpty();
      assertThat(status.queueSize()).isZero();
      assertThat(status.isRunning()).isFalse();
    }

    @Test
    void whenStarted_ShouldReportReadyWorkersWithoutAnyTask() throws OfficeException {

      started(new FakeOfficeWorker(), new FakeOfficeWorker());

      final var status = pool.getStatus();

      assertThat(status.workers())
          .containsExactly(
              new OfficeWorkerStatus(OfficeWorkerState.READY, 0, 0, 0),
              new OfficeWorkerStatus(OfficeWorkerState.READY, 0, 0, 0));
      assertThat(status.count(OfficeWorkerState.READY)).isEqualTo(2);
      assertThat(status.count(OfficeWorkerState.BUSY)).isZero();
      assertThat(status.isRunning()).isTrue();
    }

    @Test
    void shouldCountTheTasksSinceTheLastStartAndTheRestarts() throws OfficeException {

      final var worker = new FakeOfficeWorker();
      worker.maxTasks = 2;
      started(worker);

      pool.execute(NOOP);
      // The worker is ready again once its thread is back at the queue.
      await(
          () ->
              pool.getStatus()
                  .workers()
                  .equals(List.of(new OfficeWorkerStatus(OfficeWorkerState.READY, 1, 0, 0))));

      // The second task reaches the limit: the worker restarts before the third one.
      pool.execute(NOOP);
      pool.execute(NOOP);

      await(() -> states(pool).get(0) == OfficeWorkerState.READY);
      assertThat(pool.getStatus().workers())
          .containsExactly(new OfficeWorkerStatus(OfficeWorkerState.READY, 1, 1, 0));
    }

    @Test
    void whenAWorkerCannotBeMadeReady_ShouldCountTheFailedAttempts() throws Exception {

      final var worker = new FakeOfficeWorker();
      worker.failingStarts.set(2);
      pool = builder(worker).startFailFast(false).build();

      pool.start();

      // The attempts that fail are counted while the worker is not ready...
      await(() -> pool.getStatus().workers().get(0).startFailures() > 0);
      await(() -> states(pool).get(0) == OfficeWorkerState.READY);
      // ...and forgotten once it is ready. The attempts of the start are not restarts.
      assertThat(pool.getStatus().workers())
          .containsExactly(new OfficeWorkerStatus(OfficeWorkerState.READY, 0, 0, 0));
    }

    @Test
    void whileATaskWaits_ShouldReportTheBusyWorkerAndTheQueue() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);
      final var blocking = new BlockingTask();
      final var running = pool.submit(blocking);
      blocking.awaitStarted();
      final var waiting = pool.submit(NOOP);

      final var status = pool.getStatus();

      assertThat(status.workers().get(0).state()).isEqualTo(OfficeWorkerState.BUSY);
      assertThat(status.count(OfficeWorkerState.BUSY)).isEqualTo(1);
      assertThat(status.queueSize()).isEqualTo(1);
      assertThat(status.isRunning()).isTrue();

      blocking.release.countDown();
      running.get(10, TimeUnit.SECONDS);
      waiting.get(10, TimeUnit.SECONDS);
    }
  }

  @Nested
  class Stop {

    @Test
    void shouldFailTheTasksStopTheWorkersAndDeleteTheTempDir() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);
      final var tempDir = pool.getTempDir();
      final var task = new BlockingTask();
      final var running = pool.submit(task);
      task.awaitStarted();
      final var waiting = pool.submit(NOOP);

      pool.stop();

      assertThat(failureOf(running))
          .hasMessageStartingWith("Task was cancelled, the office manager is stopping: ");
      assertThat(failureOf(waiting))
          .hasMessageStartingWith("Task was not executed, the office manager is stopped: ");
      assertThat(task.interrupted).isTrue();
      assertThat(worker.calls).containsExactly("start", "execute", "abort", "stop");
      assertThat(states(pool)).containsExactly(OfficeWorkerState.STOPPED);
      assertThat(pool.isRunning()).isFalse();
      assertThat(pool.getStatus().queueSize()).isZero();
      assertThat(tempDir).doesNotExist();
    }

    @Test
    void whenNeverStarted_ShouldShutdownWithoutTouchingTheWorkers() throws OfficeException {

      final var worker = new FakeOfficeWorker();
      pool = builder(worker).build();

      pool.stop();

      assertThat(worker.calls).isEmpty();
      assertThatIllegalStateException()
          .isThrownBy(pool::start)
          .withMessage("This office manager has been shutdown.");
    }

    @Test
    void whenAlreadyStopped_ShouldDoNothing() throws OfficeException {

      final var worker = new FakeOfficeWorker();
      started(worker);

      pool.stop();
      pool.stop();

      assertThat(worker.count("stop")).isEqualTo(1);
    }

    @Test
    void whenTheWorkersAreIdle_ShouldStopThemWithoutAbortingThem() throws Exception {

      final var worker1 = new FakeOfficeWorker();
      final var worker2 = new FakeOfficeWorker();
      started(worker1, worker2);
      pool.execute(context -> {});

      pool.stop();

      // An idle worker has nothing to abort: it can stop properly.
      assertThat(worker1.count("abort")).isZero();
      assertThat(worker2.count("abort")).isZero();
      assertThat(worker1.count("stop")).isEqualTo(1);
      assertThat(worker2.count("stop")).isEqualTo(1);
    }

    @Test
    void whileAWorkerIsStarting_ShouldAbortItsStart() throws Exception {

      final var worker = new FakeOfficeWorker();
      worker.startGate = new CountDownLatch(1);
      pool = builder(worker).startFailFast(false).build();
      pool.start();
      assertThat(worker.startBegun.await(10, TimeUnit.SECONDS)).isTrue();

      pool.stop();

      assertThat(worker.calls).containsExactly("start", "abort", "stop");
    }

    @Test
    void whileAWorkerIsRestarting_ShouldAbortItsRestart() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);
      // The worker loses its office process, and its restart does not end.
      worker.startGate = new CountDownLatch(1);
      worker.setReady(false);
      await(() -> worker.count("restart") == 1);

      pool.stop();

      assertThat(worker.calls).containsExactly("start", "restart", "abort", "stop");
    }

    @Test
    void whileAWorkerWaitsToRetry_ShouldNotWaitForTheDelay() throws Exception {

      final var worker = new FakeOfficeWorker();
      worker.failingStarts.set(Integer.MAX_VALUE);
      pool = builder(worker).startFailFast(false).build();
      pool.setRestartDelays(60_000L);
      pool.start();
      await(() -> worker.count("start") == 1);
      // Let the worker enter its delay.
      Thread.sleep(50);

      pool.stop();

      assertThat(worker.calls).containsExactly("start", "abort", "stop");
    }

    @Test
    void whenAWorkerFailsToAbortAndStop_ShouldStopAnyway() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);
      worker.failAbortAndStop = true;
      final var task = new BlockingTask();
      final var running = pool.submit(task);
      task.awaitStarted();

      assertThatCode(pool::stop).doesNotThrowAnyException();

      assertThat(running).isCompletedExceptionally();
      assertThat(worker.calls).containsExactly("start", "execute", "abort", "stop");
    }

    @Test
    void whileATimedOutTaskIsStillRunning_ShouldWaitForItWithoutCompletingItAgain()
        throws Exception {

      final var worker = new FakeOfficeWorker();
      pool = builder(worker).taskExecutionTimeout(100L).build();
      pool.start();

      // A task that ignores the interruption: it is still running after its timeout.
      final var release = new AtomicBoolean();
      final var future =
          pool.submit(
              context -> {
                while (!release.get()) {
                  try {
                    Thread.sleep(5);
                  } catch (InterruptedException ignored) {
                    // Ignored on purpose.
                  }
                }
              });
      final var timeout = failureOf(future);

      final var stopper =
          new Thread(
              () -> {
                try {
                  pool.stop();
                } catch (OfficeException ex) {
                  fail("Unexpected exception", ex);
                }
              });
      stopper.start();
      // The stop waits for the worker, which is still in the task.
      stopper.join(200);
      assertThat(stopper.isAlive()).isTrue();

      release.set(true);
      stopper.join(10_000);

      assertThat(stopper.isAlive()).isFalse();
      // The future keeps the failure of its timeout.
      assertThat(failureOf(future)).isSameAs(timeout);
      assertThat(worker.count("stop")).isEqualTo(1);
    }

    @Test
    void whenInterruptedWhileWaitingForTheWorkers_ShouldStayInterrupted() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);

      Thread.currentThread().interrupt();
      pool.stop();

      assertThat(Thread.interrupted()).isTrue();
      await(() -> worker.count("stop") == 1);
      assertThat(pool.isRunning()).isFalse();
    }

    @Test
    void whenATaskIsAddedAfterTheStop_ShouldFailIt() throws Exception {

      // A task can be submitted at the very moment the manager stops.
      started(new FakeOfficeWorker());
      pool.stop();
      final var job = new OfficeJob(NOOP);

      pool.enqueue(job);

      assertThat(failureOf(job.getFuture()))
          .hasMessageStartingWith("Task was not executed, the office manager is stopped: ");
      assertThat(pool.getStatus().queueSize()).isZero();
    }

    @Test
    void withAnEndedTaskLeftInTheQueue_ShouldIgnoreIt() throws Exception {

      final var worker = new FakeOfficeWorker();
      started(worker);
      final var task = new BlockingTask();
      pool.submit(task);
      task.awaitStarted();
      final var ended = new OfficeJob(NOOP);
      assertThat(ended.tryEndWaiting()).isTrue();
      pool.requeueJob(ended);

      pool.stop();

      assertThat(ended.getFuture()).isNotDone();
      assertThat(pool.getStatus().queueSize()).isZero();
    }
  }

  @Nested
  class TemporaryFiles {

    @Test
    void shouldCreateNumberedFilesInTheTempDir() {

      pool = builder(new FakeOfficeWorker()).build();

      final var first = pool.makeTemporaryFile();
      final var second = pool.makeTemporaryFile("pdf");
      final var third = pool.makeTemporaryFile(" ");

      assertThat(pool.getTempDir().getParentFile()).isEqualTo(workingDir);
      assertThat(pool.getTempDir().getName()).startsWith(".jodconverter_");
      assertThat(first).isEqualTo(new File(pool.getTempDir(), "tempfile_0"));
      assertThat(second).isEqualTo(new File(pool.getTempDir(), "tempfile_1.pdf"));
      assertThat(third).isEqualTo(new File(pool.getTempDir(), "tempfile_2"));
    }
  }

  @Nested
  class Builder {

    @Test
    void shouldKeepTheDefaultWorkingDirWhenGivenNullsOrBlank() throws Exception {

      final var worker = new FakeOfficeWorker();
      final var builder =
          FakeOfficeWorkerPool.builder()
              .workers(worker)
              .workingDir((File) null)
              .workingDir((String) null)
              .workingDir(" ");

      assertThat(builder.getWorkingDir()).isEqualTo(OfficeUtils.getDefaultWorkingDir());
      assertThat(builder.isInstall()).isFalse();

      // The default timeouts are long, and there is no limit to the queue.
      pool = builder.workingDir(workingDir.getPath()).install().build();
      assertThat(builder.getWorkingDir()).isEqualTo(workingDir);
      assertThat(builder.isInstall()).isTrue();
      pool.start();
      final var running = new BlockingTask();
      final var futures = new ArrayList<CompletableFuture<Void>>();
      futures.add(pool.submit(running));
      for (var i = 0; i < 10; i++) {
        futures.add(pool.submit(NOOP));
      }
      running.awaitStarted();
      Thread.sleep(100);
      assertThat(futures).noneMatch(CompletableFuture::isDone);
      running.release.countDown();
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
    }

    @Test
    void withNegativeValues_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> FakeOfficeWorkerPool.builder().taskExecutionTimeout(-1L))
          .withMessage("taskExecutionTimeout -1 must be greater than or equal to 0");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> FakeOfficeWorkerPool.builder().taskQueueTimeout(-1L))
          .withMessage("taskQueueTimeout -1 must be greater than or equal to 0");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> FakeOfficeWorkerPool.builder().taskQueueCapacity(-1))
          .withMessage("taskQueueCapacity -1 must be greater than or equal to 0");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNullWorkingDirInConstructor_ShouldThrowNullPointerException() {

      assertThatNullPointerException()
          .isThrownBy(() -> new AbstractOfficeWorkerPool(null, 0L, 0L, 0, true) {})
          .withMessage("workingDir must not be null");
    }
  }
}
