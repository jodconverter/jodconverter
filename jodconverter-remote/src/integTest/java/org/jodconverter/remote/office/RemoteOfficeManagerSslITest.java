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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.remote.RemoteConverter;
import org.jodconverter.remote.ssl.SslConfig;

/** Contains tests for the {@link SslConfig} class. */
class RemoteOfficeManagerSslITest {

  private static final String RESOURCES_PATH = "src/integTest/resources/";
  private static final String SOURCE_FILE_PATH = RESOURCES_PATH + "documents/test1.doc";

  private static final String CLIENT_KEYSTORE_PATH = RESOURCES_PATH + "clientkeystore.jks";
  private static final String CLIENT_KEYSTORE_PWD = "clientkeystore";

  private static final String CLIENT_KEYSTOREKEYPWD_PATH =
      RESOURCES_PATH + "clientkeystore_key.jks";
  private static final String CLIENT_KEYSTOREKEYPWD_PWD = "clientkeystore";
  private static final String CLIENT_KEYSTOREKEYPWD_KEY_PWD = "clientkeystorekey";

  private static final String CLIENT_TRUSTSTORE_PATH = RESOURCES_PATH + "clienttruststore.jks";
  private static final String CLIENT_TRUSTSTORE_PWD = "clienttruststore";

  private static final String SERVER_KEYSTORE_PATH = RESOURCES_PATH + "serverkeystore.jks";
  private static final String SERVER_KEYSTORE_PWD = "serverkeystore";

  private static final String SERVER_TRUSTSTORE_PATH = RESOURCES_PATH + "servertruststore.jks";
  private static final String SERVER_TRUSTSTORE_PWD = "servertruststore";

  @Nested
  class Execute {

    @Test
    void withKeyPasswordAndPasswordNotProvided_ShouldThrowUnrecoverableKeyException(
        final @TempDir File testFolder) throws OfficeException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD)
                  .trustStorePath(SERVER_TRUSTSTORE_PATH)
                  .trustStorePassword(SERVER_TRUSTSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setKeyStore(CLIENT_KEYSTOREKEYPWD_PATH);
        sslConfig.setKeyStorePassword(CLIENT_KEYSTOREKEYPWD_PWD);
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool/convert-to/")
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          assertThatExceptionOfType(OfficeException.class)
              .isThrownBy(
                  () -> RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute())
              .withCauseExactlyInstanceOf(UnrecoverableKeyException.class);

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {
        wireMockServer.stop();
      }
    }

