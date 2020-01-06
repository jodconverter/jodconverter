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

package org.jodconverter.office;

/** Contains the request configuration to communication with LibreOffice Online. */
public class RequestConfig {

  private final String url;
  private final int connectTimeout;
  private final int socketTimeout;

  /**
   * Constructs a new configuration with the specified arguments.
   *
   * @param url The URL for the conversion.
   * @param connectTimeout The timeout in milliseconds until a connection is established. A timeout
   *     value of zero is interpreted as an infinite timeout. A negative value is interpreted as
   *     undefined (system default).
   * @param socketTimeout The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the
   *     timeout for waiting for data or, put differently, a maximum period inactivity between two
   *     consecutive data packets). A timeout value of zero is interpreted as an infinite timeout. A
   *     negative value is interpreted as undefined (system default).
   */
  public RequestConfig(final String url, final int connectTimeout, final int socketTimeout) {

    this.url = url;
    this.connectTimeout = connectTimeout;
    this.socketTimeout = socketTimeout;
  }

  /**
   * Gets the URL where conversion request can be sent.
   *
   * @return The URL where to send conversion request.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets the timeout in milliseconds until a connection is established. A timeout value of zero is
   * interpreted as an infinite timeout.
   *
   * <p>A timeout value of zero is interpreted as an infinite timeout. A negative value is
   * interpreted as undefined (system default).
   *
   * <p>Default: {@code -1}
   *
   * @return The connection timeout.
   */
  public int getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Gets the socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the timeout for waiting
   * for data or, put differently, a maximum period inactivity between two consecutive data
   * packets).
   *
   * <p>A timeout value of zero is interpreted as an infinite timeout. A negative value is
   * interpreted as undefined (system default).
   *
   * <p>Default: {@code -1}
   *
   * @return The socket timeout.
   */
  public int getSocketTimeout() {
    return socketTimeout;
  }
}
