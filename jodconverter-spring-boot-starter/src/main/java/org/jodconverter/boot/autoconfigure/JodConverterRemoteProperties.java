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

package org.jodconverter.boot.autoconfigure;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import org.jodconverter.remote.ssl.SslConfig;

/** Configuration class for JODConverter Remote. */
@ConfigurationProperties("jodconverter.remote")
@org.checkerframework.framework.qual.DefaultQualifier(
    value = org.checkerframework.checker.nullness.qual.Nullable.class,
    locations = {
      org.checkerframework.framework.qual.TypeUseLocation.PARAMETER,
      org.checkerframework.framework.qual.TypeUseLocation.RETURN
    })
public class JodConverterRemoteProperties {

  /** Enable JODConverter Remote. */
  private boolean enabled;

  /** The URL to the LibreOffice Online server. */
  private String url;

  /** Pool size of the manager. */
  private int poolSize = 1;

  /**
   * Directory where temporary files will be created. If not set, it defaults to the system
   * temporary directory as specified by the java.io.tmpdir system property.
   */
  private String workingDir;

  /**
   * Maximum time allowed to process a task. If the processing time of a task is longer than this
   * timeout, this task will be aborted and the next task is processed.
   */
  private long taskExecutionTimeout = 120_000L;

  /**
   * Maximum living time of a task in the conversion queue. The task will be removed from the queue
   * if the waiting time is longer than this timeout.
   */
  private long taskQueueTimeout = 30_000L;

  @NestedConfigurationProperty private SslProperties ssl;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  public int getPoolSize() {
    return poolSize;
  }

  public void setPoolSize(final int poolSize) {
    this.poolSize = poolSize;
  }

  public String getWorkingDir() {
    return workingDir;
  }

  public void setWorkingDir(final String workingDir) {
    this.workingDir = workingDir;
  }

  public long getTaskExecutionTimeout() {
    return taskExecutionTimeout;
  }

  public void setTaskExecutionTimeout(final long taskExecutionTimeout) {
    this.taskExecutionTimeout = taskExecutionTimeout;
  }

  public long getTaskQueueTimeout() {
    return taskQueueTimeout;
  }

  public void setTaskQueueTimeout(final long taskQueueTimeout) {
    this.taskQueueTimeout = taskQueueTimeout;
  }

  public SslProperties getSsl() {
    return this.ssl;
  }

  public void setSsl(final SslProperties ssl) {
    this.ssl = ssl;
  }

  /** Contains the SSL configuration to secure communication with LibreOffice Online. */
  public static class SslProperties {

    /** Enable SSL support. */
    private boolean enabled = true;

    /** Supported SSL ciphers. */
    private String[] ciphers;

    /** Enabled SSL protocols. */
    private String[] enabledProtocols;

    /** Alias that identifies the key in the key store. */
    private String keyAlias;

    /** Password used to access the key in the key store. */
    private String keyPassword;

    /** Path to the key store that holds the SSL certificate (typically a jks file). */
    private String keyStore;

    /** Password used to access the key store. */
    private String keyStorePassword;

    /** Type of the key store. */
    private String keyStoreType;

    /** Provider for the key store. */
    private String keyStoreProvider;

    /** Trust store that holds SSL certificates. */
    private String trustStore;

    /** Password used to access the trust store. */
    private String trustStorePassword;

    /** Type of the trust store. */
    private String trustStoreType;

    /** Provider for the trust store. */
    private String trustStoreProvider;

    /** SSL protocol to use. */
    private String protocol = "TLS";

    /** Indicates if all certificates are trusted (certificate validation becomes disabled). */
    private boolean trustAll = true;

    /** Enable hostname verification during SSL handshake. */
    private boolean verifyHostname = true;

    public boolean isEnabled() {
      return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
      this.enabled = enabled;
    }

    public String[] getCiphers() {
      return this.ciphers;
    }

    public void setCiphers(final String[] ciphers) {
      this.ciphers = ciphers;
    }

    public String getKeyAlias() {
      return this.keyAlias;
    }

    public void setKeyAlias(final String keyAlias) {
      this.keyAlias = keyAlias;
    }

    public String getKeyPassword() {
      return this.keyPassword;
    }

    public void setKeyPassword(final String keyPassword) {
      this.keyPassword = keyPassword;
    }

    public String getKeyStore() {
      return this.keyStore;
    }

    public void setKeyStore(final String keyStore) {
      this.keyStore = keyStore;
    }

    public String getKeyStorePassword() {
      return this.keyStorePassword;
    }

    public void setKeyStorePassword(final String keyStorePassword) {
      this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStoreType() {
      return this.keyStoreType;
    }

    public void setKeyStoreType(final String keyStoreType) {
      this.keyStoreType = keyStoreType;
    }

    public String getKeyStoreProvider() {
      return this.keyStoreProvider;
    }

    public void setKeyStoreProvider(final String keyStoreProvider) {
      this.keyStoreProvider = keyStoreProvider;
    }

    public String[] getEnabledProtocols() {
      return this.enabledProtocols;
    }

    public void setEnabledProtocols(final String[] enabledProtocols) {
      this.enabledProtocols = enabledProtocols;
    }

    public String getTrustStore() {
      return this.trustStore;
    }

    public void setTrustStore(final String trustStore) {
      this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
      return this.trustStorePassword;
    }

    public void setTrustStorePassword(final String trustStorePassword) {
      this.trustStorePassword = trustStorePassword;
    }

    public String getTrustStoreType() {
      return this.trustStoreType;
    }

    public void setTrustStoreType(final String trustStoreType) {
      this.trustStoreType = trustStoreType;
    }

    public String getTrustStoreProvider() {
      return this.trustStoreProvider;
    }

    public void setTrustStoreProvider(final String trustStoreProvider) {
      this.trustStoreProvider = trustStoreProvider;
    }

    public String getProtocol() {
      return this.protocol;
    }

    public void setProtocol(final String protocol) {
      this.protocol = protocol;
    }

    public boolean isTrustAll() {
      return this.trustAll;
    }

    public void setTrustAll(final boolean trustAll) {
      this.trustAll = trustAll;
    }

    public boolean isVerifyHostname() {
      return this.verifyHostname;
    }

    public void setVerifyHostname(final boolean verifyHostname) {
      this.verifyHostname = verifyHostname;
    }

    @NonNull
    public SslConfig sslConfig() {

      final SslConfig sslConfig = new SslConfig();
      sslConfig.setEnabled(isEnabled());
      sslConfig.setCiphers(getCiphers());
      sslConfig.setKeyAlias(getKeyAlias());
      sslConfig.setKeyPassword(getKeyPassword());
      sslConfig.setKeyStore(getKeyStore());
      sslConfig.setKeyStorePassword(getKeyStorePassword());
      sslConfig.setKeyStoreType(getKeyStoreType());
      sslConfig.setKeyStoreProvider(getKeyStoreProvider());
      sslConfig.setEnabledProtocols(getEnabledProtocols());
      sslConfig.setTrustStore(getTrustStore());
      sslConfig.setTrustStorePassword(getTrustStorePassword());
      sslConfig.setTrustStoreType(getTrustStoreType());
      sslConfig.setTrustStoreProvider(getTrustStoreProvider());
      sslConfig.setProtocol(getProtocol());
      sslConfig.setVerifyHostname(isVerifyHostname());
      return sslConfig;
    }
  }
}
