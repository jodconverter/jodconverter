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
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.task.LoadDocumentMode;

/** Configuration class for JODConverter. */
@ConfigurationProperties("jodconverter.local")
public class JodConverterLocalProperties extends JodConverterPoolProperties {

  /** Enable JODConverter, which means that office instances will be launched. */
  private boolean enabled;

  /**
   * Represents the office home directory. If not set, the office installation directory is
   * auto-detected, the most recent version of LibreOffice first.
   */
  private String officeHome;

  /**
   * Program that starts the office processes, instead of the executable found in the office home.
   * Use it for a launcher, such as the one of a snap (/snap/bin/libreoffice) or an AppImage. When
   * set, the office home is not required.
   */
  private String officeExecutable;

  /**
   * Host name that will be used in the --accept argument when starting an office process. Most of
   * the time, the default will work. But if it doesn't work (unable to connect to the started
   * process), using 'localhost' as the host name instead may work.
   */
  private String hostName = LocalOfficeManager.DEFAULT_HOSTNAME;

  /**
   * List of ports, separated by commas, used by each JODConverter processing thread. The number of
   * office instances is equal to the number of port numbers/pipe names, since 1 office process will
   * be launched for each port number/pipe name. When neither port numbers, pipe names nor a pool
   * size are set, the port 2002 is used.
   */
  private int[] portNumbers;

  /**
   * Number of office processes to start, using free port numbers picked at startup. An alternative
   * to port numbers and pipe names, which cannot be combined with them.
   */
  private Integer poolSize;

  /**
   * List of pipe names, separated by commas, used by each JODConverter processing thread. The
   * number of office instances is equal to the number of port numbers/pipe names, since 1 office
   * process will be launched for each port number/pipe name.
   */
  private String[] pipeNames = {};

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
   * Process timeout, used when trying to execute an office process call (start/connect/terminate).
   * A plain number is in milliseconds.
   */
  @DurationUnit(ChronoUnit.MILLIS)
  private Duration processTimeout = Duration.ofMillis(LocalOfficeManager.DEFAULT_PROCESS_TIMEOUT);

  /**
   * Process retry interval, used for waiting between office process call tries
   * (start/connect/terminate). A plain number is in milliseconds.
   */
  @DurationUnit(ChronoUnit.MILLIS)
  private Duration processRetryInterval =
      Duration.ofMillis(LocalOfficeManager.DEFAULT_PROCESS_RETRY_INTERVAL);

  /**
   * Specifies the delay after an attempt to start an office process before doing anything else. A
   * plain number is in milliseconds.
   */
  @DurationUnit(ChronoUnit.MILLIS)
  private Duration afterStartProcessDelay =
      Duration.ofMillis(LocalOfficeManager.DEFAULT_AFTER_START_PROCESS_DELAY);

  /**
   * Specifies the action that must be taken when starting a new office process, and there already
   * is an existing running process for the same connection string.
   */
  private ExistingProcessAction existingProcessAction =
      LocalOfficeManager.DEFAULT_EXISTING_PROCESS_ACTION;

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

  /** Maximum number of tasks an office process can execute before restarting. */
  private int maxTasksPerProcess = LocalOfficeManager.DEFAULT_MAX_TASKS_PER_PROCESS;

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
  private LoadDocumentMode loadDocumentMode = LocalConverter.DEFAULT_LOAD_DOCUMENT_MODE;

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

  public @Nullable String getOfficeExecutable() {
    return officeExecutable;
  }

  public void setOfficeExecutable(final @Nullable String officeExecutable) {
    this.officeExecutable = officeExecutable;
  }

  public @Nullable String getHostName() {
    return hostName;
  }

  public void setHostName(final @Nullable String hostName) {
    this.hostName = hostName;
  }

  public int @Nullable [] getPortNumbers() {
    return portNumbers;
  }

  public void setPortNumbers(final int @Nullable [] portNumbers) {
    this.portNumbers = portNumbers;
  }

  public @Nullable Integer getPoolSize() {
    return poolSize;
  }

  public void setPoolSize(final @Nullable Integer poolSize) {
    this.poolSize = poolSize;
  }

  public String[] getPipeNames() {
    return pipeNames;
  }

  public void setPipeNames(final String[] pipeNames) {
    this.pipeNames = pipeNames;
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

  public @NonNull Duration getProcessTimeout() {
    return processTimeout;
  }

  public void setProcessTimeout(final @NonNull Duration processTimeout) {
    this.processTimeout = processTimeout;
  }

  public @NonNull Duration getProcessRetryInterval() {
    return processRetryInterval;
  }

  public void setProcessRetryInterval(final @NonNull Duration procesRetryInterval) {
    this.processRetryInterval = procesRetryInterval;
  }

  public @NonNull Duration getAfterStartProcessDelay() {
    return afterStartProcessDelay;
  }

  public void setAfterStartProcessDelay(final @NonNull Duration afterStartProcessDelay) {
    this.afterStartProcessDelay = afterStartProcessDelay;
  }

  public @NonNull ExistingProcessAction getExistingProcessAction() {
    return existingProcessAction;
  }

  public void setExistingProcessAction(final @NonNull ExistingProcessAction existingProcessAction) {
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

  public int getMaxTasksPerProcess() {
    return maxTasksPerProcess;
  }

  public void setMaxTasksPerProcess(final int maxTasksPerProcess) {
    this.maxTasksPerProcess = maxTasksPerProcess;
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

  public @NonNull LoadDocumentMode getLoadDocumentMode() {
    return loadDocumentMode;
  }

  public void setLoadDocumentMode(final @NonNull LoadDocumentMode loadDocumentMode) {
    this.loadDocumentMode = loadDocumentMode;
  }
}
