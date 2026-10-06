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

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.task.OfficeTask;

/**
 * The task an office worker is executing, so that its thread can be interrupted when the connection
 * to the office process is lost: such a task cannot succeed anymore, and may not notice the loss by
 * itself.
 *
 * <p>The thread of the worker calls {@link #begin(OfficeTask)} and {@link #end()}; {@link
 * #interrupt()} is called from the thread that notices the lost connection.
 */
final class RunningTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(RunningTask.class);

  private final Predicate<OfficeTask> interruptible;
  private final Object lock = new Object();
  private OfficeTask task;
  private Thread thread;

  /**
   * Creates a new instance.
   *
   * @param interruptible Tells whether a task must be interrupted when the connection is lost. A
   *     task that caused the lost connection itself, and fails by itself, must not be.
   */
  /* default */ RunningTask(final Predicate<OfficeTask> interruptible) {
    this.interruptible = interruptible;
  }

  /**
   * Records the task that the current thread starts executing.
   *
   * @param task The task.
   */
  /* default */ void begin(final OfficeTask task) {
    synchronized (lock) {
      this.task = task;
      this.thread = Thread.currentThread();
    }
  }

  /** Records that the task is no longer executed. */
  /* default */ void end() {
    synchronized (lock) {
      task = null;
      thread = null;
    }
  }

  /**
   * Interrupts the thread of the task being executed, if there is one and if it is interruptible.
   */
  /* default */ void interrupt() {
    synchronized (lock) {
      if (task != null && interruptible.test(task)) {
        LOGGER.warn("Connection lost unexpectedly; interrupting the task being executed");
        thread.interrupt();
      }
    }
  }
}
