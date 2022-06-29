/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_EXECUTION_TIMEOUT;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_QUEUE_TIMEOUT;
import static org.jodconverter.local.office.ExternalOfficeManager.DEFAULT_CONNECT_FAIL_FAST;
import static org.jodconverter.local.office.ExternalOfficeManager.DEFAULT_CONNECT_ON_START;
import static org.jodconverter.local.office.ExternalOfficeManager.DEFAULT_CONNECT_RETRY_INTERVAL;
import static org.jodconverter.local.office.ExternalOfficeManager.DEFAULT_CONNECT_TIMEOUT;
import static org.jodconverter.local.office.ExternalOfficeManager.DEFAULT_HOSTNAME;
import static org.jodconverter.local.office.ExternalOfficeManager.DEFAULT_MAX_TASKS_PER_CONNECTION;
import static org.jodconverter.local.office.ExternalOfficeManager.MAX_CONNECT_RETRY_INTERVAL;

import java.io.File;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;

/** Contains tests for the {@link ExternalOfficeManager} class. */
class ExternalOfficeManagerTest {

  @Nested
  class Make {

    @Test
    void shouldInitializedManagerWithDefaultValues() {

      final OfficeManager manager = ExternalOfficeManager.make();

      assertThat(manager).isInstanceOf(ExternalOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir")
          .satisfies(
              o ->
                  assertThat(o)
                      .asInstanceOf(InstanceOfAssertFactories.FILE)
                      .hasParent(OfficeUtils.getDefaultWorkingDir()));
      assertThat(manager)
          .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT);
      assertThat(manager)
          .extracting("entries")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .isInstanceOf(ExternalOfficeManagerPoolEntry.class)
                      .extracting(
                          "taskExecutionTimeout",
                          "connectOnStart",
                          "maxTasksPerConnection",
                          "connectionManager.connectTimeout",
                          "connectionManager.connectRetryInterval",
                          "connectionManager.connectFailFast",
                          "connectionManager.connection.officeUrl.connectString")
                      .containsExactly(
                          DEFAULT_TASK_EXECUTION_TIMEOUT,
                          DEFAULT_CONNECT_ON_START,
                          DEFAULT_MAX_TASKS_PER_CONNECTION,
                          DEFAULT_CONNECT_TIMEOUT,
                          DEFAULT_CONNECT_RETRY_INTERVAL,
                          DEFAULT_CONNECT_FAIL_FAST,
                          new OfficeUrl(DEFAULT_HOSTNAME, 2002).getConnectString()));
    }
  }

  @Nested
  class Install {

    @Test
    void shouldSetInstalledOfficeManagerHolder() {

      // Ensure we do not replace the current installed manager
      final OfficeManager installedManager = InstalledOfficeManagerHolder.getInstance();
      try {
        final OfficeManager manager = ExternalOfficeManager.install();
        assertThat(InstalledOfficeManagerHolder.getInstance()).isEqualTo(manager);
      } finally {
        InstalledOfficeManagerHolder.setInstance(installedManager);
      }
    }
  }

  @Nested
  class Build {

    @Test
    @SuppressWarnings("RedundantArrayCreation")
    void withNullValues_ShouldInitializedManagerWithDefaultValues() {

      final OfficeManager manager =
          ExternalOfficeManager.builder()
              .workingDir((String) null)
              .workingDir((File) null)
              .taskExecutionTimeout(null)
              .taskQueueTimeout(null)
              .pipeNames((String[]) null)
              .pipeNames(new String[] {})
              .hostName(null)
              .portNumbers((int[]) null)
              .portNumbers(new int[] {})
              .connectOnStart(null)
              .connectTimeout(null)
              .connectRetryInterval(null)
              .connectFailFast(null)
              .maxTasksPerConnection(null)
              .build();

      assertThat(manager).isInstanceOf(ExternalOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir")
          .satisfies(
              o ->
                  assertThat(o)
                      .asInstanceOf(InstanceOfAssertFactories.FILE)
                      .hasParent(OfficeUtils.getDefaultWorkingDir()));
      assertThat(manager)
          .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT);
      assertThat(manager)
          .extracting("entries")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .isInstanceOf(ExternalOfficeManagerPoolEntry.class)
                      .extracting(
                          "taskExecutionTimeout",
                          "connectOnStart",
                          "maxTasksPerConnection",
                          "connectionManager.connectTimeout",
                          "connectionManager.connectRetryInterval",
                          "connectionManager.connectFailFast",
                          "connectionManager.connection.officeUrl.connectString")
                      .containsExactly(
                          DEFAULT_TASK_EXECUTION_TIMEOUT,
                          DEFAULT_CONNECT_ON_START,
                          DEFAULT_MAX_TASKS_PER_CONNECTION,
                          DEFAULT_CONNECT_TIMEOUT,
                          DEFAULT_CONNECT_RETRY_INTERVAL,
                          DEFAULT_CONNECT_FAIL_FAST,
                          new OfficeUrl(DEFAULT_HOSTNAME, 2002).getConnectString()));
    }

    @Test
    void withCustomValues_ShouldInitializedManagerWithCustomValues(final @TempDir File testFolder) {

      final OfficeManager manager =
          ExternalOfficeManager.builder()
              .workingDir(testFolder.getPath())
              .taskExecutionTimeout(11_000L)
              .taskQueueTimeout(12_000L)
              .pipeNames("test")
              .hostName("localhost")
              .portNumbers(2003)
              .connectOnStart(false)
              .connectTimeout(5_000L)
              .connectRetryInterval(1_000L)
              .connectFailFast(true)
              .maxTasksPerConnection(99)
              .build();

      assertThat(manager).isInstanceOf(ExternalOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir")
          .satisfies(
              o ->
                  assertThat(o).asInstanceOf(InstanceOfAssertFactories.FILE).hasParent(testFolder));
      assertThat(manager).hasFieldOrPropertyWithValue("taskQueueTimeout", 12_000L);
      assertThat(manager)
          .extracting("entries")
          .asList()
          .hasSize(2)
          .allSatisfy(
              o ->
                  assertThat(o)
                      .isInstanceOf(ExternalOfficeManagerPoolEntry.class)
                      .extracting(
                          "taskExecutionTimeout",
                          "connectOnStart",
                          "maxTasksPerConnection",
                          "connectionManager.connectTimeout",
                          "connectionManager.connectRetryInterval",
                          "connectionManager.connectFailFast")
                      .containsExactly(11_000L, false, 99, 5_000L, 1_000L, true))
          .satisfies(
              o ->
                  assertThat(o.get(0))
                      .hasFieldOrPropertyWithValue(
                          "connectionManager.connection.officeUrl.connectString",
                          new OfficeUrl("localhost", 2003).getConnectString()))
          .satisfies(
              o ->
                  assertThat(o.get(1))
                      .hasFieldOrPropertyWithValue(
                          "connectionManager.connection.officeUrl.connectString",
                          new OfficeUrl("test").getConnectString()));
    }

    @Test
    void whenInvalidConnectTimeout_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> ExternalOfficeManager.builder().connectTimeout(-1L).build());
    }

    @Test
    void whenInvalidConnectRetryInterval_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> ExternalOfficeManager.builder().connectRetryInterval(-1L).build());
      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  ExternalOfficeManager.builder()
                      .connectRetryInterval(MAX_CONNECT_RETRY_INTERVAL + 1)
                      .build());
    }

    @Test
    void whenInvalidMaxTasksPerConnection_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> ExternalOfficeManager.builder().maxTasksPerConnection(-1).build());
    }
  }
}
