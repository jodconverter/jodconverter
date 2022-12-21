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

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An RemoteOfficeConnection holds the request configuration to communicate with the LibreOffice
 * Online server.
 */
public class RemoteOfficeConnection implements RemoteOfficeContext {

  private final CloseableHttpClient httpClient;
  private final RequestConfig requestConfig;

  /**
   * Constructs a new connection with the specified client and URL.
   *
   * @param httpClient The HTTP client (already initialized) used to communicate with the
   *     LibreOffice Online server.
   * @param requestConfig The request configuration for the conversion.
   */
  public RemoteOfficeConnection(
      final @NonNull CloseableHttpClient httpClient, final @NonNull RequestConfig requestConfig) {

    this.httpClient = httpClient;
    this.requestConfig = requestConfig;
  }

  @Override
  public @NonNull HttpClient getHttpClient() {
    return httpClient;
  }

  @Override
  public @NonNull RequestConfig getRequestConfig() {
    return requestConfig;
  }
}
