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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The thread of an {@link OfficeWorker} in an {@link AbstractOfficeWorkerPool}: it makes its worker
 * ready, takes a job from the queue of the pool, executes it, and starts again.
 *
 * <p>The runner only goes to the queue while its worker is ready. A worker that is starting or
 * restarting is thus never given a job, and the jobs go to the workers that are ready.
 */
final class OfficeWorkerRunner implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeWorkerRunner.class);

  private final AbstractOfficeWorkerPool pool;
  private final OfficeWorker worker;
  private final boolean failFast;

  // Completed when the worker is ready for the first time, or failed to be with failFast.
  private final CompletableFuture<Void> firstStart = new CompletableFuture<>();

  // Guards the job being executed, so that only that job can be aborted.
  private final Object jobLock = new Object();
  private OfficeJob currentJob;

  private volatile OfficeWorkerState state = OfficeWorkerState.STOPPED;
  private volatile boolean stopping;
  private volatile Thread thread;

  // Only used by the thread of this runner.
  private boolean startAttempted;
  private boolean started;
  private boolean restartRequired;

  /**
   * Creates the runner of the given worker.
   *
   * @param pool The pool this runner belongs to.
   * @param worker The worker to drive.
   * @param failFast Whether a failure of the first start is final, instead of being retried.
   */
  /* default */ OfficeWorkerRunner(
      final AbstractOfficeWorkerPool pool, final OfficeWorker worker, final boolean failFast) {
    this.pool = pool;
    this.worker = worker;
    this.failFast = failFast;
  }

  /* default */ OfficeWorkerState getState() {
    return state;
  }

  /* default */ CompletableFuture<Void> getFirstStart() {
    return firstStart;
  }

  /**
   * Starts the thread of this runner.
   *
   * @param threadFactory The factory of the thread.
   */
  /* default */ void start(final ThreadFactory threadFactory) {
    // The worker is starting from now on, not when its thread gets to run.
    state = OfficeWorkerState.STARTING;
    thread = threadFactory.newThread(this);
    thread.start();
  }

  @Override
  public void run() {

    try {
      while (!stopping && makeReady()) {
        state = OfficeWorkerState.READY;
        final var job = pool.takeJob();
        if (job == null) {
          // Nothing to do yet: check that the worker is still ready, then wait again.
          continue;
        }
        if (!worker.isReady()) {
          // This worker cannot execute the job after all: leave it to the others.
          pool.requeueJob(job);
          continue;
        }
        execute(job);
      }
    } finally {
      state = OfficeWorkerState.STOPPED;
      stopWorker();
      // Does nothing if the worker did start.
      firstStart.completeExceptionally(
          new OfficeException("The office worker was stopped before it was ready"));
    }
  }

  // Makes the worker ready if it is not, retrying after a delay when it fails. Returns false if
  // the runner must end: it is stopping, or the first start failed with failFast.
  private boolean makeReady() {

    if (started && !restartRequired && worker.isReady()) {
      return true;
    }

    state = started ? OfficeWorkerState.RESTARTING : OfficeWorkerState.STARTING;
    var failures = 0;
    while (!stopping) {
      try {
        if (startAttempted) {
          worker.restart();
        } else {
          startAttempted = true;
          worker.start();
        }
        started = true;
        restartRequired = false;
        firstStart.complete(null);
        return true;
      } catch (OfficeException | RuntimeException ex) {
        if (stopping) {
          break;
        }
        if (failFast && !started) {
          firstStart.completeExceptionally(ex);
          return false;
        }
        final var delay = pool.getRestartDelay(failures++);
        LOGGER.warn("An office worker could not be made ready; retrying in {} ms", delay, ex);
        sleep(delay);
      }
    }
    return false;
  }

  private void sleep(final long delay) {
    try {
      Thread.sleep(delay);
    } catch (InterruptedException ex) {
      // The runner is being stopped: the caller checks it.
      LOGGER.trace("Interrupted while waiting to retry", ex);
    }
  }

  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  private void execute(final OfficeJob job) {

    synchronized (jobLock) {
      if (stopping) {
        // The stop found this worker idle, and did not abort it: the job must not be started.
        pool.requeueJob(job);
        return;
      }
      if (!job.tryStart(this)) {
        // The job timed out in the queue, or was cancelled, just before it was taken.
        return;
      }
      currentJob = job;
    }
    state = OfficeWorkerState.BUSY;
    pool.scheduleExecutionTimeout(job);

    Throwable failure = null;
    try {
      worker.execute(job.getTask());
    } catch (Throwable ex) {
      // Whatever happens, the job must be completed and the runner must go on.
      failure = ex;
    }

    synchronized (jobLock) {
      currentJob = null;
    }
    // An aborted job interrupts this thread; the next job must not inherit that.
    final var interrupted = Thread.interrupted();

    if (job.tryEndRunning()) {
      if (failure == null) {
        job.getFuture().complete(null);
      } else {
        job.getFuture().completeExceptionally(pool.toOfficeException(job, failure));
      }
    } else {
      // The job timed out or was cancelled while running, and its future is already completed:
      // the result is discarded. The worker was aborted, so it must be restarted.
      LOGGER.debug(
          "Discarding the result of an aborted task (interrupted: {}): {}",
          interrupted,
          job.getTask());
      restartRequired = true;
    }
  }

  /**
   * Makes the execution of the given job end as soon as possible, if this runner is still executing
   * it.
   *
   * @param job The job to abort.
   */
  /* default */ void abort(final OfficeJob job) {
    synchronized (jobLock) {
      if (currentJob == job) {
        interruptWorker(true);
      }
    }
  }

  /**
   * Asks this runner to end. The job it executes is cancelled, and whatever its worker is blocked
   * in (a task, a start, a restart) is aborted. A worker that is idle is only woken up: it has
   * nothing to abort, and can be stopped properly.
   */
  /* default */ void requestStop() {

    stopping = true;
    final OfficeJob cancelled;
    synchronized (jobLock) {
      cancelled = currentJob != null && currentJob.tryEndRunning() ? currentJob : null;
      final var current = state;
      interruptWorker(
          currentJob != null
              || current == OfficeWorkerState.STARTING
              || current == OfficeWorkerState.RESTARTING);
    }
    if (cancelled != null) {
      cancelled
          .getFuture()
          .completeExceptionally(
              new OfficeException(
                  String.format(
                      "Task was cancelled, the office manager is stopping: %s",
                      cancelled.getTask())));
    }
  }

  /**
   * Waits for the thread of this runner to end.
   *
   * @throws InterruptedException If the calling thread is interrupted while waiting.
   */
  /* default */ void join() throws InterruptedException {
    final var runnerThread = thread;
    if (runnerThread != null) {
      runnerThread.join();
    }
  }

  // Interrupts the thread of this runner, after having aborted its worker if asked to.
  private void interruptWorker(final boolean abort) {
    final var runnerThread = thread;
    if (runnerThread == null || !runnerThread.isAlive()) {
      // The worker is not driven by any thread: there is nothing to interrupt.
      return;
    }
    if (abort) {
      try {
        worker.abort();
      } catch (RuntimeException ex) {
        LOGGER.warn("An office worker could not be aborted", ex);
      }
    }
    runnerThread.interrupt();
  }

  private void stopWorker() {
    // The stop of the worker must not be disturbed by the interruption that ended the loop.
    final var interrupted = Thread.interrupted();
    LOGGER.trace("Stopping an office worker (interrupted: {})", interrupted);
    try {
      worker.stop();
    } catch (OfficeException | RuntimeException ex) {
      LOGGER.warn("An office worker could not be stopped properly", ex);
    }
  }
}
