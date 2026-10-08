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
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import org.jodconverter.core.office.AbstractOfficeWorkerPool;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeWorkerStatus;

/**
 * A {@link HealthIndicator} of the office managers of the application: up when every manager is
 * running, down otherwise. For a manager that is a pool of workers, the details give the status of
 * each worker and the number of tasks waiting in the queue.
 */
public class OfficeManagerHealthIndicator implements HealthIndicator {

  private final Map<String, OfficeManager> officeManagers;

  /**
   * Creates a new indicator for the given managers.
   *
   * @param officeManagers The office managers, by bean name.
   */
  public OfficeManagerHealthIndicator(final @NonNull Map<String, OfficeManager> officeManagers) {
    this.officeManagers = new LinkedHashMap<>(officeManagers);
  }

  @Override
  public @NonNull Health health() {

    var up = true;
    final var details = new LinkedHashMap<String, Object>();
    for (final var entry : officeManagers.entrySet()) {
      final var manager = entry.getValue();
      final var running = manager.isRunning();
      up &= running;
      details.put(entry.getKey(), detailsOf(manager, running));
    }
    return (up ? Health.up() : Health.down()).withDetails(details).build();
  }

  private static Map<String, Object> detailsOf(final OfficeManager manager, final boolean running) {

    final var details = new LinkedHashMap<String, Object>();
    details.put("running", running);
    if (manager instanceof AbstractOfficeWorkerPool pool) {
      final var status = pool.getStatus();
      details.put("queueSize", status.queueSize());
      details.put(
          "workers",
          status.workers().stream().map(OfficeManagerHealthIndicator::detailsOf).toList());
    }
    return details;
  }

  private static Map<String, Object> detailsOf(final OfficeWorkerStatus worker) {

    final var details = new LinkedHashMap<String, Object>();
    details.put("state", worker.state().name());
    details.put("tasksSinceStart", worker.tasksSinceStart());
    details.put("restarts", worker.restarts());
    details.put("startFailures", worker.startFailures());
    return details;
  }
}