    @Test
    void withKeyPasswordAndPasswordProvided_ShouldSucceed(final @TempDir File testFolder)
        throws Exception {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD)
                  .trustStorePath(SERVER_TRUSTSTORE_PATH)
                  .trustStorePassword(SERVER_TRUSTSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setKeyStore(CLIENT_KEYSTOREKEYPWD_PATH);
        sslConfig.setKeyStorePassword(CLIENT_KEYSTOREKEYPWD_PWD);
        sslConfig.setKeyPassword(CLIENT_KEYSTOREKEYPWD_KEY_PWD);
        sslConfig.setKeyStoreType("jks");
        sslConfig.setKeyStoreProvider("SUN");
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        sslConfig.setTrustStoreType("jks");
        sslConfig.setTrustStoreProvider("SUN");
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001") // try
                // all
                // accepted
                // URL
                // paths...
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          // Try to converter the input document
          RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute();

          // Check that the output file was created with the expected content.
          final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
          assertThat(content).as("Check content: %s", content).contains("Test Document");
        } finally {
          manager.stop();
        }
      } finally {
        wireMockServer.stop();
      }
    }

    @Test
    void withNeedClientAuthAndConfiguredClientAuth_ShouldSucceed(final @TempDir File testFolder)
        throws Exception {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD)
                  .trustStorePath(SERVER_TRUSTSTORE_PATH)
                  .trustStorePassword(SERVER_TRUSTSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setKeyStore(CLIENT_KEYSTORE_PATH);
        sslConfig.setKeyStorePassword(CLIENT_KEYSTORE_PWD);
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/") // try
                // all
                // accepted
                // URL
                // paths...
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          // Try to converter the input document
          RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute();

          // Check that the output file was created with the expected content.
          final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
          assertThat(content).as("Check content: %s", content).contains("Test Document");
        } finally {
          manager.stop();
        }
      } finally {
        wireMockServer.stop();
      }
    }

    @Test
    void withNeedClientAuthAndMissingClientAuth_ShouldThrowSSLException(
        final @TempDir File testFolder) throws OfficeException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD)
                  .needClientAuth(true));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool/convert-to/")
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          assertThatExceptionOfType(OfficeException.class)
              .isThrownBy(
                  () -> RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute());

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {
        wireMockServer.stop();
      }
    }

    @Test
    void withSpecifiedPrivateKeyAndBadPrivateKeySpecified_ShouldThrowSSLException(
        final @TempDir File testFolder) throws OfficeException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD)
                  .trustStorePath(SERVER_TRUSTSTORE_PATH)
                  .trustStorePassword(SERVER_TRUSTSTORE_PWD)
                  .needClientAuth(true));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setKeyStore(CLIENT_KEYSTORE_PATH);
        sslConfig.setKeyStorePassword(CLIENT_KEYSTORE_PWD);
        sslConfig.setKeyAlias("foo");
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool/convert-to/")
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          assertThatExceptionOfType(OfficeException.class)
              .isThrownBy(
                  () -> RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute());

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {
        wireMockServer.stop();
      }
    }

    @Test
    void withSpecifiedPrivateKeyAndGoodPrivateKeySpecified_ShouldSucceed(
        final @TempDir File testFolder) throws Exception {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD)
                  .trustStorePath(SERVER_TRUSTSTORE_PATH)
                  .trustStorePassword(SERVER_TRUSTSTORE_PWD)
                  .needClientAuth(true));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setKeyStore(CLIENT_KEYSTORE_PATH);
        sslConfig.setKeyStorePassword(CLIENT_KEYSTORE_PWD);
        sslConfig.setKeyAlias("clientkeypair");
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool") // try
                // all
                // accepted
                // URL
                // paths...
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          // Try to converter the input document
          RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute();

          // Check that the output file was created with the expected content.
          final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
          assertThat(content).as("Check content: %s", content).contains("Test Document");
        } finally {
          manager.stop();
        }
      } finally {

        wireMockServer.stop();
      }
    }

    @Test
    void withSelfSignedCertificateAndNoSslConfiguration_ShouldThrowSSLException(
        final @TempDir File testFolder) throws OfficeException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD));
      wireMockServer.start();
      try {
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool/convert-to/")
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          assertThatExceptionOfType(OfficeException.class)
              .isThrownBy(
                  () -> RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute())
              .withCauseExactlyInstanceOf(SSLHandshakeException.class);

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {

        wireMockServer.stop();
      }
    }

    @Test
    void withSelfSignedCertificateAndSslConfiguration_ShouldSucceed(final @TempDir File testFolder)
        throws Exception {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool/") // try all accepted URL paths...
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          // Try to converter the input document
          RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute();

          // Check that the output file was created with the expected content.
          final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
          assertThat(content).as("Check content: %s", content).contains("Test Document");
        } finally {
          manager.stop();
        }
      } finally {

        wireMockServer.stop();
      }
    }

    @Test
    void withSelfSignedCertificateAndTrustAll_ShouldSucceed(final @TempDir File testFolder)
        throws Exception {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setTrustAll(true);
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool/") // try all accepted URL paths...
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          // Try to converter the input document
          RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute();

          // Check that the output file was created with the expected content.
          final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
          assertThat(content).as("Check content: %s", content).contains("Test Document");
        } finally {
          manager.stop();
        }
      } finally {

        wireMockServer.stop();
      }
    }

    @Test
    void withSelfSignedCertificateAndHostnameVerification_ShouldThrowSslException(
        final @TempDir File testFolder) throws OfficeException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool/convert-to/")
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          assertThatExceptionOfType(OfficeException.class)
              .isThrownBy(
                  () -> RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute())
              .withCauseExactlyInstanceOf(SSLPeerUnverifiedException.class);

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {

        wireMockServer.stop();
      }
    }

    @Test
    void withSelfSignedCertificateAndSslDisabled_ShouldThrowSSLException(
        final @TempDir File testFolder) throws OfficeException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool/convert-to/")
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          assertThatExceptionOfType(OfficeException.class)
              .isThrownBy(
                  () -> RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute())
              .withCauseExactlyInstanceOf(SSLHandshakeException.class);

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {

        wireMockServer.stop();
      }
    }

    @Test
    void withUnknownSslProtocol_ShouldThrowNoSuchAlgorithmException(final @TempDir File testFolder)
        throws OfficeException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setProtocol("UnknownProtocol");
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool/convert-to/")
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          assertThatExceptionOfType(OfficeException.class)
              .isThrownBy(
                  () -> RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute())
              .withCauseExactlyInstanceOf(NoSuchAlgorithmException.class);

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {

        wireMockServer.stop();
      }
    }

    @Test
    void withKnownSslProtocol_ShouldSucceed(final @TempDir File testFolder)
        throws OfficeException, IOException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setProtocol("TLSv1.2");
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection(
                    "https://localhost:8001/lool/convert-to") // try all accepted URL paths...
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          // Try to converter the input document
          RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute();

          // Check that the output file was created with the expected content.
          final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
          assertThat(content).as("Check content: %s", content).contains("Test Document");
        } finally {
          manager.stop();
        }
      } finally {

        wireMockServer.stop();
      }
    }

    @Test
    void withUnknownEnabledlProtocol_ShouldThrowNoSuchAlgorithmException(
        final @TempDir File testFolder) throws OfficeException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setEnabledProtocols(new String[] {"UnknownProtocol"});
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool/convert-to/")
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          assertThatExceptionOfType(OfficeException.class)
              .isThrownBy(
                  () -> RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute())
              .withCauseExactlyInstanceOf(IllegalArgumentException.class);

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {

        wireMockServer.stop();
      }
    }

    @Test
    void withKnownEnabledProtocol_ShouldSucceed(final @TempDir File testFolder)
        throws OfficeException, IOException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setEnabledProtocols(new String[] {"TLSv1.2"});
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection(
                    "https://localhost:8001/lool/convert-to") // try all accepted URL paths...
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          // Try to converter the input document
          RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute();

          // Check that the output file was created with the expected content.
          final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
          assertThat(content).as("Check content: %s", content).contains("Test Document");
        } finally {
          manager.stop();
        }
      } finally {

        wireMockServer.stop();
      }
    }

    @Test
    void withUnknownCipher_ShouldThrowNoSuchAlgorithmException(final @TempDir File testFolder)
        throws OfficeException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setCiphers(new String[] {"UnknownCipher"});
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("https://localhost:8001/lool/convert-to/")
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          assertThatExceptionOfType(OfficeException.class)
              .isThrownBy(
                  () -> RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute())
              .withCauseExactlyInstanceOf(IllegalArgumentException.class);

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {
        wireMockServer.stop();
      }
    }

    @Test
    void withKnownEnabledCipher_ShouldSucceed(final @TempDir File testFolder)
        throws OfficeException, IOException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer =
          new WireMockServer(
              options()
                  .port(8000)
                  .httpsPort(8001)
                  .keystorePath(SERVER_KEYSTORE_PATH)
                  .keystorePassword(SERVER_KEYSTORE_PWD)
                  .keyManagerPassword(SERVER_KEYSTORE_PWD));
      wireMockServer.start();
      try {
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setEnabled(true);
        sslConfig.setCiphers(new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA"});
        sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
        sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
        sslConfig.setVerifyHostname(false);
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection(
                    "https://localhost:8001/lool/convert-to/") // try all accepted URL paths...
                .sslConfig(sslConfig)
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withBody("Test Document")));

          // Try to converter the input document
          RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute();

          // Check that the output file was created with the expected content.
          final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
          assertThat(content).as("Check content: %s", content).contains("Test Document");
        } finally {
          manager.stop();
        }
      } finally {
        wireMockServer.stop();
      }
    }
  }
}
