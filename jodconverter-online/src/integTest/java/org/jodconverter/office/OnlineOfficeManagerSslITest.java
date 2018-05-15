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

package org.jodconverter.office;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.tomakehurst.wiremock.WireMockServer;

import org.jodconverter.OnlineConverter;
import org.jodconverter.ssl.SslConfig;

public class OnlineOfficeManagerSslITest {

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

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void execute_WithKeyPasswordAndPasswordNotProvided_ShouldThrowUnrecoverableKeyException() {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD)
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
          OnlineOfficeManager.builder()
              .urlConnection("https://localhost:8001/lool/convert-to/")
              .sslConfig(sslConfig)
              .build();
      try {
        manager.start();
        wireMockServer.stubFor(
            post(urlPathEqualTo("/lool/convert-to/txt"))
                .willReturn(aResponse().withBody("Test Document")));

        // Try to converter the input document
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Be sure the an exception is thrown.
        fail();

      } catch (Exception ex) {
        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasCauseExactlyInstanceOf(UnrecoverableKeyException.class);
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithKeyPasswordAndPasswordProvided_ShouldSucceed() throws Exception {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD)
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
          OnlineOfficeManager.builder()
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
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Check that the output file was created with the expected content.
        final String content = FileUtils.readFileToString(outputFile, Charset.forName("UTF-8"));
        assertThat(content).contains("Test Document");
      } finally {
        manager.stop();
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithNeedClientAuthAndConfiguredClientAuth_ShouldSucceed() throws Exception {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD)
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
          OnlineOfficeManager.builder()
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
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Check that the output file was created with the expected content.
        final String content = FileUtils.readFileToString(outputFile, Charset.forName("UTF-8"));
        assertThat(content).contains("Test Document");
      } finally {
        manager.stop();
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithNeedClientAuthAndMissingClientAuth_ShouldThrowSSLException() {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD)
                .needClientAuth(true));
    wireMockServer.start();
    try {
      final SslConfig sslConfig = new SslConfig();
      sslConfig.setEnabled(true);
      sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
      sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
      final OfficeManager manager =
          OnlineOfficeManager.builder()
              .urlConnection("https://localhost:8001/lool/convert-to/")
              .sslConfig(sslConfig)
              .build();
      try {
        manager.start();
        wireMockServer.stubFor(
            post(urlPathEqualTo("/lool/convert-to/txt"))
                .willReturn(aResponse().withBody("Test Document")));

        // Try to converter the input document
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Be sure the an exception is thrown.
        fail();

      } catch (Exception ex) {
        assertThat(ex).isExactlyInstanceOf(OfficeException.class);
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithSpecifiedPrivateKeyAndBadPrivateKeySpecified_ShouldThrowSSLException() {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD)
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
          OnlineOfficeManager.builder()
              .urlConnection("https://localhost:8001/lool/convert-to/")
              .sslConfig(sslConfig)
              .build();
      try {
        manager.start();
        wireMockServer.stubFor(
            post(urlPathEqualTo("/lool/convert-to/txt"))
                .willReturn(aResponse().withBody("Test Document")));

        // Try to converter the input document
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Be sure the an exception is thrown.
        fail();

      } catch (Exception ex) {
        assertThat(ex).isExactlyInstanceOf(OfficeException.class);
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithSpecifiedPrivateKeyAndGoodPrivateKeySpecified_ShouldSucceed()
      throws Exception {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD)
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
          OnlineOfficeManager.builder()
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
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Check that the output file was created with the expected content.
        final String content = FileUtils.readFileToString(outputFile, Charset.forName("UTF-8"));
        assertThat(content).contains("Test Document");
      } finally {
        manager.stop();
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithSelfSignedCertificateAndNoSslConfiguration_ShouldThrowSSLException() {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD));
    wireMockServer.start();
    try {
      final OfficeManager manager =
          OnlineOfficeManager.builder()
              .urlConnection("https://localhost:8001/lool/convert-to/")
              .build();
      try {
        manager.start();
        wireMockServer.stubFor(
            post(urlPathEqualTo("/lool/convert-to/txt"))
                .willReturn(aResponse().withBody("Test Document")));

        // Try to converter the input document
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Be sure the an exception is thrown.
        fail();

      } catch (Exception ex) {
        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasCauseExactlyInstanceOf(SSLHandshakeException.class);
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithSelfSignedCertificateAndSslConfiguration_ShouldSucceed()
      throws Exception {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD));
    wireMockServer.start();
    try {
      final SslConfig sslConfig = new SslConfig();
      sslConfig.setEnabled(true);
      sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
      sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
      sslConfig.setVerifyHostname(false);
      final OfficeManager manager =
          OnlineOfficeManager.builder()
              .urlConnection("https://localhost:8001/lool/") // try
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
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Check that the output file was created with the expected content.
        final String content = FileUtils.readFileToString(outputFile, Charset.forName("UTF-8"));
        assertThat(content).contains("Test Document");
      } finally {
        manager.stop();
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithSelfSignedCertificateAndHostnameVerification_ShouldThrowSslException() {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD));
    wireMockServer.start();
    try {
      final SslConfig sslConfig = new SslConfig();
      sslConfig.setEnabled(true);
      sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
      sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
      final OfficeManager manager =
          OnlineOfficeManager.builder()
              .urlConnection("https://localhost:8001/lool/convert-to/")
              .sslConfig(sslConfig)
              .build();
      try {
        manager.start();
        wireMockServer.stubFor(
            post(urlPathEqualTo("/lool/convert-to/txt"))
                .willReturn(aResponse().withBody("Test Document")));

        // Try to converter the input document
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Be sure the an exception is thrown.
        fail();

      } catch (Exception ex) {
        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasCauseExactlyInstanceOf(SSLPeerUnverifiedException.class);
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithSelfSignedCertificateAndSslDisabled_ShouldThrowSSLException() {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD));
    wireMockServer.start();
    try {
      final SslConfig sslConfig = new SslConfig();
      sslConfig.setEnabled(false);
      final OfficeManager manager =
          OnlineOfficeManager.builder()
              .urlConnection("https://localhost:8001/lool/convert-to/")
              .sslConfig(sslConfig)
              .build();
      try {
        manager.start();
        wireMockServer.stubFor(
            post(urlPathEqualTo("/lool/convert-to/txt"))
                .willReturn(aResponse().withBody("Test Document")));

        // Try to converter the input document
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Be sure the an exception is thrown.
        fail();

      } catch (Exception ex) {
        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasCauseExactlyInstanceOf(SSLHandshakeException.class);
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithUnknownSslProtocol_ShouldThrowNoSuchAlgorithmException() {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD));
    wireMockServer.start();
    try {
      final SslConfig sslConfig = new SslConfig();
      sslConfig.setEnabled(true);
      sslConfig.setProtocol("UnknownProtocol");
      sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
      sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
      sslConfig.setVerifyHostname(false);
      final OfficeManager manager =
          OnlineOfficeManager.builder()
              .urlConnection("https://localhost:8001/lool/convert-to/")
              .sslConfig(sslConfig)
              .build();
      try {
        manager.start();
        wireMockServer.stubFor(
            post(urlPathEqualTo("/lool/convert-to/txt"))
                .willReturn(aResponse().withBody("Test Document")));

        // Try to converter the input document
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Be sure the an exception is thrown.
        fail();

      } catch (Exception ex) {
        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasCauseExactlyInstanceOf(NoSuchAlgorithmException.class);
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithKnownSslProtocol_ShouldSucceed() throws Exception {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD));
    wireMockServer.start();
    try {
      final SslConfig sslConfig = new SslConfig();
      sslConfig.setEnabled(true);
      sslConfig.setProtocol("TLSv1.2");
      sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
      sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
      sslConfig.setVerifyHostname(false);
      final OfficeManager manager =
          OnlineOfficeManager.builder()
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
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Check that the output file was created with the expected content.
        final String content = FileUtils.readFileToString(outputFile, Charset.forName("UTF-8"));
        assertThat(content).contains("Test Document");
      } finally {
        manager.stop();
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithUnknownEnabledlProtocol_ShouldThrowNoSuchAlgorithmException() {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD));
    wireMockServer.start();
    try {
      final SslConfig sslConfig = new SslConfig();
      sslConfig.setEnabled(true);
      sslConfig.setEnabledProtocols(new String[] {"UnknownProtocol"});
      sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
      sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
      sslConfig.setVerifyHostname(false);
      final OfficeManager manager =
          OnlineOfficeManager.builder()
              .urlConnection("https://localhost:8001/lool/convert-to/")
              .sslConfig(sslConfig)
              .build();
      try {
        manager.start();
        wireMockServer.stubFor(
            post(urlPathEqualTo("/lool/convert-to/txt"))
                .willReturn(aResponse().withBody("Test Document")));

        // Try to converter the input document
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Be sure the an exception is thrown.
        fail();

      } catch (Exception ex) {
        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasCauseExactlyInstanceOf(IllegalArgumentException.class);
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithKnownEnabledProtocol_ShouldSucceed() throws Exception {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD));
    wireMockServer.start();
    try {
      final SslConfig sslConfig = new SslConfig();
      sslConfig.setEnabled(true);
      sslConfig.setEnabledProtocols(new String[] {"TLSv1.2"});
      sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
      sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
      sslConfig.setVerifyHostname(false);
      final OfficeManager manager =
          OnlineOfficeManager.builder()
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
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Check that the output file was created with the expected content.
        final String content = FileUtils.readFileToString(outputFile, Charset.forName("UTF-8"));
        assertThat(content).contains("Test Document");
      } finally {
        manager.stop();
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithUnknownCipher_ShouldThrowNoSuchAlgorithmException() {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD));
    wireMockServer.start();
    try {
      final SslConfig sslConfig = new SslConfig();
      sslConfig.setEnabled(true);
      sslConfig.setCiphers(new String[] {"UnknownCipher"});
      sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
      sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
      sslConfig.setVerifyHostname(false);
      final OfficeManager manager =
          OnlineOfficeManager.builder()
              .urlConnection("https://localhost:8001/lool/convert-to/")
              .sslConfig(sslConfig)
              .build();
      try {
        manager.start();
        wireMockServer.stubFor(
            post(urlPathEqualTo("/lool/convert-to/txt"))
                .willReturn(aResponse().withBody("Test Document")));

        // Try to converter the input document
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Be sure the an exception is thrown.
        fail();

      } catch (Exception ex) {
        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasCauseExactlyInstanceOf(IllegalArgumentException.class);
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_WithKnownEnabledCipher_ShouldSucceed() throws Exception {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD));
    wireMockServer.start();
    try {
      final SslConfig sslConfig = new SslConfig();
      sslConfig.setEnabled(true);
      sslConfig.setCiphers(new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA"});
      sslConfig.setTrustStore(CLIENT_TRUSTSTORE_PATH);
      sslConfig.setTrustStorePassword(CLIENT_TRUSTSTORE_PWD);
      sslConfig.setVerifyHostname(false);
      final OfficeManager manager =
          OnlineOfficeManager.builder()
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
        OnlineConverter.make(manager).convert(inputFile).to(outputFile).execute();

        // Check that the output file was created with the expected content.
        final String content = FileUtils.readFileToString(outputFile, Charset.forName("UTF-8"));
        assertThat(content).contains("Test Document");
      } finally {
        manager.stop();
      }
    } finally {
      FileUtils.deleteQuietly(outputFile);
      wireMockServer.stop();
    }
  }
}
