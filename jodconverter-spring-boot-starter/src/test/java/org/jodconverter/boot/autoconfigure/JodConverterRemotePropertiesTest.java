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

import org.junit.jupiter.api.Test;

import org.jodconverter.remote.office.RemoteOfficeManager;
import org.jodconverter.remote.ssl.SslConfig;

/** Contains tests for the {@link JodConverterRemoteProperties} class. */
class JodConverterRemotePropertiesTest {

  @Test
  void sslConfig_ShouldCopyEveryProperty() {

    final var ssl = new JodConverterRemoteProperties.SslProperties();
    ssl.setEnabled(true);
    ssl.setCiphers(new String[] {"cipher"});
    ssl.setEnabledProtocols(new String[] {"TLSv1.3"});
    ssl.setKeyAlias("alias");
    ssl.setKeyPassword("keypassword");
    ssl.setKeyStore("keystore");
    ssl.setKeyStorePassword("keystorepassword");
    ssl.setKeyStoreType("jks");
    ssl.setKeyStoreProvider("SUN");
    ssl.setTrustStore("truststore");
    ssl.setTrustStorePassword("truststorepassword");
    ssl.setTrustStoreType("pkcs12");
    ssl.setTrustStoreProvider("SunJSSE");
    ssl.setProtocol("TLS");
    ssl.setTrustAll(true);
    ssl.setVerifyHostname(false);

    final var config = ssl.sslConfig();

    assertThat(config)
        .usingRecursiveComparison()
        .isEqualTo(
            new SslConfig() {
              {
                setEnabled(true);
                setCiphers(new String[] {"cipher"});
                setEnabledProtocols(new String[] {"TLSv1.3"});
                setKeyAlias("alias");
                setKeyPassword("keypassword");
                setKeyStore("keystore");
                setKeyStorePassword("keystorepassword");
                setKeyStoreType("jks");
                setKeyStoreProvider("SUN");
                setTrustStore("truststore");
                setTrustStorePassword("truststorepassword");
                setTrustStoreType("pkcs12");
                setTrustStoreProvider("SunJSSE");
                setProtocol("TLS");
                setTrustAll(true);
                setVerifyHostname(false);
              }
            });
  }

  @Test
  void defaults_ShouldMatchTheManagerAndTheSslConfig() {

    final var properties = new JodConverterRemoteProperties();

    assertThat(properties.getConnectTimeout().toMillis())
        .isEqualTo(RemoteOfficeManager.DEFAULT_CONNECT_TIMEOUT);
    assertThat(properties.getSocketTimeout().toMillis())
        .isEqualTo(RemoteOfficeManager.DEFAULT_SOCKET_TIMEOUT);
    // Trusting every certificate must be an explicit choice.
    assertThat(new JodConverterRemoteProperties.SslProperties().isTrustAll())
        .isEqualTo(new SslConfig().isTrustAll())
        .isFalse();
  }
}
