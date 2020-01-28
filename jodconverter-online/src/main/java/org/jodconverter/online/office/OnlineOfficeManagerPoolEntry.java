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

package org.jodconverter.online.office;

import static java.lang.Math.toIntExact;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import org.jodconverter.core.office.AbstractOfficeManagerPoolEntry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.online.ssl.SslConfig;

/**
 * A OnlineOfficeManagerPoolEntry is responsible to execute tasks submitted through a {@link
 * org.jodconverter.online.office.OnlineOfficeManager} that does not depend on an office
 * installation. It will send conversion request to a LibreOffice Online server and wait until the
 * task is done or a configured task execution timeout is reached.
 *
 * @see org.jodconverter.online.office.OnlineOfficeManager
 */
class OnlineOfficeManagerPoolEntry extends AbstractOfficeManagerPoolEntry {

  private final String connectionUrl;
  private final SslConfig sslConfig;

  private static final class SelectByAlias implements PrivateKeyStrategy {

    private final String keyAlias;

    @Override
    public String chooseAlias(final Map<String, PrivateKeyDetails> aliases, final Socket socket) {

      return aliases.keySet().stream()
          .filter(key -> StringUtils.equalsIgnoreCase(key, keyAlias))
          .findFirst()
          .orElse(null);
    }

    public SelectByAlias(final String keyAlias) {
      this.keyAlias = keyAlias;
    }
  }

  private static final class TrustAllStrategy implements TrustStrategy {

    private static final TrustAllStrategy INSTANCE = new TrustAllStrategy();

    @Override
    public boolean isTrusted(final X509Certificate[] chain, final String authType) {
      return true;
    }
  }

  // Taken from Spring org.springframework.util.ClassUtils class.
  private static ClassLoader getDefaultClassLoader() {

    ClassLoader cl = null;
    try {
      cl = Thread.currentThread().getContextClassLoader();
    } catch (Throwable ex) {
      // Cannot access thread context ClassLoader - falling back...
    }
    if (cl == null) {
      // No thread context class loader -> use class loader of this class.
      cl = OnlineOfficeManagerPoolEntry.class.getClassLoader();
      if (cl == null) {
        // getClassLoader() returning null indicates the bootstrap ClassLoader
        try {
          cl = ClassLoader.getSystemClassLoader();
        } catch (Throwable ex) {
          // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
        }
      }
    }
    return cl;
  }

  // Taken from spring org.springframework.util.ResourceUtils class
  private static File getFile(final URL url) {

    try {
      return new File(
          new URI(StringUtils.replace(url.toString(), " ", "%20")).getSchemeSpecificPart());
    } catch (URISyntaxException ex) {
      // Fallback for URLs that are not valid URIs (should hardly ever happen).
      return new File(url.getFile());
    }
  }

  // Taken from spring org.springframework.util.ResourceUtils class
  private static File getFile(final String resourceLocation) throws FileNotFoundException {

    Validate.notNull(resourceLocation, "Resource location must not be null");
    if (resourceLocation.startsWith("classpath:")) {
      final String path = resourceLocation.substring("classpath:".length());
      final String description = "class path resource [" + path + "]";
      final ClassLoader cl = getDefaultClassLoader();
      final URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
      if (url == null) {
        throw new FileNotFoundException(
            description + " cannot be resolved to absolute file path because it does not exist");
      }
      return getFile(url.toString());
    }

    try {
      // try URL
      return getFile(new URL(resourceLocation));
    } catch (MalformedURLException ex) {
      // no URL -> treat as file path
      return new File(resourceLocation);
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

    if (sslConfig.isTrustAll()) {
      sslBuilder.loadTrustMaterial(null, TrustAllStrategy.INSTANCE);
    } else {
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
  }

  @Override
  protected void doExecute(final OfficeTask task) throws OfficeException {

    final SSLConnectionSocketFactory sslFactory = configureSsl();
    try (CloseableHttpClient httpClient =
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
  protected void doStart() {

    taskExecutor.setAvailable(true);
  }

  @Override
  protected void doStop() {
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

      try (FileInputStream instream = new FileInputStream(getFile(store))) {
        keyStore.load(instream, storePassword.toCharArray());
      }

      return keyStore;
    }
    return null;
  }
}
