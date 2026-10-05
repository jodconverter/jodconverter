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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.jodconverter.core.task.OfficeTask;

/**
 * An {@link OfficeWorker} for testing purposes, driven by the tests: its starts can be blocked or
 * made to fail, it can stop being ready, and it records what the pool asks it to do.
 */
public final class FakeOfficeWorker implements OfficeWorker {

  /** What the pool called, in order: start, restart, execute, abort, stop. */
  public final List<String> calls = new CopyOnWriteArrayList<>();

  public final AtomicInteger executedTasks = new AtomicInteger();

  /** The number of the next starts or restarts that fail, with an {@link OfficeException}. */
  public final AtomicInteger failingStarts = new AtomicInteger();

  /** When true, a failing start throws a {@link RuntimeException} instead. */
  public volatile boolean failWithRuntimeException;

  /** When set, a start or restart blocks until the latch is released, or the worker aborted. */
  public volatile CountDownLatch startGate;

  /** Released when a start or restart begins. */
  public volatile CountDownLatch startBegun = new CountDownLatch(1);

  /** The number of tasks after which the worker is no longer ready; 0 means no limit. */
  public volatile int maxTasks;

  /** When set, called each time the pool asks whether the worker is ready. */
  public volatile Runnable onIsReady;

  /** When true, abort and stop throw. */
  public volatile boolean failAbortAndStop;

  private volatile boolean ready;
  private volatile int tasksSinceStart;

  /**
   * Makes this worker ready or not, as an office process that is lost would.
   *
   * @param ready Whether the worker is ready.
   */
  public void setReady(final boolean ready) {
    this.ready = ready;
  }

  /**
   * Counts the calls of the given kind.
   *
   * @param call The call: start, restart, execute, abort or stop.
   * @return The number of calls.
   */
  public int count(final String call) {
    return (int) calls.stream().filter(call::equals).count();
  }

  @Override
  public void start() throws OfficeException {
    calls.add("start");
    makeReady();
  }

  @Override
  public void restart() throws OfficeException {
    calls.add("restart");
    makeReady();
  }

  private void makeReady() throws OfficeException {

    ready = false;
    startBegun.countDown();
    final CountDownLatch gate = startGate;
    if (gate != null) {
      try {
        gate.await();
      } catch (InterruptedException ex) {
        throw new OfficeException("The start was aborted", ex);
      }
    }
    if (failingStarts.getAndUpdate(count -> count > 0 ? count - 1 : 0) > 0) {
      if (failWithRuntimeException) {
        throw new IllegalStateException("The start failed unexpectedly");
      }
      throw new OfficeException("The start failed");
    }
    tasksSinceStart = 0;
    ready = true;
  }

  @Override
  public boolean isReady() {
    final Runnable hook = onIsReady;
    if (hook != null) {
      hook.run();
    }
    return ready && (maxTasks == 0 || tasksSinceStart < maxTasks);
  }

  @Override
  public void execute(final OfficeTask task) throws OfficeException {
    calls.add("execute");
    tasksSinceStart++;
    task.execute(new SimpleOfficeContext());
    executedTasks.incrementAndGet();
  }

  @Override
  public void abort() {
    calls.add("abort");
    // An aborted office process is gone.
    ready = false;
    if (failAbortAndStop) {
      throw new IllegalStateException("The abort failed");
    }
  }

  @Override
  public void stop() throws OfficeException {
    calls.add("stop");
    ready = false;
    if (failAbortAndStop) {
      throw new OfficeException("The stop failed");
    }
  }
}
