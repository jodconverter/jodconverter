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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Builds the {@link SSLContext} and the {@link SSLParameters} described by an {@link SslConfig}.
 * The key store and the trust store are read once, from the class path ({@code classpath:}), a URL
 * ({@code file:}...) or a file path.
 */
public final class SslContexts {

  private static final String CLASSPATH_PREFIX = "classpath:";

  // Suppresses default constructor, ensuring non-instantiability.
  private SslContexts() {
    throw new AssertionError("Utility class must not be instantiated");
  }

  /**
   * Creates the SSL context described by the given configuration: its protocol, its key store (for
   * the client authentication) and its trust store, or every certificate trusted when {@code
   * trustAll} is set. When the host name verification is disabled, the trust managers skip it.
   *
   * @param config The SSL configuration.
   * @return The SSL context.
   * @throws GeneralSecurityException If the protocol is unknown, or if a store or a key cannot be
   *     used (an {@link java.security.UnrecoverableKeyException} for a wrong key password).
   * @throws IOException If a store cannot be read.
   */
  public static @NonNull SSLContext create(final @NonNull SslConfig config)
      throws GeneralSecurityException, IOException {

    Objects.requireNonNull(config, "config must not be null");
    final var context =
        SSLContext.getInstance(config.getProtocol() == null ? "TLS" : config.getProtocol());
    context.init(keyManagers(config), trustManagers(config), null);
    return context;
  }

  /**
   * Creates the SSL parameters described by the given configuration: the enabled protocols and the
   * cipher suites, when set, which must be supported by the context.
   *
   * @param config The SSL configuration.
   * @param context The SSL context the parameters are for.
   * @return The parameters, those of the context by default.
   * @throws IllegalArgumentException If a protocol or a cipher suite is not supported.
   */
  public static @NonNull SSLParameters parameters(
      final @NonNull SslConfig config, final @NonNull SSLContext context) {

    final var parameters = context.getDefaultSSLParameters();
    final var supported = context.getSupportedSSLParameters();
    if (config.getEnabledProtocols() != null) {
      parameters.setProtocols(
          supported(config.getEnabledProtocols(), supported.getProtocols(), "protocol"));
    }
    if (config.getCiphers() != null) {
      parameters.setCipherSuites(
          supported(config.getCiphers(), supported.getCipherSuites(), "cipher suite"));
    }
    return parameters;
  }

  private static String[] supported(
      final String[] values, final String[] supportedValues, final String kind) {

    final var supported = Set.of(supportedValues);
    for (final var value : values) {
      if (!supported.contains(value)) {
        throw new IllegalArgumentException("Unsupported " + kind + ": " + value);
      }
    }
    return Arrays.copyOf(values, values.length);
  }

  private static KeyManager @Nullable [] keyManagers(final SslConfig config)
      throws GeneralSecurityException, IOException {

    final var keyStore =
        loadStore(
            config.getKeyStore(),
            config.getKeyStorePassword(),
            config.getKeyStoreType(),
            config.getKeyStoreProvider());
    if (keyStore == null) {
      return null;
    }
    final var keyPassword =
        config.getKeyPassword() == null ? config.getKeyStorePassword() : config.getKeyPassword();
    final var factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    factory.init(keyStore, Objects.requireNonNull(keyPassword).toCharArray());
    final var managers = factory.getKeyManagers();
    if (config.getKeyAlias() == null) {
      return managers;
    }
    for (var i = 0; i < managers.length; i++) {
      if (managers[i] instanceof X509KeyManager keyManager) {
        managers[i] = new AliasKeyManager(keyManager, config.getKeyAlias());
      }
    }
    return managers;
  }

  private static TrustManager @Nullable [] trustManagers(final SslConfig config)
      throws GeneralSecurityException, IOException {

    final TrustManager[] managers;
    if (config.isTrustAll()) {
      managers = new TrustManager[] {TrustAllManager.INSTANCE};
    } else {
      final var trustStore =
          loadStore(
              config.getTrustStore(),
              config.getTrustStorePassword(),
              config.getTrustStoreType(),
              config.getTrustStoreProvider());
      if (trustStore == null) {
        if (config.isVerifyHostname()) {
          return null; // The default trust managers of the JVM
        }
        final var factory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        managers = factory.getTrustManagers();
      } else {
        final var factory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(trustStore);
        managers = factory.getTrustManagers();
      }
    }
    if (config.isVerifyHostname()) {
      return managers;
    }
    // The JVM verifies the host name inside the extended trust managers, when the connection
    // asks for it: a manager that checks the chain without the connection skips it.
    for (var i = 0; i < managers.length; i++) {
      if (managers[i] instanceof X509TrustManager trustManager) {
        managers[i] = new NoHostnameVerificationTrustManager(trustManager);
      }
    }
    return managers;
  }

