/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import org.jodconverter.DefaultConverter;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFamily;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.DefaultOfficeManager;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;

/**
 * The purpose of this class is to provide to the Spring Container a Bean that encapsulates the
 * functionality already present in the JODConverter-CORE library. The target of this bean is to
 * provide the functionality of the PocessPoolOfficeManager.
 *
 * <p>The Controller shall launch the OO processes. The Controller shall stop the OO processes when
 * it´s time to shutdown the application
 *
 * @author Jose Luis López López
 */
public class JodConverterBean implements InitializingBean, DisposableBean {

  private static final Logger logger = LoggerFactory.getLogger(JodConverterBean.class);

  private String officeHome;
  private String portNumbers;
  private String workingDir;
  private String templateProfileDir;
  private Boolean killExistingProcess;
  private Long processTimeout;
  private Long processRetryInterval;
  private Long taskExecutionTimeout;
  private Integer maxTasksPerProcess;
  private Long taskQueueTimeout;

  private OfficeManager officeManager;
  private DocumentConverter documentConverter;

  @Override
  public void afterPropertiesSet() throws OfficeException { // NOSONAR

    final DefaultOfficeManager.Builder builder = DefaultOfficeManager.builder();

    builder.officeHome(officeHome);
    builder.workingDir(workingDir);

    final Set<Integer> ports = buildPortNumbers(this.portNumbers);
    if (!ports.isEmpty()) {
      builder.portNumbers(ArrayUtils.toPrimitive(ports.toArray(new Integer[] {})));
    }

    builder.templateProfileDir(templateProfileDir);

    if (killExistingProcess != null) {
      builder.killExistingProcess(killExistingProcess);
    }

    if (processTimeout != null) {
      builder.processTimeout(processTimeout);
    }

    if (processRetryInterval != null) {
      builder.processRetryInterval(processRetryInterval);
    }

    if (taskExecutionTimeout != null) {
      builder.taskExecutionTimeout(taskExecutionTimeout);
    }

    if (maxTasksPerProcess != null) {
      builder.maxTasksPerProcess(maxTasksPerProcess);
    }

    if (taskQueueTimeout != null) {
      builder.taskQueueTimeout(taskQueueTimeout);
    }

    // Starts the manager
    officeManager = builder.build();
    documentConverter = DefaultConverter.make(officeManager);
    officeManager.start();
  }

  // Create a set of port numbers from a string
  private Set<Integer> buildPortNumbers(final String str) {

    final Set<Integer> iports = new HashSet<>();

    if (StringUtils.isBlank(str)) {
      return iports;
    }

    final String[] portNums = StringUtils.split(str, ", ");
    if (portNums.length == 0) {
      return iports;
    }

    for (final String portNumber : portNums) {
      if (!StringUtils.isBlank(portNumber)) {
        iports.add(Integer.parseInt(StringUtils.trim(portNumber)));
      }
    }
    return iports;
  }

  /**
   * Gets the {@link DocumentConverter} created by this bean.
   *
   * @return The converter created by this bean.
   */
  public DocumentConverter getConverter() {

    return documentConverter;
  }

  @Override
  public void destroy() throws OfficeException {

    if (officeManager != null) {
      officeManager.stop();
    }
  }

  /**
   * Gets whether an existing office process is killed when starting a new office process for the
   * same connection string.
   *
   * <p>&nbsp; <b><i>Default</i></b>: true
   *
   * @return {@code true} to kill existing process when a new process must be created with the same
   *     connection string, {@code false} otherwise.
   */
  public Boolean getKillExistingProcess() {
    return killExistingProcess;
  }

  /**
   * Gets the maximum number of tasks an office process can execute before restarting.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 200
   *
   * @return the maximum value.
   */
  public Integer getMaxTasksPerProcess() {
    return maxTasksPerProcess;
  }

  /**
   * Gets the office home directory (office installation).
   *
   * @return the office home directory.
   */
  public String getOfficeHome() {
    return officeHome;
  }

  /**
   * Gets the list of ports, separated by commas, used by each JODConverter processing thread. The
   * number of office instances is equal to the number of ports, since 1 office will be launched for
   * each port number.
   *
   * @return the port numbers to use.
   */
  public String getPortNumbers() {
    return portNumbers;
  }

  /**
   * Gets the delay, in milliseconds, between each try when trying to execute an office process call
   * (start/terminate).
   *
   * <p>&nbsp; <b><i>Default</i></b>: 250 (0.25 seconds)
   *
   * @return the retry interval, in milliseconds.
   */
  public Long getProcessRetryInterval() {
    return processRetryInterval;
  }

  /**
   * Gets the timeout, in milliseconds, when trying to execute an office process call
   * (start/terminate).
   *
   * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
   *
   * @return the retry timeout, in milliseconds.
   */
  public Long getProcessTimeout() {
    return processTimeout;
  }

  /**
   * Gets the maximum time allowed to process a task. If the processing time of a task is longer
   * than this timeout, this task will be aborted and the next task is processed.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
   *
   * @return the timeout value.
   */
  public Long getTaskExecutionTimeout() {
    return taskExecutionTimeout;
  }

  /**
   * Gets the maximum living time of a task in the conversion queue. The task will be removed from
   * the queue if the waiting time is longer than this timeout.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 30000 (30 seconds)
   * 
   * @return the task queue timeout.
   */
  public Long getTaskQueueTimeout() {
    return taskQueueTimeout;
  }

