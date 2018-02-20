/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import org.jodconverter.boot.autoconfigure.JodConverterLocalProperties;
import org.jodconverter.boot.autoconfigure.JodConverterOnlineProperties;
import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.OnlineOfficeManager;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:config/application-props.properties")
public class AutoConfigurationPropertiesTest {

  // Provided valid OfficeManager beans so we will be able to test the Autowired properties.
  @TestConfiguration
  static class TestConfig {

    @Bean
    public OfficeManager localOfficeManager() {
      return LocalOfficeManager.make();
    }

    @Bean
    public OfficeManager onlineOfficeManager() {
      return OnlineOfficeManager.make("some url");
    }
  }

  @Autowired private JodConverterLocalProperties localProps;

  @Autowired private JodConverterOnlineProperties onlineProps;

  @Test
  public void testLocalProperties() {

    assertThat(localProps)
        .extracting(
            "enabled",
            "officeHome",
            "portNumbers",
            "workingDir",
            "templateProfileDir",
            "killExistingProcess",
            "processTimeout",
            "processRetryInterval",
            "taskExecutionTimeout",
            "maxTasksPerProcess",
            "taskQueueTimeout")
        .containsExactly(
            true,
            "office-home",
            "2005",
            "working-dir",
            "template-profile-dir",
            false,
            180000L,
            1000L,
            60000L,
            20,
            60000L);
  }

  @Test
  public void testOnlineProperties() {

    assertThat(onlineProps)
        .extracting(
            "enabled",
            "url",
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
            "ssl.verifyHostname")
        .containsExactly(
            true,
            "https://localhost:8001",
            "working-dir",
            1,
            60000L,
            60000L,
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
            false);
  }
}
