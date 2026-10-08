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

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.GeneralSecurityException;
import java.time.Duration;
import javax.net.ssl.SSLContext;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeWorker;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.remote.ssl.SslConfig;
import org.jodconverter.remote.ssl.SslContexts;

/**
 * A RemoteOfficeWorker executes the tasks submitted through a {@link RemoteOfficeManager}, which
 * does not depend on an office installation: it sends the conversion requests to a LibreOffice
 * Online server. Its HTTP client, with the SSL material of the manager, is built once when the
 * worker starts; the worker is then always ready.
 *
 * @see RemoteOfficeManager
 */
class RemoteOfficeWorker implements OfficeWorker {

  private final RequestConfig requestConfig;
  private final SslConfig sslConfig;
  private final SSLContext sslContext;
  private HttpClient httpClient;
  // The context of the task being executed, aborted to end its request.
  private volatile RemoteOfficeConnection current;

  /**
   * Creates a new worker with the specified configuration.
   *
   * @param requestConfig The configuration of the requests: the URL of the conversion service and
   *     the timeouts.
   * @param sslConfig The SSL configuration used to secure the communication with the server, or
   *     null for the defaults of the JVM; ignored when an SSL context is given.
   * @param sslContext The SSL context used to secure the communication with the server, or null to
   *     build it from the SSL configuration.
   */
  /* default */ RemoteOfficeWorker(
      final RequestConfig requestConfig,
      final @Nullable SslConfig sslConfig,
      final @Nullable SSLContext sslContext) {
    super();

    this.requestConfig = requestConfig;
    this.sslConfig = sslConfig;
    this.sslContext = sslContext;
  }

  /**
   * Builds the HTTP client of this worker, with the SSL material loaded once.
   *
   * @return The client.
   * @throws OfficeException If the SSL material cannot be used.
   */
  private HttpClient buildHttpClient() throws OfficeException {

    // HTTP/1.1, as the conversion services expect: a multipart body of unknown length over
    // HTTP/2 gets its stream reset by some servers.
    final var builder =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL);
    if (requestConfig.connectTimeout() > 0) {
      builder.connectTimeout(Duration.ofMillis(requestConfig.connectTimeout()));
    }
    try {
      if (sslContext != null) {
        builder.sslContext(sslContext);
      } else if (sslConfig != null && sslConfig.isEnabled()) {
        final var context = SslContexts.create(sslConfig);
        builder.sslContext(context).sslParameters(SslContexts.parameters(sslConfig, context));
      }
    } catch (GeneralSecurityException | IOException | IllegalArgumentException ex) {
      throw new OfficeException("Could not create the SSL context", ex);
    }
    return builder.build();
  }

  private synchronized HttpClient httpClient() throws OfficeException {
    if (httpClient == null) {
      httpClient = buildHttpClient();
    }
    return httpClient;
  }

  @Override
  public void start() throws OfficeException {
    httpClient();
  }

  @Override
  public void restart() throws OfficeException {
    httpClient();
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void execute(final OfficeTask task) throws OfficeException {

    final var context = new RemoteOfficeConnection(httpClient(), requestConfig);
    current = context; // NOPMD - read by abort() from another thread
    try {
      task.execute(context);
    } finally {
      current = null;
    }
  }

  @Override
  public void abort() {
    final var context = current;
    if (context != null) {
      context.abort();
    }
  }

  @Override
  public void stop() {
    // The client has nothing to close: its connections are released by the JVM.
  }
}
