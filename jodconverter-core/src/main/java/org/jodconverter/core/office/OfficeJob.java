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
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.jodconverter.core.task.OfficeTask;

/**
 * A task submitted to an {@link AbstractOfficeWorkerPool}, with the future of its result.
 *
 * <p>A job is waiting, then running, then done. Each change of state can happen only once, and
 * several threads compete for it: the worker that takes the job, the watchdog of the timeouts, the
 * caller that cancels, and the pool that stops. The one that wins the change of state is the one
 * that completes the future.
 */
final class OfficeJob {

  private static final int WAITING = 0;
  private static final int RUNNING = 1;
  private static final int DONE = 2;

  private final OfficeTask task;
  private final CompletableFuture<Void> future = new CompletableFuture<>();
  private final AtomicInteger state = new AtomicInteger(WAITING);

  // The timeout that currently applies to this job: the queue timeout, then the execution one.
  private volatile Future<?> timeout;

  // The worker that runs this job, once it is running.
  private volatile OfficeWorkerRunner runner;

  /* default */ OfficeJob(final OfficeTask task) {
    this.task = task;
  }

  /* default */ OfficeTask getTask() {
    return task;
  }

  /* default */ CompletableFuture<Void> getFuture() {
    return future;
  }

  /* default */ OfficeWorkerRunner getRunner() {
    return runner;
  }

  // Replaces the timeout of this job, cancelling the previous one.
  /* default */ void setTimeout(final Future<?> timeout) {
    final Future<?> previous = this.timeout;
    this.timeout = timeout;
    if (previous != null) {
      previous.cancel(false);
    }
  }

  /**
   * Makes this waiting job a running job of the given worker.
   *
   * @param runner The worker that will run this job.
   * @return {@code true} if the job is now running, {@code false} if it was no longer waiting.
   */
  /* default */ boolean tryStart(final OfficeWorkerRunner runner) {
    if (state.compareAndSet(WAITING, RUNNING)) {
      this.runner = runner;
      return true;
    }
    return false;
  }

  /**
   * Ends this job while it is waiting.
   *
   * @return {@code true} if the caller ended the job and must complete its future, {@code false} if
   *     the job was no longer waiting.
   */
  /* default */ boolean tryEndWaiting() {
    return end(WAITING);
  }

  /**
   * Ends this job while it is running.
   *
   * @return {@code true} if the caller ended the job and must complete its future, {@code false} if
   *     the job was no longer running.
   */
  /* default */ boolean tryEndRunning() {
    return end(RUNNING);
  }

  private boolean end(final int expectedState) {
    if (state.compareAndSet(expectedState, DONE)) {
      setTimeout(null);
      return true;
    }
    return false;
  }
}
