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
import org.springframework.boot.convert.DurationUnit;

import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExternalOfficeManager;
import org.jodconverter.local.task.LoadDocumentMode;

/**
 * Configuration class for JODConverter External: connects to office processes that are started and
 * managed outside of the application (another container, a service manager...).
 */
@ConfigurationProperties("jodconverter.external")
@SuppressWarnings({
  "PMD.ArrayIsStoredDirectly",
  "PMD.ExcessivePublicCount",
  "PMD.MethodReturnsInternalArray",
  "PMD.TooManyFields",
  "PMD.UseVarargs"
})
public class JodConverterExternalProperties extends JodConverterPoolProperties {

  /** Enable JODConverter External, which connects to already running office processes. */
  private boolean enabled;

  /** Host name of the office processes to connect to, used with the port numbers. */
  private String hostName = ExternalOfficeManager.DEFAULT_HOSTNAME;

  /**
   * List of ports, separated by commas, of the office processes to connect to. One connection is
   * made for each port number/pipe name/websocket URL. When none of them is set, the port 2002 is
   * used.
   */
  private int[] portNumbers;

  /** List of pipe names, separated by commas, of the office processes to connect to. */
  private String[] pipeNames;

  /** List of websocket URLs, separated by commas, of the office processes to connect to. */
  private String[] websocketUrls;

  /** Whether the connections are made when the manager starts, or on the first conversion. */
  private boolean connectOnStart = ExternalOfficeManager.DEFAULT_CONNECT_ON_START;

  /**
   * Timeout after which a connection attempt is considered failed. A plain number is in
   * milliseconds.
   */
  @DurationUnit(ChronoUnit.MILLIS)
  private Duration connectTimeout =
      Duration.ofMillis(ExternalOfficeManager.DEFAULT_CONNECT_TIMEOUT);

  /** Delay between each connection attempt. A plain number is in milliseconds. */
  @DurationUnit(ChronoUnit.MILLIS)
  private Duration connectRetryInterval =
      Duration.ofMillis(ExternalOfficeManager.DEFAULT_CONNECT_RETRY_INTERVAL);

  /**
   * Whether the manager start fails when a connection cannot be made, instead of retrying in the
   * background.
   */
  private boolean connectFailFast = ExternalOfficeManager.DEFAULT_CONNECT_FAIL_FAST;

  /**
   * Maximum number of tasks executed through a connection before reconnecting. 0 means an infinite
   * number of tasks (never reconnects).
   */
  private int maxTasksPerConnection = ExternalOfficeManager.DEFAULT_MAX_TASKS_PER_CONNECTION;

  /**
   * Whether the default load properties (Hidden, ReadOnly and UpdateDocMode NO_UPDATE) are applied
   * when loading a document.
   */
  private boolean applyDefaultLoadProperties = LocalConverter.DEFAULT_APPLY_DEFAULT_LOAD_PROPS;

  /**
   * How documents are loaded and stored: local (the office process reads and writes the files
   * directly), remote (documents are streamed, for an office process running on another host or
   * container) or auto.
   */
  private LoadDocumentMode loadDocumentMode = LocalConverter.DEFAULT_LOAD_DOCUMENT_MODE;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public @NonNull String getHostName() {
    return hostName;
  }

  public void setHostName(final @NonNull String hostName) {
    this.hostName = hostName;
  }

  public int @Nullable [] getPortNumbers() {
    return portNumbers;
  }

  public void setPortNumbers(final int @Nullable [] portNumbers) {
    this.portNumbers = portNumbers;
  }

  public String @Nullable [] getPipeNames() {
    return pipeNames;
  }

  public void setPipeNames(final String @Nullable [] pipeNames) {
    this.pipeNames = pipeNames;
  }

  public String @Nullable [] getWebsocketUrls() {
    return websocketUrls;
  }

  public void setWebsocketUrls(final String @Nullable [] websocketUrls) {
    this.websocketUrls = websocketUrls;
  }

  public boolean isConnectOnStart() {
    return connectOnStart;
  }

  public void setConnectOnStart(final boolean connectOnStart) {
    this.connectOnStart = connectOnStart;
  }

  public @NonNull Duration getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(final @NonNull Duration connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public @NonNull Duration getConnectRetryInterval() {
    return connectRetryInterval;
  }

  public void setConnectRetryInterval(final @NonNull Duration connectRetryInterval) {
    this.connectRetryInterval = connectRetryInterval;
  }

  public boolean isConnectFailFast() {
    return connectFailFast;
  }

  public void setConnectFailFast(final boolean connectFailFast) {
    this.connectFailFast = connectFailFast;
  }

  public int getMaxTasksPerConnection() {
    return maxTasksPerConnection;
  }

  public void setMaxTasksPerConnection(final int maxTasksPerConnection) {
    this.maxTasksPerConnection = maxTasksPerConnection;
  }

  public boolean isApplyDefaultLoadProperties() {
    return applyDefaultLoadProperties;
  }

  public void setApplyDefaultLoadProperties(final boolean applyDefaultLoadProperties) {
    this.applyDefaultLoadProperties = applyDefaultLoadProperties;
  }

  public @NonNull LoadDocumentMode getLoadDocumentMode() {
    return loadDocumentMode;
  }

  public void setLoadDocumentMode(final @NonNull LoadDocumentMode loadDocumentMode) {
    this.loadDocumentMode = loadDocumentMode;
  }
}
