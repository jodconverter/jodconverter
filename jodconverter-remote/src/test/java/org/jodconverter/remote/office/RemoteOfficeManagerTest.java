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

package org.jodconverter.remote.office;

import static org.assertj.core.api.Assertions.*;
import static org.jodconverter.remote.office.RemoteOfficeManager.*;

import java.io.File;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeUtils;

/** Contains tests for the {@link RemoteOfficeManager} class. */
class RemoteOfficeManagerTest {

  @TempDir File testFolder;

  @Nested
  class Make {

    @Test
    void shouldInitializedManagerWithDefaultValues() {

      final var manager = RemoteOfficeManager.make("localhost");

      assertThat(manager).isInstanceOf(RemoteOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir")
          .satisfies(
              o ->
                  assertThat(o)
                      .asInstanceOf(InstanceOfAssertFactories.FILE)
                      .hasParent(OfficeUtils.getDefaultWorkingDir()));
      assertThat(manager)
          .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT)
          .hasFieldOrPropertyWithValue("taskExecutionTimeout", DEFAULT_TASK_EXECUTION_TIMEOUT);
      assertThat(manager)
          .extracting("workers")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .isInstanceOf(RemoteOfficeWorker.class)
                      .extracting("connectionUrl", "sslConfig", "connectTimeout", "socketTimeout")
                      .containsExactly(
                          "localhost", null, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SOCKET_TIMEOUT));
    }
  }

  @Nested
  class Install {

    @Test
    void shouldSetInstalledOfficeManagerHolder() {

      // Ensure we do not replace the current installed manager
      final var installedManager = InstalledOfficeManagerHolder.getInstance();
      try {
        final var manager = RemoteOfficeManager.install("localhost");
        assertThat(InstalledOfficeManagerHolder.getInstance()).isEqualTo(manager);
      } finally {
        InstalledOfficeManagerHolder.setInstance(installedManager);
      }
    }
  }

  @Nested
  class Build {

    @Test
    void withNullValues_ShouldInitializedManagerWithDefaultValues() {

      final var manager =
          RemoteOfficeManager.builder()
              .workingDir((String) null)
              .workingDir((File) null)
              .poolSize(null)
              .taskExecutionTimeout(null)
              .taskQueueTimeout(null)
              .urlConnection("localhost")
              .connectTimeout(null)
              .socketTimeout(null)
              .build();

      assertThat(manager).isInstanceOf(RemoteOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir")
          .satisfies(
              o ->
                  assertThat(o)
                      .asInstanceOf(InstanceOfAssertFactories.FILE)
                      .hasParent(OfficeUtils.getDefaultWorkingDir()));
      assertThat(manager)
          .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT)
          .hasFieldOrPropertyWithValue("taskExecutionTimeout", DEFAULT_TASK_EXECUTION_TIMEOUT);
      assertThat(manager)
          .extracting("workers")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .isInstanceOf(RemoteOfficeWorker.class)
                      .extracting("connectionUrl", "sslConfig", "connectTimeout", "socketTimeout")
                      .containsExactly(
                          "localhost", null, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SOCKET_TIMEOUT));
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void withCustomValues_ShouldInitializedManagerWithCustomValues() {

      final var workingDir = new File(testFolder, "temp");
      workingDir.mkdirs();

      final var manager =
          RemoteOfficeManager.builder()
              .workingDir(workingDir.getPath())
              .taskExecutionTimeout(500L)
              .taskQueueTimeout(501L)
              .poolSize(2)
              .urlConnection("localhost")
              .sslConfig(null)
              .connectTimeout(502L)
              .socketTimeout(503L)
              .build();

      assertThat(manager).isInstanceOf(RemoteOfficeManager.class);
      assertThat(manager)
          .extracting("tempDir")
          .satisfies(
              o ->
                  assertThat(o).asInstanceOf(InstanceOfAssertFactories.FILE).hasParent(workingDir));
      assertThat(manager)
          .hasFieldOrPropertyWithValue("taskQueueTimeout", 501L)
          .hasFieldOrPropertyWithValue("taskExecutionTimeout", 500L);
      assertThat(manager)
          .extracting("workers")
          .asList()
          .hasSize(2)
          .allSatisfy(
              o ->
                  assertThat(o)
                      .isInstanceOf(RemoteOfficeWorker.class)
                      .extracting("connectionUrl", "sslConfig", "connectTimeout", "socketTimeout")
                      .containsExactly("localhost", null, 502L, 503L));
    }

    @Test
    void whenMissingUrlConnection_ShouldThrowIllegalArgumentException() {

      assertThatNullPointerException().isThrownBy(() -> RemoteOfficeManager.builder().build());
    }

    @Test
    void whenInvalidPoolSize_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> RemoteOfficeManager.builder().poolSize(-1).build());
      assertThatIllegalArgumentException()
          .isThrownBy(() -> RemoteOfficeManager.builder().poolSize(MAX_POOL_SIZE + 1).build());
    }

    @Test
    void whenInvalidConnectTimeout_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> RemoteOfficeManager.builder().connectTimeout(-1L).build());
    }

    @Test
    void whenInvalidSocketTimeout_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> RemoteOfficeManager.builder().socketTimeout(-1L).build());
    }
  }
}
