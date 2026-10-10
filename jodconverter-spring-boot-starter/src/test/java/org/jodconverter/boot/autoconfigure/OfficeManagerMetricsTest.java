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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import org.jodconverter.core.office.AbstractOfficeWorkerPool;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeWorkerPoolStatus;
import org.jodconverter.core.office.OfficeWorkerState;
import org.jodconverter.core.office.OfficeWorkerStatus;
import org.jodconverter.remote.office.RemoteOfficeManager;

/**
 * Contains tests for the {@link OfficeManagerMetrics} and {@link
 * JodConverterMetricsAutoConfiguration} classes.
 */
class OfficeManagerMetricsTest {

  private static final String POOL = "pool";

  private static AbstractOfficeWorkerPool poolWith(final OfficeWorkerPoolStatus status) {
    final var pool = mock(AbstractOfficeWorkerPool.class);
    given(pool.getStatus()).willReturn(status);
    return pool;
  }

  private static double gauge(final MeterRegistry registry, final String name) {
    return registry.get(name).tag("name", POOL).gauge().value();
  }

  private static double workers(final MeterRegistry registry, final String state) {
    return registry
        .get(OfficeManagerMetrics.WORKERS)
        .tag("name", POOL)
        .tag("state", state)
        .gauge()
        .value();
  }

  @Nested
  class BindTo {

    @Test
    void withAPool_ShouldExposeItsStatus() {

      final var pool =
          poolWith(
              new OfficeWorkerPoolStatus(
                  List.of(
                      new OfficeWorkerStatus(OfficeWorkerState.READY, 3, 1, 0),
                      new OfficeWorkerStatus(OfficeWorkerState.BUSY, 2, 2, 0),
                      new OfficeWorkerStatus(OfficeWorkerState.BUSY, 7, 0, 0),
                      new OfficeWorkerStatus(OfficeWorkerState.RESTARTING, 0, 4, 2)),
                  5));
      final var registry = new SimpleMeterRegistry();

      new OfficeManagerMetrics(Map.of(POOL, pool)).bindTo(registry);

      assertThat(gauge(registry, OfficeManagerMetrics.QUEUE_SIZE)).isEqualTo(5);
      assertThat(workers(registry, "starting")).isZero();
      assertThat(workers(registry, "ready")).isEqualTo(1);
      assertThat(workers(registry, "busy")).isEqualTo(2);
      assertThat(workers(registry, "restarting")).isEqualTo(1);
      assertThat(workers(registry, "stopped")).isZero();
      assertThat(gauge(registry, OfficeManagerMetrics.WORKERS_TASKS)).isEqualTo(12);
      assertThat(gauge(registry, OfficeManagerMetrics.WORKERS_START_FAILURES)).isEqualTo(2);
      assertThat(
              registry
                  .get(OfficeManagerMetrics.WORKERS_RESTARTS)
                  .tag("name", POOL)
                  .functionCounter()
                  .count())
          .isEqualTo(7);
    }

    @Test
    void whenTheStatusChanges_ShouldReadItAgain() {

      final var pool = poolWith(new OfficeWorkerPoolStatus(List.of(), 0));
      final var registry = new SimpleMeterRegistry();
      new OfficeManagerMetrics(Map.of(POOL, pool)).bindTo(registry);

      assertThat(gauge(registry, OfficeManagerMetrics.QUEUE_SIZE)).isZero();

      given(pool.getStatus()).willReturn(new OfficeWorkerPoolStatus(List.of(), 9));

      assertThat(gauge(registry, OfficeManagerMetrics.QUEUE_SIZE)).isEqualTo(9);
    }

    @Test
    void withAManagerThatIsNotAPool_ShouldHaveNoMeter() {

      final var managers = new LinkedHashMap<String, OfficeManager>();
      managers.put("other", mock(OfficeManager.class));
      final var registry = new SimpleMeterRegistry();

      new OfficeManagerMetrics(managers).bindTo(registry);

      assertThat(registry.getMeters()).isEmpty();
    }
  }

  @Nested
  class AutoConfiguration {

    private final ApplicationContextRunner runner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JodConverterMetricsAutoConfiguration.class));

    @Test
    void withAnOfficeManager_ShouldCreateTheMeters() {

      runner
          .withBean(OfficeManager.class, () -> RemoteOfficeManager.make("http://localhost/"))
          .run(context -> assertThat(context).hasSingleBean(OfficeManagerMetrics.class));
    }

    @Test
    void withoutOfficeManager_ShouldNotCreateTheMeters() {

      runner.run(context -> assertThat(context).doesNotHaveBean(OfficeManagerMetrics.class));
    }

    @Test
    void whenDisabled_ShouldNotCreateTheMeters() {

      runner
          .withBean(OfficeManager.class, () -> RemoteOfficeManager.make("http://localhost/"))
          .withPropertyValues("jodconverter.metrics.enabled=false")
          .run(context -> assertThat(context).doesNotHaveBean(OfficeManagerMetrics.class));
    }

    @Test
    void withoutMicrometer_ShouldNotCreateTheMeters() {

      runner
          .withBean(OfficeManager.class, () -> RemoteOfficeManager.make("http://localhost/"))
          .withClassLoader(new FilteredClassLoader("io.micrometer"))
          .run(context -> assertThat(context).doesNotHaveBean("jodconverterMetrics"));
    }

    @Test
    void withACustomBean_ShouldKeepIt() {

      final var custom = new OfficeManagerMetrics(Map.of());

      runner
          .withBean(OfficeManager.class, () -> RemoteOfficeManager.make("http://localhost/"))
          .withBean("jodconverterMetrics", OfficeManagerMetrics.class, () -> custom)
          .run(context -> assertThat(context.getBean(OfficeManagerMetrics.class)).isSameAs(custom));
    }
  }
}
