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

import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.jodconverter.core.document.DocumentFormatProperties;
import org.jodconverter.core.office.AbstractOfficeManagerPool;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;

/** Configuration class for JODConverter. */
@ConfigurationProperties("jodconverter.local")
public class JodConverterLocalProperties {

  /** Enable JODConverter, which means that office instances will be launched. */
  private boolean enabled;

  /**
   * Represents the office home directory. If not set, the office installation directory is
   * auto-detected, the most recent version of LibreOffice first.
   */
  private String officeHome;

  /**
   * Host name that will be used in the --accept argument when starting an office process. Most of
   * the time, the default will work. But if it doesn't work (unable to connect to the started
   * process), using 'localhost' as the host name instead may work.
   */
  private String hostName = LocalOfficeManager.DEFAULT_HOSTNAME;

  /**
   * List of ports, separated by commas, used by each JODConverter processing thread. The number of
   * office instances is equal to the number of ports, since 1 office process will be launched for
   * each port number.
   */
  private int[] portNumbers = {2002};

  /**
   * Directory where temporary office profiles will be created. If not set, it defaults to the
   * system temporary directory as specified by the java.io.tmpdir system property.
   */
  private String workingDir;

  /**
   * Template profile directory to copy to a created office profile directory when an office
   * processed is launched.
   */
  private String templateProfileDir;

  /**
   * Class name for explicit office process manager. Type of the provided process manager. The class
   * must implement the org.jodconverter.local.process.ProcessManager interface.
   */
  private String processManagerClass;

  /**
   * Process timeout (milliseconds). Used when trying to execute an office process call
   * (start/connect/terminate).
   */
  private long processTimeout = LocalOfficeManager.DEFAULT_PROCESS_TIMEOUT;

  /**
   * Process retry interval (milliseconds). Used for waiting between office process call tries
   * (start/connect/terminate).
   */
  private long processRetryInterval = LocalOfficeManager.DEFAULT_PROCESS_RETRY_INTERVAL;

  /** Specifies the delay after an attempt to start an office process before doing anything else. */
  private long afterStartProcessDelay = LocalOfficeManager.DEFAULT_AFTER_START_PROCESS_DELAY;

  /**
   * Specifies the action that must be taken when starting a new office process, and there already
   * is an existing running process for the same connection string.
   */
  private String existingProcessAction = LocalOfficeManager.DEFAULT_EXISTING_PROCESS_ACTION_STRING;

  /**
   * Controls whether the manager will "fail fast" if an office process cannot be started or the
   * connection to the started process fails. If set to {@code true}, the start of a process will
   * wait for the task to be completed, and will throw an exception if the office process is not
   * started successfully or if the connection to the started process fails, preventing the
   * application from starting. If set to {@code false}, the task of starting the process and
   * connecting to it will be submitted and will return immediately, meaning a faster starting
   * process. Only error logs will be produced if anything goes wrong.
   */
  private boolean startFailFast = LocalOfficeManager.DEFAULT_START_FAIL_FAST;

  /**
   * Controls whether the manager will keep the office process alive on shutdown. If set to {@code
   * true}, the stop task will only disconnect from the office process, which will stay alive. If
   * set to {@code false}, the office process will be stopped gracefully (or killed if could not be
   * stopped gracefully).
   */
  private boolean keepAliveOnShutdown = LocalOfficeManager.DEFAULT_KEEP_ALIVE_ON_SHUTDOWN;

  /**
   * Maximum living time of a task in the conversion queue. The task will be removed from the queue
   * if the waiting time is longer than this timeout.
   */
  private long taskQueueTimeout = AbstractOfficeManagerPool.DEFAULT_TASK_QUEUE_TIMEOUT;

  /**
   * Maximum time allowed to process a task. If the processing time of a task is longer than this
   * timeout, this task will be aborted and the next task is processed.
   */
  private long taskExecutionTimeout = AbstractOfficeManagerPool.DEFAULT_TASK_EXECUTION_TIMEOUT;

  /** Maximum number of tasks an office process can execute before restarting. */
  private int maxTasksPerProcess = LocalOfficeManager.DEFAULT_MAX_TASKS_PER_PROCESS;

  /** Path to the registry which contains the document formats that will be supported by default. */
  private String documentFormatRegistry;

  /** Custom properties required to load(open) and store(save) documents. */
  private Map<String, DocumentFormatProperties> formatOptions;

  /**
   * Specifies this converter will apply the default load properties when loading a source document.
   */
  private boolean applyDefaultLoadProperties = LocalConverter.DEFAULT_APPLY_DEFAULT_LOAD_PROPS;

