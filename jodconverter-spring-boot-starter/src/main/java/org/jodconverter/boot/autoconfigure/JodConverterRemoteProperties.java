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

package org.jodconverter.boot.autoconfigure;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.convert.DurationUnit;

import org.jodconverter.remote.office.RemoteOfficeManager;
import org.jodconverter.remote.ssl.SslConfig;

/** Configuration class for JODConverter Remote. */
@ConfigurationProperties("jodconverter.remote")
@SuppressWarnings({
  "PMD.ArrayIsStoredDirectly",
  "PMD.ExcessivePublicCount",
  "PMD.MethodReturnsInternalArray",
  "PMD.TooManyFields",
  "PMD.UseVarargs"
})
public class JodConverterRemoteProperties extends JodConverterPoolProperties {

  /** Enable JODConverter Remote. */
  private boolean enabled;

  /** The URL to the LibreOffice Online server. */
  private String url;

  /**
   * The timeout until a connection is established. A timeout value of zero is interpreted as an
   * infinite timeout. A negative value is interpreted as undefined (system default). A plain number
   * is in milliseconds.
   */
  @DurationUnit(ChronoUnit.MILLIS)
  private Duration connectTimeout = Duration.ofMillis(RemoteOfficeManager.DEFAULT_CONNECT_TIMEOUT);

  /**
   * The socket timeout, which is the timeout for waiting for data or, put differently, a maximum
   * period inactivity between two consecutive data packets. A timeout value of zero is interpreted
   * as an infinite timeout. A negative value is interpreted as undefined (system default). A plain
   * number is in milliseconds.
   */
  @DurationUnit(ChronoUnit.MILLIS)
  private Duration socketTimeout = Duration.ofMillis(RemoteOfficeManager.DEFAULT_SOCKET_TIMEOUT);

  /** Pool size of the manager. */
  private int poolSize = RemoteOfficeManager.DEFAULT_POOL_SIZE;

  /** The SSL configuration of the connection to the LibreOffice Online server, if it uses HTTPS. */
  @NestedConfigurationProperty private SslConfig ssl;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public @Nullable String getUrl() {
    return url;
  }

  public void setUrl(final @Nullable String url) {
    this.url = url;
  }

  public @NonNull Duration getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(final @NonNull Duration connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public @NonNull Duration getSocketTimeout() {
    return socketTimeout;
  }

  public void setSocketTimeout(final @NonNull Duration socketTimeout) {
    this.socketTimeout = socketTimeout;
  }

  public int getPoolSize() {
    return poolSize;
  }

  public void setPoolSize(final int poolSize) {
    this.poolSize = poolSize;
  }

  public @Nullable SslConfig getSsl() {
    return this.ssl;
  }

  public void setSsl(final @Nullable SslConfig ssl) {
    this.ssl = ssl;
  }
}
