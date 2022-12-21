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

package org.jodconverter.spring;

import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_EXECUTION_TIMEOUT;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_QUEUE_TIMEOUT;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_AFTER_START_PROCESS_DELAY;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_DISABLE_OPENGL;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_EXISTING_PROCESS_ACTION;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_HOSTNAME;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_KEEP_ALIVE_ON_SHUTDOWN;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_MAX_TASKS_PER_PROCESS;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_PROCESS_RETRY_INTERVAL;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_PROCESS_TIMEOUT;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_START_FAIL_FAST;
import static org.jodconverter.local.office.LocalOfficeManager.builder;

import java.util.Set;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager.Builder;
import org.jodconverter.local.process.ProcessManager;

/**
 * The purpose of this class is to provide to the Spring Container a Bean that encapsulates the
 * functionality already present in the JODConverter-CORE library. The target of this bean is to
 * provide the functionality of the PocessPoolOfficeManager.
 *
 * <p>The Controller shall launch the OO processes. The Controller shall stop the OO processes when
 * it´s time to shut down the application
 *
 * @author Jose Luis López López
 */
public class JodConverterBean implements InitializingBean, DisposableBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(JodConverterBean.class);

  private String workingDir;
  private Long taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;
  private Long taskQueueTimeout = DEFAULT_TASK_QUEUE_TIMEOUT;

  private String hostName = DEFAULT_HOSTNAME;
  private String portNumbers;
  private String officeHome;
  private String processManagerClass;
  private String templateProfileDir;
  private Boolean useDefaultOnInvalidTemplateProfileDir;
  private Long processTimeout = DEFAULT_PROCESS_TIMEOUT;
  private Long processRetryInterval = DEFAULT_PROCESS_RETRY_INTERVAL;
  private Long afterStartProcessDelay = DEFAULT_AFTER_START_PROCESS_DELAY;
  private ExistingProcessAction existingProcessAction = DEFAULT_EXISTING_PROCESS_ACTION;
  private Boolean startFailFast = DEFAULT_START_FAIL_FAST;
  private Boolean keepAliveOnShutdown = DEFAULT_KEEP_ALIVE_ON_SHUTDOWN;
  private Boolean disableOpengl = DEFAULT_DISABLE_OPENGL;
  private Integer maxTasksPerProcess = DEFAULT_MAX_TASKS_PER_PROCESS;

  private OfficeManager officeManager;
  private DocumentConverter documentConverter;

  @Override
  public void afterPropertiesSet() throws OfficeException {

    final Builder builder = builder();

    if (!StringUtils.isBlank(portNumbers)) {
      builder.portNumbers(
          Stream.of(portNumbers.split("\\s*,\\s*"))
              .mapToInt(
                  str -> {
                    try {
                      return Integer.parseInt(str);
                    } catch (final Exception e) {
                      return 2002;
                    }
                  })
              .toArray());
    }

    builder
        .hostName(hostName)
        .workingDir(workingDir)
        .taskExecutionTimeout(taskExecutionTimeout)
        .taskQueueTimeout(taskQueueTimeout)
        .officeHome(officeHome)
        .processManager(processManagerClass)
        .processTimeout(processTimeout)
        .processRetryInterval(processRetryInterval)
        .afterStartProcessDelay(afterStartProcessDelay)
        .existingProcessAction(existingProcessAction)
        .startFailFast(startFailFast)
        .keepAliveOnShutdown(keepAliveOnShutdown)
        .disableOpengl(disableOpengl)
        .maxTasksPerProcess(maxTasksPerProcess);
    if (Boolean.TRUE.equals(useDefaultOnInvalidTemplateProfileDir)) {
      builder.templateProfileDirOrDefault(templateProfileDir);
    } else {
      builder.templateProfileDir(templateProfileDir);
    }

    // Starts the manager
    officeManager = builder.build();
    documentConverter = LocalConverter.make(officeManager);
    officeManager.start();
  }

  /**
   * Gets the {@link OfficeManager} created by this bean.
   *
   * @return The manager created by this bean.
   */
  @NonNull
  public OfficeManager getManager() {
    return officeManager;
  }

  /**
   * Gets the {@link DocumentConverter} created by this bean.
   *
   * @return The converter created by this bean.
   */
  @NonNull
  public DocumentConverter getConverter() {
    return documentConverter;
  }

  @Override
  public void destroy() {

    OfficeUtils.stopQuietly(officeManager);
  }

  /** Prints the available formats provided by the JODConverter module. */
  public void logAvailableFormats() {

    final DocumentFormatRegistry ref = DefaultDocumentFormatRegistry.getInstance();

    Set<DocumentFormat> formats = ref.getOutputFormats(DocumentFamily.TEXT);
    logSupportedGroupFormats("Supported Text Document Formats are:", formats);
    formats = ref.getOutputFormats(DocumentFamily.SPREADSHEET);
    logSupportedGroupFormats("Supported SpreadSheet Document Formats are:", formats);
    formats = ref.getOutputFormats(DocumentFamily.PRESENTATION);
    logSupportedGroupFormats("Supported Presentation Document Formats are:", formats);
    formats = ref.getOutputFormats(DocumentFamily.DRAWING);
    logSupportedGroupFormats("Supported Drawing Document Formats are:", formats);
  }

  /** Prints the available formats provided by the JODConverter module. */
  private void logSupportedGroupFormats(final String text, final Set<DocumentFormat> formats) {

    LOGGER.info(text);
    formats.stream().map(DocumentFormat::getName).forEach(LOGGER::info);
  }

  /**
   * Specifies the directory where temporary files and directories are created.
   *
   * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
   * java.io.tmpdir</code> system property.
   *
   * @param workingDir The new working directory to set.
   */
  public void setWorkingDir(final @Nullable String workingDir) {
    this.workingDir = workingDir;
  }

  /**
   * Specifies the maximum time allowed to process a task. If the processing time of a task is
   * longer than this timeout, this task will be aborted and the next task is processed.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
   *
   * @param taskExecutionTimeout The task execution timeout, in milliseconds.
   */
  public void setTaskExecutionTimeout(final @Nullable Long taskExecutionTimeout) {
    this.taskExecutionTimeout = taskExecutionTimeout;
  }

  /**
   * Specifies the maximum living time of a task in the conversion queue. The task will be removed
   * from the queue if the waiting time is longer than this timeout.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 30000 (30 seconds)
   *
   * @param taskQueueTimeout The task queue timeout, in milliseconds.
   */
  public void setTaskQueueTimeout(final @Nullable Long taskQueueTimeout) {
    this.taskQueueTimeout = taskQueueTimeout;
  }

  /**
   * Sets the host name that will be use in the --accept argument when starting an office process.
   * Most of the time, the default will work. But if it doesn't work (unable to connect to the
   * started process), using {@code localhost} instead may work.
   *
   * @param hostName the host name to use.
   */
  public void setHostName(final @Nullable String hostName) {
    this.hostName = hostName;
  }

  /**
   * Sets the list of ports, separated by commas, used by each JODConverter processing thread. The
   * number of office instances is equal to the number of ports, since 1 office will be launched for
   * each port number.
   *
   * @param portNumbers the port numbers to use.
   */
  public void setPortNumbers(final @Nullable String portNumbers) {
    this.portNumbers = portNumbers;
  }

  /**
   * Specifies the office home directory (office installation).
   *
   * @param officeHome The new home directory to set.
   */
  public void setOfficeHome(final @Nullable String officeHome) {
    this.officeHome = officeHome;
  }

  /**
   * Provides a custom {@link ProcessManager} implementation, which may not be included in the
   * standard JODConverter distribution.
   *
   * @param processManagerClass Type of the provided process manager. The class must implement the
   *     {@code ProcessManager} interface, must be on the classpath (or more specifically accessible
   *     from the current classloader) and must have a default public constructor (no argument).
   * @see org.jodconverter.local.process.ProcessManager
   * @see org.jodconverter.local.process.AbstractProcessManager
   */
  public void setProcessManager(final @Nullable String processManagerClass) {
    this.processManagerClass = processManagerClass;
  }

  /**
   * Specifies the directory to copy to the temporary office profile directories to be created.
   *
   * @param templateProfileDir The new template profile directory.
   */
  public void setTemplateProfileDir(final @Nullable String templateProfileDir) {
    this.templateProfileDir = templateProfileDir;
  }

  /**
   * Specifies the directory to copy to the temporary office profile directories to be created. If
   * the given templateProfileDir is not valid, it will be ignored and the default behavior will be
   * applied.
   *
   * @param templateProfileDir The new template profile directory.
   */
  public void setTemplateProfileDirOrDefault(final @Nullable String templateProfileDir) {

    this.templateProfileDir = templateProfileDir;
    this.useDefaultOnInvalidTemplateProfileDir = true;
  }

  /**
   * Specifies the timeout, in milliseconds, when trying to execute an office process call
   * (start/terminate).
   *
   * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
   *
   * @param processTimeout The process timeout, in milliseconds.
   */
  public void setProcessTimeout(final @Nullable Long processTimeout) {
    this.processTimeout = processTimeout;
  }

  /**
   * Specifies the delay, in milliseconds, between each try when trying to execute an office process
   * call (start/terminate).
   *
   * <p>&nbsp; <b><i>Default</i></b>: 250 (0.25 seconds)
   *
   * @param processRetryInterval The retry interval, in milliseconds.
   */
  public void setProcessRetryInterval(final @Nullable Long processRetryInterval) {
    this.processRetryInterval = processRetryInterval;
  }

  /**
   * Specifies the delay, in milliseconds, after an attempt to start an office process before doing
   * anything else. It is required on some OS to avoid an attempt to connect to the started process
   * that will hang for more than 5 minutes before throwing a timeout exception, we do not know why.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 0 (no delay). On FreeBSD, which is a known OS needing this, it
   * defaults to 2000 (2 seconds).
   *
   * @param afterStartProcessDelay The delay, in milliseconds.
   */
  public void setAfterStartProcessDelay(final @Nullable Long afterStartProcessDelay) {
    this.afterStartProcessDelay = afterStartProcessDelay;
  }

  /**
   * Specifies the action the must be taken when starting a new office process and there already is
   * an existing running process for the same connection string.
   *
   * <p>&nbsp; <b><i>Default</i></b>: ExistingProcessAction.KILL
   *
   * @param existingProcessAction The existing process action.
   */
  public void setExistingProcessAction(
      final @Nullable ExistingProcessAction existingProcessAction) {
    this.existingProcessAction = existingProcessAction;
  }

  /**
   * Controls whether the manager will "fail fast" if an office process cannot be started or the
   * connection to the started process fails. If set to {@code true}, the start of a process will
   * wait for the task to be completed, and will throw an exception if the office process is not
   * started successfully or if the connection to the started process fails. If set to {@code
   * false}, the task of starting the process and connecting to it will be submitted and will return
   * immediately, meaning a faster starting process. Only error logs will be produced if anything
   * goes wrong.
   *
   * <p>&nbsp; <b><i>Default</i></b>: false
   *
   * @param startFailFast {@code true} to "fail fast", {@code false} otherwise.
   */
  public void setStartFailFast(final @Nullable Boolean startFailFast) {
    this.startFailFast = startFailFast;
  }

  /**
   * Controls whether the manager will keep the office process alive on shutdown. If set to {@code
   * true}, the stop task will only disconnect from the office process, which will stay alive. If
   * set to {@code false}, the office process will be stopped gracefully (or killed if could not
   * been stopped gracefully).
   *
   * <p>&nbsp; <b><i>Default</i></b>: false
   *
   * @param keepAliveOnShutdown {@code true} to keep the process alive, {@code false} otherwise.
   */
  public void setKeepAliveOnShutdown(final @Nullable Boolean keepAliveOnShutdown) {
    this.keepAliveOnShutdown = keepAliveOnShutdown;
  }

  /**
   * Specifies whether OpenGL must be disabled when starting a new office process. Nothing will be
   * done if OpenGL is already disabled according to the user profile used with the office process.
   * If the options is changed, then office must be restarted.
   *
   * <p>&nbsp; <b><i>Default</i></b>: false
   *
   * @param disableOpengl {@code true} to disable OpenGL, {@code false} otherwise.
   */
  public void setDisableOpengl(final @Nullable Boolean disableOpengl) {
    this.disableOpengl = disableOpengl;
  }

  /**
   * Specifies the maximum number of tasks an office process can execute before restarting. 0 means
   * infinite number of task (will never restart).
   *
   * <p>&nbsp; <b><i>Default</i></b>: 200
   *
   * @param maxTasksPerProcess The new maximum number of tasks an office process can execute.
   */
  public void setMaxTasksPerProcess(final @Nullable Integer maxTasksPerProcess) {
    this.maxTasksPerProcess = maxTasksPerProcess;
  }
}
