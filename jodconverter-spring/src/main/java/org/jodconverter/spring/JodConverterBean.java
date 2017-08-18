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
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import org.jodconverter.DefaultConverter;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFamily;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.office.DefaultOfficeManager;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.OfficeUtils;

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
@SuppressWarnings({"PMD.AtLeastOneConstructor", "PMD.LawOfDemeter"})
public class JodConverterBean implements InitializingBean, DisposableBean {

  private static final Logger logger = LoggerFactory.getLogger(JodConverterBean.class);

  private String officeHome;
  private String portNumbers;
  private String workingDir;
  private String templateProfileDir;
  private Boolean killExistingProcess = true;
  private Long processTimeout = 120000L;
  private Long processRetryInterval = 250L;
  private Long taskExecutionTimeout = 120000L;
  private Integer maxTasksPerProcess = 200;
  private Long taskQueueTimeout = 30000L;

  private OfficeManager officeManager;
  private DocumentConverter documentConverter;

  @Override
  public void afterPropertiesSet() throws OfficeException { // NOSONAR

    final DefaultOfficeManager.Builder builder = DefaultOfficeManager.builder();

    if (!StringUtils.isBlank(portNumbers)) {
      final Set<Integer> iports = new HashSet<>();
      for (final String portNumber : StringUtils.split(portNumbers, ", ")) {
        iports.add(NumberUtils.toInt(portNumber, 2002));
      }
      builder.portNumbers(ArrayUtils.toPrimitive(iports.toArray(new Integer[iports.size()])));
    }

    builder.officeHome(officeHome);
    builder.workingDir(workingDir);
    builder.templateProfileDir(templateProfileDir);
    builder.killExistingProcess(killExistingProcess);
    builder.processTimeout(processTimeout);
    builder.processRetryInterval(processRetryInterval);
    builder.taskExecutionTimeout(taskExecutionTimeout);
    builder.maxTasksPerProcess(maxTasksPerProcess);
    builder.taskQueueTimeout(taskQueueTimeout);

    // Starts the manager
    officeManager = builder.build();
    documentConverter = DefaultConverter.make(officeManager);
    officeManager.start();
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

    logger.info(text);
    for (final DocumentFormat format : formats) {
      logger.info(format.getName());
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
