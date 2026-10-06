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

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import org.jodconverter.boot.autoconfigure.JodConverterLocalProperties;
import org.jodconverter.boot.autoconfigure.JodConverterRemoteProperties;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.task.LoadDocumentMode;
import org.jodconverter.remote.office.RemoteOfficeManager;

/**
 * Test both the {@link JodConverterLocalProperties} and {@link JodConverterRemoteProperties}
 * classes for default values.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:config/application-props-default.properties")
class AutoConfigurationDefaultPropertiesITest {

  @Autowired private JodConverterLocalProperties localProps;
  @Autowired private JodConverterRemoteProperties remoteProps;

  // Provided valid OfficeManager beans, so we will be able to test the Autowired properties.
  @TestConfiguration
  /* default */ static class TestConfig {

    @Bean
    /* default */ OfficeManager localOfficeManager() {
      return LocalOfficeManager.make();
    }

    @Bean
    /* default */ OfficeManager remoteOfficeManager() {
      return RemoteOfficeManager.make("some url");
    }
  }

  @Test
  void testLocalProperties() {

    assertThat(localProps)
        .extracting(
            "enabled",
            "officeHome",
            "hostName",
            "portNumbers",
            "pipeNames",
            "workingDir",
            "templateProfileDir",
            "processTimeout",
            "processRetryInterval",
            "afterStartProcessDelay",
            "existingProcessAction",
            "startFailFast",
            "keepAliveOnShutdown",
            "taskQueueCapacity",
            "taskQueueTimeout",
            "taskExecutionTimeout",
            "maxTasksPerProcess",
            "documentFormatRegistry",
            "applyDefaultLoadProperties",
            "useUnsafeQuietUpdate",
            "loadDocumentMode")
        .containsExactly(
            true,
            null,
            "127.0.0.1",
            null,
            new String[] {},
            null,
            null,
            Duration.ofMillis(120_000L),
            Duration.ofMillis(250L),
            Duration.ZERO,
            ExistingProcessAction.KILL,
            false,
            false,
            0,
            Duration.ofMillis(30_000L),
            Duration.ofMillis(120_000L),
            200,
            null,
            true,
            false,
            LoadDocumentMode.AUTO);
  }

  @Test
  void testRemoteProperties() {

    assertThat(remoteProps)
        .extracting(
            "enabled",
            "url",
            "connectTimeout",
            "socketTimeout",
            "workingDir",
            "poolSize",
            "taskExecutionTimeout",
            "taskQueueCapacity",
            "taskQueueTimeout",
            "ssl")
        .containsExactly(
            true,
            "https://localhost:8001",
            Duration.ofMillis(RemoteOfficeManager.DEFAULT_CONNECT_TIMEOUT),
            Duration.ofMillis(RemoteOfficeManager.DEFAULT_SOCKET_TIMEOUT),
            null,
            1,
            Duration.ofMillis(120_000L),
            0,
            Duration.ofMillis(30_000L),
            null);
  }
}
