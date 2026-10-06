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

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A snapshot of an {@link AbstractOfficeWorkerPool}: its workers and its queue.
 *
 * @param workers The status of each worker, in the order of the workers of the pool.
 * @param queueSize The number of tasks waiting in the queue for a worker.
 */
public record OfficeWorkerPoolStatus(
    @NonNull List<@NonNull OfficeWorkerStatus> workers, int queueSize) {

  /**
   * Creates a status.
   *
   * @param workers The status of each worker.
   * @param queueSize The number of tasks waiting in the queue.
   */
  public OfficeWorkerPoolStatus {
    workers = List.copyOf(workers);
  }

  /**
   * Counts the workers that are in the given state.
   *
   * @param state The state.
   * @return The number of workers in that state.
   */
  public int count(final @NonNull OfficeWorkerState state) {
    return (int) workers.stream().filter(worker -> worker.state() == state).count();
  }

  /**
   * Gets whether at least one worker can execute tasks, that is, is ready or executing one.
   *
   * @return {@code true} if a worker is ready or busy, {@code false} otherwise.
   */
  public boolean isRunning() {
    return workers.stream()
        .anyMatch(
            worker ->
                worker.state() == OfficeWorkerState.READY
                    || worker.state() == OfficeWorkerState.BUSY);
  }
}
