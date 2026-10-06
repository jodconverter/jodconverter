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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
public class JodConverterExternalProperties {

  /** Enable JODConverter External, which connects to already running office processes. */
  private boolean enabled;

  /** Host name of the office processes to connect to, used with the port numbers. */
  private String hostName = "127.0.0.1";

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

  /**
   * Directory where temporary files will be created. If not set, it defaults to the system
   * temporary directory as specified by the java.io.tmpdir system property.
   */
  private String workingDir;

  /**
   * Maximum number of tasks waiting in the conversion queue. A task submitted while the queue is
   * full fails at once. 0 means no limit.
   */
  private int taskQueueCapacity;

  /**
   * Maximum living time of a task in the conversion queue. The task will be removed from the queue
   * if the waiting time is longer than this timeout.
   */
  private long taskQueueTimeout = 30_000L;

  /**
   * Maximum time allowed to process a task. If the processing time of a task is longer than this
   * timeout, this task will be aborted and the next task is processed.
   */
  private long taskExecutionTimeout = 120_000L;

  /** Whether the connections are made when the manager starts, or on the first conversion. */
  private boolean connectOnStart = true;

  /** Timeout, in milliseconds, after which a connection attempt is considered failed. */
  private long connectTimeout = 120_000L;

  /** Delay, in milliseconds, between each connection attempt. */
  private long connectRetryInterval = 250L;

  /**
   * Whether the manager start fails when a connection cannot be made, instead of retrying in the
   * background.
   */
  private boolean connectFailFast;

  /**
   * Maximum number of tasks executed through a connection before reconnecting. 0 means an infinite
   * number of tasks (never reconnects).
   */
  private int maxTasksPerConnection = 1_000;

  /**
   * Whether the default load properties (Hidden, ReadOnly and UpdateDocMode NO_UPDATE) are applied
   * when loading a document.
   */
  private boolean applyDefaultLoadProperties = true;

  /**
   * How documents are loaded and stored: "local" (the office process reads and writes the files
   * directly), "remote" (documents are streamed, for an office process running on another host or
   * container) or "auto".
   */
  private String loadDocumentMode = "auto";

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

  public @Nullable String getWorkingDir() {
    return workingDir;
  }

  public void setWorkingDir(final @Nullable String workingDir) {
    this.workingDir = workingDir;
  }

  public int getTaskQueueCapacity() {
    return taskQueueCapacity;
  }

  public void setTaskQueueCapacity(final int taskQueueCapacity) {
    this.taskQueueCapacity = taskQueueCapacity;
  }

  public long getTaskQueueTimeout() {
    return taskQueueTimeout;
  }

  public void setTaskQueueTimeout(final long taskQueueTimeout) {
    this.taskQueueTimeout = taskQueueTimeout;
  }

  public long getTaskExecutionTimeout() {
    return taskExecutionTimeout;
  }

  public void setTaskExecutionTimeout(final long taskExecutionTimeout) {
    this.taskExecutionTimeout = taskExecutionTimeout;
  }

  public boolean isConnectOnStart() {
    return connectOnStart;
  }

  public void setConnectOnStart(final boolean connectOnStart) {
    this.connectOnStart = connectOnStart;
  }

  public long getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(final long connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public long getConnectRetryInterval() {
    return connectRetryInterval;
  }

  public void setConnectRetryInterval(final long connectRetryInterval) {
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

  public @NonNull String getLoadDocumentMode() {
    return loadDocumentMode;
  }

  public void setLoadDocumentMode(final @NonNull String loadDocumentMode) {
    this.loadDocumentMode = loadDocumentMode;
  }
}