  private static @Nullable KeyStore loadStore(
      final @Nullable String store,
      final @Nullable String password,
      final @Nullable String type,
      final @Nullable String provider)
      throws GeneralSecurityException, IOException {

    if (store == null) {
      return null;
    }
    Objects.requireNonNull(password, "The password of the store " + store + " must not be null");
    final var storeType = type == null ? KeyStore.getDefaultType() : type;
    final var keyStore =
        provider == null
            ? KeyStore.getInstance(storeType)
            : KeyStore.getInstance(storeType, provider);
    try (var input = open(store)) {
      keyStore.load(input, password.toCharArray());
    }
    return keyStore;
  }

  // Opens a store given as a class path resource, a URL or a file path.
  private static InputStream open(final String store) throws IOException {

    if (store.startsWith(CLASSPATH_PREFIX)) {
      final var path = store.substring(CLASSPATH_PREFIX.length());
      final var loader =
          Objects.requireNonNullElse(
              Thread.currentThread().getContextClassLoader(), SslContexts.class.getClassLoader());
      final var input = loader.getResourceAsStream(path.startsWith("/") ? path.substring(1) : path);
      if (input == null) {
        throw new FileNotFoundException("The class path resource " + path + " does not exist");
      }
      return input;
    }
    try {
      return new URL(store).openStream();
    } catch (MalformedURLException ex) {
      return Files.newInputStream(Path.of(store));
    }
  }

  /** A key manager that always chooses the key of a given alias for the client authentication. */
  private static final class AliasKeyManager extends X509ExtendedKeyManager {

    private final X509KeyManager delegate;
    private final String alias;

    private AliasKeyManager(final X509KeyManager delegate, final String alias) {
      super();
      this.delegate = delegate;
      this.alias = alias;
    }

    private @Nullable String chosenAlias() {
      final var aliases = delegate.getClientAliases("RSA", null);
      final var all = aliases == null ? List.<String>of() : List.of(aliases);
      return all.stream()
          .filter(candidate -> candidate.equalsIgnoreCase(alias))
          .findFirst()
          .orElseGet(() -> delegate.getPrivateKey(alias) == null ? null : alias);
    }

    @Override
    public String chooseClientAlias(
        final String[] keyType, final Principal[] issuers, final Socket socket) {
      return chosenAlias();
    }

    @Override
    public String chooseEngineClientAlias(
        final String[] keyType, final Principal[] issuers, final SSLEngine engine) {
      return chosenAlias();
    }

    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
      return delegate.getClientAliases(keyType, issuers);
    }

    @Override
    public String chooseServerAlias(
        final String keyType, final Principal[] issuers, final Socket socket) {
      return delegate.chooseServerAlias(keyType, issuers, socket);
    }

    @Override
    public String[] getServerAliases(final String keyType, final Principal[] issuers) {
      return delegate.getServerAliases(keyType, issuers);
    }

    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
      return delegate.getCertificateChain(alias);
    }

    @Override
    public PrivateKey getPrivateKey(final String alias) {
      return delegate.getPrivateKey(alias);
    }
  }

  /** A trust manager that trusts every certificate. */
  private static final class TrustAllManager extends X509ExtendedTrustManager {

    private static final TrustAllManager INSTANCE = new TrustAllManager();

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
      // Trusted
    }

    @Override
    public void checkClientTrusted(
        final X509Certificate[] chain, final String authType, final Socket socket) {
      // Trusted
    }

    @Override
    public void checkClientTrusted(
        final X509Certificate[] chain, final String authType, final SSLEngine engine) {
      // Trusted
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
      // Trusted
    }

    @Override
    public void checkServerTrusted(
        final X509Certificate[] chain, final String authType, final Socket socket) {
      // Trusted
    }

    @Override
    public void checkServerTrusted(
        final X509Certificate[] chain, final String authType, final SSLEngine engine) {
      // Trusted
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }

  /** A trust manager that checks the certificate chain, but not the host name. */
  private static final class NoHostnameVerificationTrustManager extends X509ExtendedTrustManager {

    private final X509TrustManager delegate;

    private NoHostnameVerificationTrustManager(final X509TrustManager delegate) {
      super();
      this.delegate = delegate;
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType)
        throws java.security.cert.CertificateException {
      delegate.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkClientTrusted(
        final X509Certificate[] chain, final String authType, final Socket socket)
        throws java.security.cert.CertificateException {
      delegate.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkClientTrusted(
        final X509Certificate[] chain, final String authType, final SSLEngine engine)
        throws java.security.cert.CertificateException {
      delegate.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType)
        throws java.security.cert.CertificateException {
      delegate.checkServerTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(
        final X509Certificate[] chain, final String authType, final Socket socket)
        throws java.security.cert.CertificateException {
      delegate.checkServerTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(
        final X509Certificate[] chain, final String authType, final SSLEngine engine)
        throws java.security.cert.CertificateException {
      delegate.checkServerTrusted(chain, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return delegate.getAcceptedIssuers();
    }
  }
}
