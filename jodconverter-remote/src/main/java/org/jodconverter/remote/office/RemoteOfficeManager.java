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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.stream.IntStream;
import javax.net.ssl.SSLContext;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.office.AbstractOfficeWorkerPool;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.remote.ssl.SslConfig;

/**
 * Office manager that sends the conversion tasks to a LibreOffice Online server, through its {@code
 * convert-to} service. It does not depend on an office installation: its workers are HTTP clients,
 * built once with the SSL material of the manager when it starts.
 *
 * @see org.jodconverter.core.office.OfficeManager
 */
public final class RemoteOfficeManager extends AbstractOfficeWorkerPool {

  /** The default pool size. */
  public static final int DEFAULT_POOL_SIZE = 1;

  /** The maximum size of the pool. */
  public static final int MAX_POOL_SIZE = 1000;

  /** The default connect timeout, in milliseconds (1 minute). */
  public static final long DEFAULT_CONNECT_TIMEOUT = 60_000L;

  /** The default socket timeout, in milliseconds (2 minutes). */
  public static final long DEFAULT_SOCKET_TIMEOUT = 120_000L;

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link RemoteOfficeManager} with default configuration.
   *
   * @param urlConnection The URL to the LibreOffice Online server.
   * @return A {@link RemoteOfficeManager} with default configuration.
   */
  public static @NonNull RemoteOfficeManager make(final @NonNull String urlConnection) {
    return builder().urlConnection(urlConnection).build();
  }

  /**
   * Creates a new {@link RemoteOfficeManager} with default configuration. The created manager will
   * then be the unique instance of the {@link
   * org.jodconverter.core.office.InstalledOfficeManagerHolder} class. Note that if the {@code
   * InstalledOfficeManagerHolder} class already holds an {@code OfficeManager} instance, the owner
   * of this existing manager is responsible to stopped it.
   *
   * @param urlConnection The URL to the LibreOffice Online server.
   * @return A {@link RemoteOfficeManager} with default configuration.
   */
  public static @NonNull RemoteOfficeManager install(final @NonNull String urlConnection) {
    return builder().urlConnection(urlConnection).install().build();
  }

  /**
   * Builds the URL of the conversion service from the URL given to the manager: the URL of the
   * server, of its {@code lool} or {@code cool} directory, or of the {@code convert-to} service
   * itself, with or without a trailing slash.
   *
   * @param urlConnection The URL given to the manager.
   * @return The URL of the conversion service, ending with a slash.
   * @throws IllegalArgumentException If the URL is not valid.
   */
  /* default */ static @NonNull String toServiceUrl(final @NonNull String urlConnection) {

    try {
      new URL(urlConnection).toURI();
    } catch (MalformedURLException | java.net.URISyntaxException ex) {
      throw new IllegalArgumentException(
          "urlConnection '" + urlConnection + "' is not a valid URL", ex);
    }
    final var base = urlConnection.endsWith("/") ? urlConnection : urlConnection + "/";
    final var path = base.toLowerCase(Locale.ROOT);
    if (path.endsWith("/convert-to/")) {
      return base;
    }
    if (path.endsWith("/lool/") || path.endsWith("/cool/")) {
      return base + "convert-to/";
    }
    return base + "lool/convert-to/";
  }

  private RemoteOfficeManager(
      final int poolSize,
      final File workingDir,
      final RequestConfig requestConfig,
      final SslConfig sslConfig,
      final SSLContext sslContext,
      final long taskExecutionTimeout,
      final long taskQueueTimeout,
      final int taskQueueCapacity) {
    super(workingDir, taskQueueTimeout, taskExecutionTimeout, taskQueueCapacity, true);

    setWorkers(
        IntStream.range(0, poolSize)
            .mapToObj(i -> new RemoteOfficeWorker(requestConfig, sslConfig, sslContext))
            .toList());
  }

  /**
   * A builder for constructing a {@link RemoteOfficeManager}.
   *
   * @see RemoteOfficeManager
   */
  public static final class Builder extends AbstractOfficeWorkerPoolBuilder<Builder> {

    private int poolSize = DEFAULT_POOL_SIZE;
    private String urlConnection;
    private SslConfig sslConfig;
    private SSLContext sslContext;
    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private long socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    // Private constructor so only RemoteOfficeManager can initialize an instance of this builder.
    private Builder() {
      super();
    }

    /**
     * Creates the manager that is specified by this builder.
     *
     * @return The manager that is specified by this builder.
     * @throws IllegalArgumentException If the URL of the server is missing or not valid.
     */
    @Override
    public @NonNull RemoteOfficeManager build() {

      AssertUtils.notBlank(urlConnection, "urlConnection must not be null nor blank");

      // Validate the working directory
      OfficeUtils.validateWorkingDir(workingDir);

      final var manager =
          new RemoteOfficeManager(
              poolSize,
              workingDir,
              new RequestConfig(toServiceUrl(urlConnection), connectTimeout, socketTimeout),
              sslConfig,
              sslContext,
              taskExecutionTimeout,
              taskQueueTimeout,
              taskQueueCapacity);
      return installed(manager);
    }

    /**
     * Specifies the pool size of the manager: the number of requests sent at the same time.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 1
     *
     * @param poolSize The pool size, between 1 and {@link #MAX_POOL_SIZE}.
     * @return This builder instance.
     */
    public @NonNull Builder poolSize(final int poolSize) {

      AssertUtils.isTrue(
          poolSize >= 1 && poolSize <= MAX_POOL_SIZE,
          String.format("poolSize %s must be between %d and %d", poolSize, 1, MAX_POOL_SIZE));
      this.poolSize = poolSize;
      return this;
    }

    /**
     * Specifies the URL of the LibreOffice Online server: the URL of the server, of its {@code
     * lool} or {@code cool} directory, or of the {@code convert-to} service itself.
     *
     * @param urlConnection The URL of the server.
     * @return This builder instance.
     */
    public @NonNull Builder urlConnection(final @Nullable String urlConnection) {

      this.urlConnection = urlConnection;
      return this;
    }

    /**
     * Specifies the SSL configuration used to secure the communication with the server. The SSL
     * material it describes is loaded once, when the manager starts. Ignored when an SSL context is
     * given with {@link #sslContext(SSLContext)}.
     *
     * @param sslConfig The SSL configuration, or null for the defaults of the JVM.
     * @return This builder instance.
     */
    public @NonNull Builder sslConfig(final @Nullable SslConfig sslConfig) {

      this.sslConfig = sslConfig;
      return this;
    }

    /**
     * Specifies the SSL context used to secure the communication with the server, for an
     * application that builds it itself (from a Spring Boot SSL bundle, for example). It takes
     * precedence over the SSL configuration.
     *
     * @param sslContext The SSL context, or null to use the SSL configuration.
     * @return This builder instance.
     */
    public @NonNull Builder sslContext(final @Nullable SSLContext sslContext) {

      this.sslContext = sslContext;
      return this;
    }

    /**
     * Specifies the timeout, in milliseconds, until a connection to the server is established. A
     * timeout of zero means no timeout.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 60000 (1 minute)
     *
     * @param connectTimeout The connect timeout, in milliseconds.
     * @return This builder instance.
     */
    public @NonNull Builder connectTimeout(final long connectTimeout) {

      AssertUtils.isTrue(
          connectTimeout >= 0,
          String.format("connectTimeout %s must be greater than or equal to 0", connectTimeout));
      this.connectTimeout = connectTimeout;
      return this;
    }

    /**
     * Specifies the timeout, in milliseconds, for the response of the server once a request is
     * sent. A timeout of zero means no timeout.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
     *
     * @param socketTimeout The response timeout, in milliseconds.
     * @return This builder instance.
     */
    public @NonNull Builder socketTimeout(final long socketTimeout) {

      AssertUtils.isTrue(
          socketTimeout >= 0,
          String.format("socketTimeout %s must be greater than or equal to 0", socketTimeout));
      this.socketTimeout = socketTimeout;
      return this;
    }
  }
}
