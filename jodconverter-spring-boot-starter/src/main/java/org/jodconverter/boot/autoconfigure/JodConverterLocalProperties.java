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

import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.jodconverter.core.document.DocumentFormatProperties;
import org.jodconverter.local.office.ExistingProcessAction;

/** Configuration class for JODConverter. */
@ConfigurationProperties("jodconverter.local")
public class JodConverterLocalProperties {

  /** Enable JODConverter, which means that office instances will be launched. */
  private boolean enabled;

  /**
   * Represents the office home directory. If not set, the office installation directory is
   * auto-detected, most recent version of LibreOffice first.
   */
  private String officeHome;

  /**
   * List of ports, separated by commas, used by each JODConverter processing thread. The number of
   * office instances is equal to the number of ports, since 1 office process will be launched for
   * each port number.
   */
  private int[] portNumbers = new int[] {2002};

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
   * Specifies the action the must be taken when starting a new office process and there already is
   * a existing running process for the same connection string.
   */
  private ExistingProcessAction existingProcessAction = ExistingProcessAction.KILL;

  /**
   * Process timeout (milliseconds). Used when trying to execute an office process call
   * (start/terminate).
   */
  private long processTimeout = 120_000L;

  /**
   * Process retry interval (milliseconds). Used for waiting between office process call tries
   * (start/terminate).
   */
  private long processRetryInterval = 250L;

  /**
   * Maximum time allowed to process a task. If the processing time of a task is longer than this
   * timeout, this task will be aborted and the next task is processed.
   */
  private long taskExecutionTimeout = 120_000L;

  /** Maximum number of tasks an office process can execute before restarting. */
  private int maxTasksPerProcess = 200;

  /**
   * Maximum living time of a task in the conversion queue. The task will be removed from the queue
   * if the waiting time is longer than this timeout.
   */
  private long taskQueueTimeout = 30_000L;

  /**
   * Class name for explicit office process manager. Type of the provided process manager. The class
   * must implement the org.jodconverter.process.ProcessManager interface.
   */
  private String processManagerClass;

  /** Path to the registry which contains the document formats that will be supported by default. */
  private String documentFormatRegistry;

  /** Custom properties required to load(open) and store(save) documents. */
  private Map<String, DocumentFormatProperties> formatOptions;

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

  public @Nullable ExistingProcessAction getExistingProcessAction() {
    return existingProcessAction;
  }

  public void setExistingProcessAction(
      final @Nullable ExistingProcessAction existingProcessAction) {
    this.existingProcessAction = existingProcessAction;
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

  public long getTaskQueueTimeout() {
    return taskQueueTimeout;
  }

  public void setTaskQueueTimeout(final long taskQueueTimeout) {
    this.taskQueueTimeout = taskQueueTimeout;
  }

  public @Nullable String getProcessManagerClass() {
    return processManagerClass;
  }

  public void setProcessManagerClass(final @Nullable String processManagerClass) {
    this.processManagerClass = processManagerClass;
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
}
