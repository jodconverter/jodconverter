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

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * The configuration of the requests sent to a LibreOffice Online server.
 *
 * @param url The URL of the conversion service, ending with a slash: the extension of the target
 *     format is appended to it.
 * @param connectTimeout The timeout, in milliseconds, until a connection is established; 0 for no
 *     timeout.
 * @param socketTimeout The timeout, in milliseconds, for the response of the server once the
 *     request is sent; 0 for no timeout.
 */
public record RequestConfig(@NonNull String url, long connectTimeout, long socketTimeout) {

  /**
   * Creates a new configuration.
   *
   * @param url The URL of the conversion service.
   * @param connectTimeout The connect timeout, in milliseconds.
   * @param socketTimeout The response timeout, in milliseconds.
   */
  public RequestConfig {
    Objects.requireNonNull(url, "url must not be null");
  }
}
