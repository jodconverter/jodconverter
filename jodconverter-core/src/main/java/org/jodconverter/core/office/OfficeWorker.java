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

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.task.OfficeTask;

/**
 * What a worker of an {@link AbstractOfficeWorkerPool} drives to execute tasks: an office process
 * it starts, a connection to a process it does not own, a remote server...
 *
 * <p>The pool gives each office worker its own thread. That thread is the only one calling {@link
 * #start()}, {@link #restart()}, {@link #isReady()}, {@link #execute(OfficeTask)} and {@link
 * #stop()}, one after the other; these methods need no synchronization between them. Only {@link
 * #abort()} is called from other threads, while one of the blocking methods may be running.
 */
public interface OfficeWorker {

  /**
   * Makes this worker ready to execute tasks, for the first time. This method blocks until the
   * worker is ready.
   *
   * @throws OfficeException If the worker could not be made ready.
   */
  void start() throws OfficeException;

  /**
   * Makes this worker ready again, after it stopped being ready, a task was aborted, or a previous
   * {@link #start()} or {@code restart()} failed. Whatever is left of the previous state must be
   * cleaned up first. This method blocks until the worker is ready.
   *
   * @throws OfficeException If the worker could not be made ready.
   */
  void restart() throws OfficeException;

  /**
   * Gets whether this worker can execute a task now. A worker that is no longer ready is restarted
   * before it is given another task: this is how a worker asks to be restarted, after a maximum
   * number of tasks for example.
   *
   * @return {@code true} if this worker is ready, {@code false} otherwise.
   */
  boolean isReady();

  /**
   * Executes the given task. This method blocks until the task is done.
   *
   * @param task The task to execute.
   * @throws OfficeException If the task failed.
   */
  void execute(@NonNull OfficeTask task) throws OfficeException;

  /**
   * Makes the method this worker is blocked in ({@link #execute(OfficeTask)}, {@link #start()} or
   * {@link #restart()}) end as soon as possible, by killing the office process for example. It is
   * called from another thread, when a task exceeds its execution timeout or is cancelled, and when
   * the pool is stopped while the worker is not idle. The thread of the worker is interrupted right
   * after this call.
   *
   * <p>This method must not block for long, and must not fail when the worker is not blocked. After
   * an aborted task, the worker is restarted before it is given another task.
   */
  void abort();

  /**
   * Stops this worker and releases its resources. It is called once, when the pool is stopped or
   * could not be started.
   *
   * @throws OfficeException If the worker could not be stopped properly.
   */
  void stop() throws OfficeException;
}