  /**
   * Gets the directory to copy to the temporary office profile directories to be created.
   *
   * @return the template profile directory.
   */
  public String getTemplateProfileDir() {
    return templateProfileDir;
  }

  /**
   * Gets the directory where temporary office profiles will be created.
   *
   * <p>Defaults to the system temporary directory as specified by the <code>java.io.tmpdir</code>
   * system property.
   *
   * @return the working directory.
   */
  public String getWorkingDir() {
    return workingDir;
  }

  /**
   * Gets whether we must kill existing office process when an office process already exists for the
   * same connection string. If not set, it defaults to true.
   *
   * @return {@code true} to kill existing process, {@code false} otherwise.
   */
  public boolean isKillExistingProcess() {
    return killExistingProcess;
  }

  /** Prints the available formats provided by the JODConverter module. */
  public void logAvailableFormats() {

    final DefaultDocumentFormatRegistry ref = DefaultDocumentFormatRegistry.getInstance();

    logSupportedGroupFormats(
        "Supported Text Document Formats are:", ref.getOutputFormats(DocumentFamily.TEXT));
    logSupportedGroupFormats(
        "Supported SpreadSheet Document Formats are:",
        ref.getOutputFormats(DocumentFamily.SPREADSHEET));
    logSupportedGroupFormats(
        "Supported Presentation Document Formats are:",
        ref.getOutputFormats(DocumentFamily.PRESENTATION));
    logSupportedGroupFormats(
        "Supported Drawing Document Formats are:", ref.getOutputFormats(DocumentFamily.DRAWING));
  }

  /** Prints the available formats provided by the JODConverter module. */
  private void logSupportedGroupFormats(final String text, final Set<DocumentFormat> formats) {

    logger.info(text);
    final Iterator<DocumentFormat> iter = formats.iterator();
    while (iter.hasNext()) {
      logger.info(iter.next().getName());
    }
  }

  /**
   * Sets whether an existing office process is killed when starting a new office process for the
   * same connection string.
   *
   * <p>&nbsp; <b><i>Default</i></b>: true
   *
   * @param killExistingProcess {@code true} to kill existing process when a new process must be
   *     created with the same connection string, {@code false} otherwise.
   */
  public void setKillExistingProcess(final Boolean killExistingProcess) {
    this.killExistingProcess = killExistingProcess;
  }

  /**
   * Sets the maximum number of tasks an office process can execute before restarting.
   *
   * @param maxTasksPerProcess the new value to set.
   */
  public void setMaxTasksPerProcess(final Integer maxTasksPerProcess) {
    this.maxTasksPerProcess = maxTasksPerProcess;
  }

  /**
   * Sets the office home directory (office installation).
   *
   * @param officeHome the new home directory to set.
   */
  public void setOfficeHome(final String officeHome) {
    this.officeHome = officeHome;
  }

  /**
   * Sets the list of ports, separated by commas, used by each JODConverter processing thread. The
   * number of office instances is equal to the number of ports, since 1 office will be launched for
   * each port number.
   *
   * @param portNumbers the port numbers to use.
   */
  public void setPortNumbers(final String portNumbers) {
    this.portNumbers = portNumbers;
  }

  /**
   * Specifies the delay, in milliseconds, between each try when trying to execute an office process
   * call (start/terminate).
   *
   * <p>&nbsp; <b><i>Default</i></b>: 250 (0.25 seconds)
   *
   * @param processRetryInterval the retry interval, in milliseconds.
   */
  public void setProcessRetryInterval(final Long processRetryInterval) {
    this.processRetryInterval = processRetryInterval;
  }

  /**
   * Sets the timeout, in milliseconds, when trying to execute an office process call
   * (start/terminate).
   *
   * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
   *
   * @param processTimeout the process timeout, in milliseconds.
   */
  public void setProcessTimeout(final Long processTimeout) {
    this.processTimeout = processTimeout;
  }

  /**
   * Sets the maximum time allowed to process a task. If the processing time of a task is longer
   * than this timeout, this task will be aborted and the next task is processed.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
   *
   * @param taskExecutionTimeout The task execution timeout, in milliseconds.
   */
  public void setTaskExecutionTimeout(final Long taskExecutionTimeout) {
    this.taskExecutionTimeout = taskExecutionTimeout;
  }

  /**
   * Sets the maximum living time of a task in the conversion queue. The task will be removed from
   * the queue if the waiting time is longer than this timeout.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 30000 (30 seconds)
   *
   * @param taskQueueTimeout The task queue timeout, in milliseconds.
   */
  public void setTaskQueueTimeout(final Long taskQueueTimeout) {
    this.taskQueueTimeout = taskQueueTimeout;
  }

  /**
   * Sets the directory to copy to the temporary office profile directories to be created.
   *
   * @param templateProfileDir The new template profile directory.
   */
  public void setTemplateProfileDir(final String templateProfileDir) {
    this.templateProfileDir = templateProfileDir;
  }

  /**
   * Sets the directory where temporary office profile directories will be created. An office
   * profile directory is created per office process launched.
   *
   * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
   * java.io.tmpdir</code> system property.
   *
   * @param workingDir The new working directory to set.
   */
  public void setWorkingDir(final String workingDir) {
    this.workingDir = workingDir;
  }
}
