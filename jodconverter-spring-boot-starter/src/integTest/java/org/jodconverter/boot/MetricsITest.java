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

package org.jodconverter.boot;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

import org.jodconverter.boot.autoconfigure.OfficeManagerMetrics;

/** Tests the meters auto-configured with Spring Boot Actuator. */
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@TestPropertySource(locations = "classpath:config/application-remote.properties")
class MetricsITest {

  private static final String NAME = "remoteOfficeManager";

  @Autowired private MeterRegistry registry;

  private double gauge(final String name) {
    return registry.get(name).tag("name", NAME).gauge().value();
  }

  @Test
  void whenTheManagerIsRunning_ShouldExposeTheStatusOfItsWorkers() {

    assertThat(gauge(OfficeManagerMetrics.QUEUE_SIZE)).isZero();
    assertThat(
            registry
                .get(OfficeManagerMetrics.WORKERS)
                .tag("name", NAME)
                .tag("state", "ready")
                .gauge()
                .value())
        .isEqualTo(1);
    assertThat(
            registry
                .get(OfficeManagerMetrics.WORKERS)
                .tag("name", NAME)
                .tag("state", "busy")
                .gauge()
                .value())
        .isZero();
    assertThat(gauge(OfficeManagerMetrics.WORKERS_TASKS)).isZero();
    assertThat(gauge(OfficeManagerMetrics.WORKERS_START_FAILURES)).isZero();
    assertThat(
            registry
                .get(OfficeManagerMetrics.WORKERS_RESTARTS)
                .tag("name", NAME)
                .functionCounter()
                .count())
        .isZero();
  }
}
