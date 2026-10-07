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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * The context given to the tasks of a {@link RemoteOfficeWorker}: it sends their requests with the
 * HTTP client of the worker, and cancels the request in flight when the task is aborted.
 */
public class RemoteOfficeConnection implements RemoteOfficeContext {

  private final HttpClient httpClient;
  private final RequestConfig requestConfig;
  // The request in flight, cancelled to abort the task.
  private volatile CompletableFuture<?> inFlight;
  private volatile boolean aborted;

  /**
   * Creates a new context.
   *
   * @param httpClient The HTTP client of the worker.
   * @param requestConfig The configuration of the requests.
   */
  public RemoteOfficeConnection(
      final @NonNull HttpClient httpClient, final @NonNull RequestConfig requestConfig) {
    super();

    this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
    this.requestConfig = Objects.requireNonNull(requestConfig, "requestConfig must not be null");
  }

  @Override
  public @NonNull HttpClient getHttpClient() {
    return httpClient;
  }

  @Override
  public @NonNull RequestConfig getRequestConfig() {
    return requestConfig;
  }

  @Override
  public <T> @NonNull HttpResponse<T> send(
      final @NonNull HttpRequest request, final HttpResponse.@NonNull BodyHandler<T> handler)
      throws IOException, InterruptedException {

    if (aborted) {
      throw new IOException("The task was aborted");
    }
    final var future = httpClient.sendAsync(request, handler);
    inFlight = future;
    try {
      return future.get();
    } catch (CancellationException ex) {
      throw new IOException("The request was aborted", ex);
    } catch (ExecutionException ex) {
      final var cause = ex.getCause();
      if (cause instanceof IOException ioEx) {
        throw ioEx;
      }
      if (cause instanceof RuntimeException runtimeEx) {
        throw runtimeEx;
      }
      throw new IOException(cause);
    } finally {
      inFlight = null;
    }
  }

  /** Aborts the task: the request in flight, if any, is cancelled, and no other one is sent. */
  /* default */ void abort() {
    aborted = true;
    final var future = inFlight;
    if (future != null) {
      future.cancel(true);
    }
  }
}