  /**
   * Specifies whether this converter will use the unsafe {@code UpdateDocMode.QUIET_UPDATE} as
   * default for the {@code UpdateDocMode} load property, which was the default until JODConverter
   * version 4.4.4.
   */
  private boolean useUnsafeQuietUpdate = LocalConverter.DEFAULT_USE_UNSAFE_QUIET_UPDATE;

  /**
   * Specifies how a document is loaded/stored when converting a document, whether it is loaded
   * assuming the office process has access to the file on disk or not. If not, the conversion
   * process will use stream adapters.
   */
  private String loadDocumentMode = LocalConverter.DEFAULT_LOAD_DOCUMENT_MODE_STRING;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public @Nullable String getOfficeHome() {
    return officeHome;
  }

  public void setOfficeHome(final @Nullable String officeHome) {
    this.officeHome = officeHome;
  }

  public @Nullable String getHostName() {
    return hostName;
  }

  public void setHostName(final @Nullable String hostName) {
    this.hostName = hostName;
  }

  public int[] getPortNumbers() {
    return portNumbers;
  }

  public void setPortNumbers(final int[] portNumbers) {
    this.portNumbers = portNumbers;
  }

  public @Nullable String getWorkingDir() {
    return workingDir;
  }

  public void setWorkingDir(final @Nullable String workingDir) {
    this.workingDir = workingDir;
  }

  public @Nullable String getTemplateProfileDir() {
    return templateProfileDir;
  }

  public void setTemplateProfileDir(final @Nullable String templateProfileDir) {
    this.templateProfileDir = templateProfileDir;
  }

  public @Nullable String getProcessManagerClass() {
    return processManagerClass;
  }

  public void setProcessManagerClass(final @Nullable String processManagerClass) {
    this.processManagerClass = processManagerClass;
  }

  public long getProcessTimeout() {
    return processTimeout;
  }

  public void setProcessTimeout(final long processTimeout) {
    this.processTimeout = processTimeout;
  }

  public long getProcessRetryInterval() {
    return processRetryInterval;
  }

  public void setProcessRetryInterval(final long procesRetryInterval) {
    this.processRetryInterval = procesRetryInterval;
  }

  public long getAfterStartProcessDelay() {
    return afterStartProcessDelay;
  }

  public void setAfterStartProcessDelay(final long afterStartProcessDelay) {
    this.afterStartProcessDelay = afterStartProcessDelay;
  }

  public @Nullable String getExistingProcessAction() {
    return existingProcessAction;
  }

  public void setExistingProcessAction(final @Nullable String existingProcessAction) {
    this.existingProcessAction = existingProcessAction;
  }

  public boolean isStartFailFast() {
    return startFailFast;
  }

  public void setStartFailFast(final boolean startFailFast) {
    this.startFailFast = startFailFast;
  }

  public boolean isKeepAliveOnShutdown() {
    return keepAliveOnShutdown;
  }

  public void setKeepAliveOnShutdown(final boolean keepAliveOnShutdown) {
    this.keepAliveOnShutdown = keepAliveOnShutdown;
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

  public int getMaxTasksPerProcess() {
    return maxTasksPerProcess;
  }

  public void setMaxTasksPerProcess(final int maxTasksPerProcess) {
    this.maxTasksPerProcess = maxTasksPerProcess;
  }

  public @Nullable String getDocumentFormatRegistry() {
    return documentFormatRegistry;
  }

  public void setDocumentFormatRegistry(final @Nullable String documentFormatRegistry) {
    this.documentFormatRegistry = documentFormatRegistry;
  }

  public @Nullable Map<@NonNull String, @NonNull DocumentFormatProperties> getFormatOptions() {
    return formatOptions;
  }

  public void setFormatOptions(
      final @Nullable Map<@NonNull String, @NonNull DocumentFormatProperties> formatOptions) {
    this.formatOptions = formatOptions;
  }

  public boolean isApplyDefaultLoadProperties() {
    return applyDefaultLoadProperties;
  }

  public void setApplyDefaultLoadProperties(final boolean applyDefaultLoadProperties) {
    this.applyDefaultLoadProperties = applyDefaultLoadProperties;
  }

  public boolean isUseUnsafeQuietUpdate() {
    return useUnsafeQuietUpdate;
  }

  public void setUseUnsafeQuietUpdate(final boolean useUnsafeQuietUpdate) {
    this.useUnsafeQuietUpdate = useUnsafeQuietUpdate;
  }

  public @Nullable String getLoadDocumentMode() {
    return loadDocumentMode;
  }

  public void setLoadDocumentMode(final @Nullable String loadDocumentMode) {
    this.loadDocumentMode = loadDocumentMode;
  }
}
