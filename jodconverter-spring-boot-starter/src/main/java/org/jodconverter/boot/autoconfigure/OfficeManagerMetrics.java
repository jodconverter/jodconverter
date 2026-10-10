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

package org.jodconverter.boot.autoconfigure;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToIntFunction;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.office.AbstractOfficeWorkerPool;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeWorkerState;
import org.jodconverter.core.office.OfficeWorkerStatus;

/**
 * A {@link MeterBinder} that exposes the status of the office managers that are pools of workers:
 * the tasks waiting in the queue, the tasks executed again after their worker was lost, the workers
 * in each state, the tasks they executed since they were last made ready, their restarts and their
 * failed start attempts. Each meter has a {@code name} tag, the name of the office manager bean.
 *
 * <p>The meters read {@link AbstractOfficeWorkerPool#getStatus()} when they are collected; nothing
 * is recorded while a task is executed.
 */
public class OfficeManagerMetrics implements MeterBinder {

  /** The tasks waiting in the queue of the manager for a worker. */
  public static final String QUEUE_SIZE = "jodconverter.queue.size";

  /** The times a task of the manager was executed again after its worker was lost. */
  public static final String TASKS_RETRIES = "jodconverter.tasks.retries";

  /** The workers of the manager in a state, given by the {@code state} tag. */
  public static final String WORKERS = "jodconverter.workers";

  /** The tasks executed by the workers of the manager since each was last made ready. */
  public static final String WORKERS_TASKS = "jodconverter.workers.tasks";

  /** The restarts of the workers of the manager since it was started. */
  public static final String WORKERS_RESTARTS = "jodconverter.workers.restarts";

  /** The start attempts in a row that failed, for the workers of the manager. */
  public static final String WORKERS_START_FAILURES = "jodconverter.workers.start.failures";

  private static final String NAME_TAG = "name";
  private static final String STATE_TAG = "state";
  private static final String TASKS_UNIT = "tasks";
  private static final String WORKERS_UNIT = "workers";

  private final Map<String, AbstractOfficeWorkerPool> pools = new LinkedHashMap<>();

  /**
   * Creates a new binder for the given managers. The managers that are not a pool of workers have
   * no meter.
   *
   * @param officeManagers The office managers, by bean name.
   */
  public OfficeManagerMetrics(final @NonNull Map<String, OfficeManager> officeManagers) {
    officeManagers.forEach(
        (name, manager) -> {
          if (manager instanceof AbstractOfficeWorkerPool pool) {
            pools.put(name, pool);
          }
        });
  }

  @Override
  public void bindTo(final @NonNull MeterRegistry registry) {
    pools.forEach((name, pool) -> bind(registry, name, pool));
  }

  private static void bind(
      final MeterRegistry registry, final String name, final AbstractOfficeWorkerPool pool) {

    Gauge.builder(QUEUE_SIZE, pool, p -> p.getStatus().queueSize())
        .description("Tasks waiting in the queue for a worker")
        .baseUnit(TASKS_UNIT)
        .tag(NAME_TAG, name)
        .register(registry);

    FunctionCounter.builder(TASKS_RETRIES, pool, p -> p.getStatus().taskRetries())
        .description("Times a task was executed again after its worker was lost")
        .tag(NAME_TAG, name)
        .register(registry);

    for (final var state : OfficeWorkerState.values()) {
      Gauge.builder(WORKERS, pool, p -> p.getStatus().count(state))
          .description("Workers in a state")
          .baseUnit(WORKERS_UNIT)
          .tag(NAME_TAG, name)
          .tag(STATE_TAG, state.name().toLowerCase(Locale.ROOT))
          .register(registry);
    }

    Gauge.builder(WORKERS_TASKS, pool, p -> sum(p, OfficeWorkerStatus::tasksSinceStart))
        .description("Tasks executed by the workers since each was last made ready")
        .baseUnit(TASKS_UNIT)
        .tag(NAME_TAG, name)
        .register(registry);

    FunctionCounter.builder(WORKERS_RESTARTS, pool, p -> sum(p, OfficeWorkerStatus::restarts))
        .description("Restarts of the workers")
        .tag(NAME_TAG, name)
        .register(registry);

    Gauge.builder(WORKERS_START_FAILURES, pool, p -> sum(p, OfficeWorkerStatus::startFailures))
        .description("Start attempts in a row that failed, for all the workers")
        .tag(NAME_TAG, name)
        .register(registry);
  }

  private static double sum(
      final AbstractOfficeWorkerPool pool, final ToIntFunction<OfficeWorkerStatus> value) {
    return pool.getStatus().workers().stream().mapToInt(value).sum();
  }
}
