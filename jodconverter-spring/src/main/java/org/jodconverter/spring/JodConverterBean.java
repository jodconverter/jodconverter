/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.jodconverter.spring;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import org.jodconverter.OfficeDocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFamily;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.DefaultOfficeManagerBuilder;
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
  private Long retryTimeout;
  private Long retryInterval;
  private Boolean killExistingProcess;
  private Long taskQueueTimeout;
  private Long taskExecutionTimeout;
  private Integer maxTasksPerProcess;

  private OfficeManager officeManager;
  private OfficeDocumentConverter documentConverter;

  @Override
  public void afterPropertiesSet() throws OfficeException { // NOSONAR

    final DefaultOfficeManagerBuilder builder = new DefaultOfficeManagerBuilder();

    if (!StringUtils.isBlank(officeHome)) {
      builder.setOfficeHome(officeHome);
    }

    if (!StringUtils.isBlank(workingDir)) {
      builder.setWorkingDir(new File(workingDir));
    }

    final Set<Integer> ports = buildPortNumbers(this.portNumbers);
    if (!ports.isEmpty()) {
      builder.setPortNumbers(ArrayUtils.toPrimitive(ports.toArray(new Integer[] {})));
    }

    if (!StringUtils.isBlank(templateProfileDir)) {
      builder.setTemplateProfileDir(new File(templateProfileDir));
    }

    if (retryTimeout != null) {
      builder.setRetryTimeout(retryTimeout);
    }

    if (retryInterval != null) {
      builder.setRetryInterval(retryInterval);
    }

    if (killExistingProcess != null) {
      builder.setKillExistingProcess(killExistingProcess);
    }

    if (taskQueueTimeout != null) {
      builder.setTaskQueueTimeout(taskQueueTimeout);
    }

    if (taskExecutionTimeout != null) {
      builder.setTaskExecutionTimeout(taskExecutionTimeout);
    }

    if (maxTasksPerProcess != null) {
      builder.setMaxTasksPerProcess(maxTasksPerProcess);
    }

    // Starts the manager
    officeManager = builder.build();
    documentConverter = new OfficeDocumentConverter(officeManager);
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
   * Converts an input file to an output file. The files extensions are used to determine the input
   * and output {@link DocumentFormat}.
   *
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(final File inputFile, final File outputFile) throws OfficeException {

    documentConverter.convert(inputFile, outputFile);
  }

  /**
   * Converts an input file to an output file. The input file extension is used to determine the
   * input {@link DocumentFormat}.
   *
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @param outputFormat the target output format.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(
      final File inputFile, final File outputFile, final DocumentFormat outputFormat)
      throws OfficeException {

    documentConverter.convert(inputFile, outputFile, outputFormat);
  }

  /**
   * Converts an input file to an output file.
   *
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @param inputFormat the source input format.
   * @param outputFormat the target output format.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(
      final File inputFile,
      final File outputFile,
      final DocumentFormat inputFormat,
      final DocumentFormat outputFormat)
      throws OfficeException {

    documentConverter.convert(inputFile, outputFile, inputFormat, outputFormat);
  }

  /**
   * Converts an input file to an output file. The files extensions are used to determine the input
   * and output {@link DocumentFormat}.
   *
   * @param filterChain the FilterChain to be applied after the document is loaded and before it is
   *     stored (converted) in the new document format. A FilterChain is used to modify the document
   *     before the conversion. Filters are applied in the same order they appear in the chain.
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(final FilterChain filterChain, final File inputFile, final File outputFile)
      throws OfficeException {

    documentConverter.convert(filterChain, inputFile, outputFile);
  }

  /**
   * Converts an input file to an output file. The input file extension is used to determine the
   * input {@link DocumentFormat}.
   *
   * @param filterChain the FilterChain to be applied after the document is loaded and before it is
   *     stored (converted) in the new document format. A FilterChain is used to modify the document
   *     before the conversion. Filters are applied in the same order they appear in the chain.
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @param outputFormat the target output format.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(
      final FilterChain filterChain,
      final File inputFile,
      final File outputFile,
      final DocumentFormat outputFormat)
      throws OfficeException {

    documentConverter.convert(filterChain, inputFile, outputFile, outputFormat);
  }

  /**
   * Converts an input file to an output file.
   *
   * @param filterChain the FilterChain to be applied after the document is loaded and before it is
   *     stored (converted) in the new document format. A FilterChain is used to modify the document
   *     before the conversion. Filters are applied in the same order they appear in the chain.
   * @param inputFile the input file to convert.
   * @param outputFile the target output file.
   * @param inputFormat the source input format.
   * @param outputFormat the target output format.
   * @throws OfficeException if the conversion fails.
   */
  public void convert(
      final FilterChain filterChain,
      final File inputFile,
      final File outputFile,
      final DocumentFormat inputFormat,
      final DocumentFormat outputFormat)
      throws OfficeException {

    documentConverter.convert(filterChain, inputFile, outputFile, inputFormat, outputFormat);
  }

  @Override
  public void destroy() throws OfficeException {

    if (officeManager != null) {
      officeManager.stop();
    }
  }

  /**
   * Gets whether we must kill existing office process when an office process already exists for the
   * same connection string. If not set, it defaults to true.
   *
   * @return {@code true} to kill existing process, {@code false} otherwise.
   */
  public Boolean getKillExistingProcess() {
    return killExistingProcess;
  }

  /**
   * Gets the maximum number of tasks an office process can execute before restarting. Default is
   * 200.
   *
   * @return the maximum value.
   */
  public Integer getMaxTasksPerProcess() {
    return maxTasksPerProcess;
  }

  /**
   * Gets the office home directory.
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
   * Get the retry interval (milliseconds).Used for waiting between office process call tries
   * (start/terminate). Default is 250.
   *
   * @return the retry interval, in milliseconds.
   */
  public Long getRetryInterval() {
    return retryInterval;
  }

  /**
   * Set the retry timeout (milliseconds).Used for retrying office process calls (start/terminate).
   * If not set, it defaults to 2 minutes.
   *
   * @return the retry timeout, in milliseconds.
   */
  public Long getRetryTimeout() {
    return retryTimeout;
  }

  /**
   * Gets the maximum time allowed to process a task. If the processing time of a task is longer
   * than this timeout, this task will be aborted and the next task is processed. Default is 120000
   * (2 minutes).
   *
   * @return the timeout value.
   */
  public Long getTaskExecutionTimeout() {
    return taskExecutionTimeout;
  }

  /**
   * Gets the maximum living time of a task in the conversion queue. The task will be removed from
   * the queue if the waiting time is longer than this timeout. Default is 30000 (30 seconds).
   *
   * @return timeout value.
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
   * Sets whether we must kill existing office process when an office process already exists for the
   * same connection string.
   *
   * @param killExistingProcess {@code true} to kill existing process, {@code false} otherwise.
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
   * Sets the office home directory.
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
   * Set the retry interval (milliseconds).Used for waiting between office process call tries
   * (start/terminate).
   *
   * @param retryInterval the retry interval, in milliseconds.
   */
  public void setRetryInterval(final Long retryInterval) {
    this.retryInterval = retryInterval;
  }

  /**
   * Set the retry timeout (milliseconds). Used for retrying office process calls (start/terminate).
   *
   * @param retryTimeout the retry timeout, in milliseconds.
   */
  public void setRetryTimeout(final Long retryTimeout) {
    this.retryTimeout = retryTimeout;
  }

  /**
   * Sets the maximum time allowed to process a task. If the processing time of a task is longer
   * than this timeout, this task will be aborted and the next task is processed. Default is 120000
   * (2 minutes).
   *
   * @param taskExecutionTimeout the new timeout value.
   */
  public void setTaskExecutionTimeout(final Long taskExecutionTimeout) {
    this.taskExecutionTimeout = taskExecutionTimeout;
  }

  /**
   * Sets the maximum living time of a task in the conversion queue. The task will be removed from
   * the queue if the waiting time is longer than this timeout.
   *
   * @param taskQueueTimeout the new timeout value.
   */
  public void setTaskQueueTimeout(final Long taskQueueTimeout) {
    this.taskQueueTimeout = taskQueueTimeout;
  }

  /**
   * Sets the directory to copy to the temporary office profile directories to be created.
   *
   * @param templateProfileDir the new template profile directory.
   */
  public void setTemplateProfileDir(final String templateProfileDir) {
    this.templateProfileDir = templateProfileDir;
  }

  /**
   * Sets the directory where temporary office profiles will be created.
   *
   * @param workingDir the new working directory.
   */
  public void setWorkingDir(final String workingDir) {
    this.workingDir = workingDir;
  }
}
