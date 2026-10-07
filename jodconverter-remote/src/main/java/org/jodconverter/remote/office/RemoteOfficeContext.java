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

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.office.OfficeContext;

/**
 * The context of a task executed by a {@link RemoteOfficeManager}: the HTTP client of the office
 * worker and the configuration of the requests.
 */
public interface RemoteOfficeContext extends OfficeContext {

  /**
   * Gets the HTTP client of the worker, built once with the SSL configuration of the manager.
   *
   * @return The HTTP client.
   */
  @NonNull HttpClient getHttpClient();

  /**
   * Gets the configuration of the requests: the URL of the conversion service and the timeouts.
   *
   * @return The request configuration.
   */
  @NonNull RequestConfig getRequestConfig();

  /**
   * Sends a request with the HTTP client of the worker and waits for the response. A task sends its
   * requests through this method rather than through the client, so that the request is cancelled
   * when the task is aborted (on a task timeout, or when the manager stops).
   *
   * @param request The request to send.
   * @param handler The handler of the response body.
   * @param <T> The type of the response body.
   * @return The response.
   * @throws IOException If the request fails, or if it was aborted.
   * @throws InterruptedException If the current thread is interrupted while waiting.
   */
  <T> @NonNull HttpResponse<T> send(
      @NonNull HttpRequest request, HttpResponse.@NonNull BodyHandler<T> handler)
      throws IOException, InterruptedException;
}
