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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

import org.jodconverter.boot.autoconfigure.OfficeManagerHealthIndicator;

/** Tests the health indicator auto-configured with Spring Boot Actuator. */
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@TestPropertySource(locations = "classpath:config/application-remote.properties")
class HealthIndicatorITest {

  @Autowired
  @Qualifier("jodconverterHealthIndicator")
  private HealthIndicator healthIndicator;

  @Test
  @SuppressWarnings("unchecked")
  void whenTheManagerIsRunning_ShouldBeUpWithTheStatusOfItsWorkers() {

    assertThat(healthIndicator).isInstanceOf(OfficeManagerHealthIndicator.class);

    final var health = healthIndicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails()).containsOnlyKeys("remoteOfficeManager");
    final var details = (Map<String, Object>) health.getDetails().get("remoteOfficeManager");
    assertThat(details).containsEntry("running", true).containsEntry("queueSize", 0);
    final var workers = (List<Map<String, Object>>) details.get("workers");
    assertThat(workers)
        .hasSize(1)
        .first()
        .satisfies(
            worker ->
                assertThat(worker)
                    .containsEntry("state", "READY")
                    .containsEntry("tasksSinceStart", 0)
                    .containsEntry("restarts", 0)
                    .containsEntry("startFailures", 0));
  }
}
