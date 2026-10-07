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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.remote.office.RemoteOfficeManager;

/** Contains tests for the {@link OfficeManagerHealthIndicator} class. */
class OfficeManagerHealthIndicatorTest {

  @Test
  void whenAManagerIsNotRunning_ShouldBeDown() {

    final var running = mock(OfficeManager.class);
    given(running.isRunning()).willReturn(true);
    final var stopped = mock(OfficeManager.class);
    given(stopped.isRunning()).willReturn(false);
    final var managers = new LinkedHashMap<String, OfficeManager>();
    managers.put("first", running);
    managers.put("second", stopped);

    final var health = new OfficeManagerHealthIndicator(managers).health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    // A manager that is not a pool only tells whether it is running.
    assertThat(health.getDetails())
        .containsExactly(
            Map.entry("first", Map.of("running", true)),
            Map.entry("second", Map.of("running", false)));
  }

  @Test
  @SuppressWarnings("unchecked")
  void whenAPoolIsNotStarted_ShouldBeDownWithoutAnyWorker() {

    final var pool = RemoteOfficeManager.make("http://localhost/");

    final var health = new OfficeManagerHealthIndicator(Map.of("remote", pool)).health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    final var details = (Map<String, Object>) health.getDetails().get("remote");
    assertThat(details)
        .containsEntry("running", false)
        .containsEntry("queueSize", 0)
        .containsEntry("workers", List.of());
  }
}
