/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

import static java.lang.Math.toIntExact;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import org.jodconverter.ssl.SslConfig;
import org.jodconverter.task.OfficeTask;

/**
 * A OnlineOfficeManagerPoolEntry is responsible to execute tasks submitted through a {@link
 * OnlineOfficeManager} that does not depend on an office installation. It will send conversion
 * request to a LibreOffice Online server and wait until the task is done or a configured task
 * execution timeout is reached.
 *
 * @see OnlineOfficeManager
 */
class OnlineOfficeManagerPoolEntry extends AbstractOfficeManagerPoolEntry {

  private final String connectionUrl;
  private final SslConfig sslConfig;

  private static final class SelectByAlias implements PrivateKeyStrategy {

    private final String keyAlias;

    @Override
    public String chooseAlias(final Map<String, PrivateKeyDetails> aliases, final Socket socket) {

      return aliases
          .keySet()
          .stream()
          .filter(key -> StringUtils.equalsIgnoreCase(key, keyAlias))
          .findFirst()
          .orElse(null);
    }

    public SelectByAlias(final String keyAlias) {
      this.keyAlias = keyAlias;
    }
  }

  /**
   * Creates a new pool entry with the specified configuration.
   *
   * @param connectionUrl The URL to the LibreOffice Online server.
   * @param sslConfig The SSL configuration used to secure communication with LibreOffice Online
   *     server.
   * @param config The entry configuration.
   */
  public OnlineOfficeManagerPoolEntry(
      final String connectionUrl,
      final SslConfig sslConfig,
      final OnlineOfficeManagerPoolEntryConfig config) {
    super(config);

    this.connectionUrl = connectionUrl;
    this.sslConfig = sslConfig;
  }

  private String buildUrl(final String connectionUrl) throws MalformedURLException {

    // An example URL is like:
    // http://localhost:9980/lool/convert-to/docx

    final URL url = new URL(connectionUrl);
    final String path = url.toExternalForm().toLowerCase();
    if (StringUtils.endsWithAny(path, "lool/convert-to", "lool/convert-to/")) {
      return StringUtils.appendIfMissing(connectionUrl, "/");
    } else if (StringUtils.endsWithAny(path, "lool", "lool/")) {
      return StringUtils.appendIfMissing(connectionUrl, "/") + "convert-to/";
    }
    return StringUtils.appendIfMissing(connectionUrl, "/") + "lool/convert-to/";
  }

  private void configureKeyMaterial(final SSLContextBuilder sslBuilder)
      throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
          CertificateException, IOException, NoSuchProviderException {

    final KeyStore keystore =
        loadStore(
            sslConfig.getKeyStore(),
            sslConfig.getKeyStorePassword(),
            sslConfig.getKeyStoreType(),
            sslConfig.getKeyStoreProvider());
    if (keystore != null) {
      sslBuilder.loadKeyMaterial(
          keystore,
          sslConfig.getKeyPassword() != null
              ? sslConfig.getKeyPassword().toCharArray()
              : sslConfig.getKeyStorePassword().toCharArray(),
          sslConfig.getKeyAlias() == null ? null : new SelectByAlias(sslConfig.getKeyAlias()));
    }
  }

  private SSLConnectionSocketFactory configureSsl() throws OfficeException {

    if (sslConfig == null || !sslConfig.isEnabled()) {
      return null;
    }

    try {
      final SSLContextBuilder sslBuilder = SSLContexts.custom();
      sslBuilder.setProtocol(sslConfig.getProtocol());
      configureKeyMaterial(sslBuilder);
      configureTrustMaterial(sslBuilder);

      final SSLContext sslcontext = sslBuilder.build();

      return new SSLConnectionSocketFactory(
          sslcontext,
          sslConfig.getEnabledProtocols(),
          sslConfig.getCiphers(),
          sslConfig.isVerifyHostname()
              ? SSLConnectionSocketFactory.getDefaultHostnameVerifier()
              : NoopHostnameVerifier.INSTANCE);

    } catch (IOException
        | KeyManagementException
        | NoSuchAlgorithmException
        | KeyStoreException
        | CertificateException
        | UnrecoverableKeyException
        | NoSuchProviderException ex) {
      throw new OfficeException("Unable to create SSL context.", ex);
    }
  }

  private void configureTrustMaterial(final SSLContextBuilder sslBuilder)
      throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException,
          NoSuchProviderException {

    final KeyStore truststore =
        loadStore(
            sslConfig.getTrustStore(),
            sslConfig.getTrustStorePassword(),
            sslConfig.getTrustStoreType(),
            sslConfig.getTrustStoreProvider());
    if (truststore != null) {
      sslBuilder.loadTrustMaterial(truststore, null);
    }
  }

  @Override
  protected void doExecute(final OfficeTask task) throws OfficeException {

    final SSLConnectionSocketFactory sslFactory = configureSsl();
    try (final CloseableHttpClient httpClient =
        HttpClients.custom().setSSLSocketFactory(sslFactory).build()) {

      // Use the task execution timeout as connection and socket timeout.
      // TODO: Should the user be able to customize connection and socket timeout ?
      final RequestConfig requestConfig =
          new RequestConfig(
              buildUrl(connectionUrl),
              toIntExact(config.getTaskExecutionTimeout()),
              toIntExact(config.getTaskExecutionTimeout()));
      task.execute(new OnlineOfficeConnection(httpClient, requestConfig));

    } catch (IOException ex) {
      throw new OfficeException("Unable to create the HTTP client", ex);
    }
  }

  @Override
  protected void doStart() throws OfficeException {

    taskExecutor.setAvailable(true);
  }

  @Override
  protected void doStop() throws OfficeException {
    // Nothing to stop here.
  }

  private KeyStore loadStore(
      final String store,
      final String storePassword,
      final String storeType,
      final String storeProvider)
      throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException,
          NoSuchProviderException {

    if (store != null) {
      Validate.notNull(storePassword, "The password of store {0} must not be null", store);

      KeyStore keyStore;

      final String type = storeType == null ? KeyStore.getDefaultType() : storeType;
      if (storeProvider == null) {
        keyStore = KeyStore.getInstance(type);
      } else {
        keyStore = KeyStore.getInstance(type, storeProvider);
      }

      try (FileInputStream instream = new FileInputStream(new File(store))) {
        keyStore.load(instream, storePassword.toCharArray());
      }

      return keyStore;
    }
    return null;
  }
}
