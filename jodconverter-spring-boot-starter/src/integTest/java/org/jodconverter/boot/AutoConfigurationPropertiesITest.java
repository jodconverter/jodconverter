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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import org.jodconverter.boot.autoconfigure.JodConverterLocalProperties;
import org.jodconverter.boot.autoconfigure.JodConverterRemoteProperties;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.remote.office.RemoteOfficeManager;

/**
 * Test both the {@link JodConverterLocalProperties} and {@link JodConverterRemoteProperties}
 * classes.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:config/application-props.properties")
class AutoConfigurationPropertiesITest {

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
            "taskQueueTimeout",
            "taskExecutionTimeout",
            "maxTasksPerProcess",
            "documentFormatRegistry",
            "applyDefaultLoadProperties",
            "useUnsafeQuietUpdate",
            "loadDocumentMode")
        .containsExactly(
            true,
            "office-home",
            "localhost",
            new int[] {2005, 2006},
            new String[] {"pipe1", "pipe2"},
            "working-dir",
            "template-profile-dir",
            190_000L,
            1_000L,
            3_000L,
            "fail",
            true,
            true,
            70_000L,
            70_000L,
            20,
            null,
            true,
            true,
            "Remote");
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
            "taskQueueTimeout",
            "ssl.enabled",
            "ssl.ciphers",
            "ssl.enabledProtocols",
            "ssl.keyAlias",
            "ssl.keyPassword",
            "ssl.keyStore",
            "ssl.keyStorePassword",
            "ssl.keyStoreType",
            "ssl.keyStoreProvider",
            "ssl.trustStore",
            "ssl.trustStorePassword",
            "ssl.trustStoreType",
            "ssl.trustStoreProvider",
            "ssl.protocol",
            "ssl.trustAll",
            "ssl.verifyHostname")
        .containsExactly(
            true,
            "https://localhost:8001",
            30_000L,
            60_000L,
            "working-dir",
            1,
            70_000L,
            70_000L,
            true,
            new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA"},
            new String[] {"TLSv1.1", "TLSv1.2"},
            "clientkeypair",
            "clientkeystore",
            "classpath:clientkeystore.jks",
            "clientkeystore",
            "jks",
            "SUN",
            "classpath:clienttruststore.jks",
            "clienttruststore",
            "jks",
            "SUN",
            "TLS",
            true,
            false);
  }
}
