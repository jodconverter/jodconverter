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

package org.jodconverter.remote.ssl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link SslContexts} class. */
class SslContextsTest {

  private static final String RESOURCES_PATH = "src/integTest/resources/";
  private static final File CLIENT_KEYSTORE = new File(RESOURCES_PATH + "clientkeystore.jks");
  private static final File CLIENT_TRUSTSTORE = new File(RESOURCES_PATH + "clienttruststore.jks");
  private static final String KEYSTORE_PASSWORD = "clientkeystore";
  private static final String TRUSTSTORE_PASSWORD = "clienttruststore";

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(SslContexts.class);
  }

  @Nested
  class Create {

    @Test
    void withDefaults_ShouldUseTlsAndTheTrustOfTheJvm() throws Exception {

      final var context = SslContexts.create(new SslConfig());

      assertThat(context.getProtocol()).isEqualTo("TLS");
    }

    @Test
    void withTrustAll_ShouldCreateAContext() throws Exception {

      final var config = new SslConfig();
      config.setTrustAll(true);
      config.setProtocol("TLSv1.3");

      assertThat(SslContexts.create(config).getProtocol()).isEqualTo("TLSv1.3");
    }

    @Test
    void withoutHostnameVerificationNorTrustStore_ShouldWrapTheTrustOfTheJvm() throws Exception {

      final var config = new SslConfig();
      config.setVerifyHostname(false);

      assertThat(SslContexts.create(config)).isNotNull();
    }

    @Test
    void withStoresAsFileUrls_ShouldLoadThem() throws Exception {

      final var config = new SslConfig();
      config.setKeyStore(CLIENT_KEYSTORE.toURI().toURL().toString());
      config.setKeyStorePassword(KEYSTORE_PASSWORD);
      config.setKeyAlias("clientkeypair");
      config.setTrustStore(CLIENT_TRUSTSTORE.toURI().toURL().toString());
      config.setTrustStorePassword(TRUSTSTORE_PASSWORD);
      config.setVerifyHostname(false);

      assertThat(SslContexts.create(config)).isNotNull();
    }

    @Test
    void withMissingClasspathStore_ShouldThrowFileNotFoundException() {

      final var config = new SslConfig();
      config.setTrustStore("classpath:/does-not-exist.jks");
      config.setTrustStorePassword(TRUSTSTORE_PASSWORD);

      assertThatThrownBy(() -> SslContexts.create(config))
          .isInstanceOf(FileNotFoundException.class)
          .hasMessageContaining("does-not-exist.jks");
    }

    @Test
    void withUnknownProtocol_ShouldThrowNoSuchAlgorithmException() {

      final var config = new SslConfig();
      config.setProtocol("UNKNOWN");

      assertThatThrownBy(() -> SslContexts.create(config))
          .isInstanceOf(NoSuchAlgorithmException.class);
    }
  }

  @Nested
  class Parameters {

    @Test
    void withSupportedValues_ShouldSetThem() throws Exception {

      final var context = SslContexts.create(new SslConfig());
      final var cipher = context.getSupportedSSLParameters().getCipherSuites()[0];
      final var config = new SslConfig();
      config.setEnabledProtocols(new String[] {"TLSv1.2"});
      config.setCiphers(new String[] {cipher});

      final var parameters = SslContexts.parameters(config, context);

      assertThat(parameters.getProtocols()).containsExactly("TLSv1.2");
      assertThat(parameters.getCipherSuites()).containsExactly(cipher);
    }

    @Test
    void withoutValues_ShouldKeepTheDefaults() throws Exception {

      final var context = SslContexts.create(new SslConfig());

      final var parameters = SslContexts.parameters(new SslConfig(), context);

      assertThat(parameters.getProtocols())
          .containsExactly(context.getDefaultSSLParameters().getProtocols());
    }

    @Test
    void withUnsupportedValues_ShouldThrowIllegalArgumentException() throws Exception {

      final var context = SslContexts.create(new SslConfig());
      final var protocol = new SslConfig();
      protocol.setEnabledProtocols(new String[] {"SSLv1"});
      final var cipher = new SslConfig();
      cipher.setCiphers(new String[] {"TLS_NOT_A_CIPHER"});

      assertThatIllegalArgumentException()
          .isThrownBy(() -> SslContexts.parameters(protocol, context))
          .withMessage("Unsupported protocol: SSLv1");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> SslContexts.parameters(cipher, context))
          .withMessage("Unsupported cipher suite: TLS_NOT_A_CIPHER");
    }
  }
}
