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

package org.jodconverter.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration class for JODConverter. */
@ConfigurationProperties(prefix = "jodconverter")
public class JodConverterProperties {

  /**
   * Enable JODConverter, which means that office instances will be launched. If not set, it
   * defaults to false.
   */
  private boolean enabled;

  /**
   * Represents the office home directory. If not set, it defaults to an auto detected home
   * directory (most recent version first).
   */
  private String officeHome;

  /**
   * List of ports, separated by commas, used by each JODConverter processing thread. The number of
   * office instances is equal to the number of ports, since 1 office will be launched for each port
   * number. If not set, it defaults to a list with a single port, 2002.
   */
  private String portNumbers = "2002";

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
   * Retry timeout (milliseconds).Used for retrying office process calls (start/terminate). If not
   * set, it defaults to 120000 (2 minutes).
   */
  private long retryTimeout = 120000L;

  /**
   * Retry interval (milliseconds).Used for waiting between office process call tries
   * (start/terminate). If not set, it defaults to 250.
   */
  private long retryInterval = 250;

  /**
   * Indicates whether we must kill existing office process when an office process already exists
   * for the same connection string. If not set, it defaults to true.
   */
  private boolean killExistingProcess = true;

  /**
   * Maximum living time of a task in the conversion queue. The task will be removed from the queue
   * if the waiting time is longer than this timeout. If not set, it defaults to 30000 (30 seconds).
   */
  private long taskQueueTimeout = 30000L;

  /**
   * Maximum time allowed to process a task. If the processing time of a task is longer than this
   * timeout, this task will be aborted and the next task is processed. If not set, it defaults to
   * 120000 (2 minutes).
   */
  private long taskExecutionTimeout = 120000L;

  /**
   * Maximum number of tasks an office process can execute before restarting. If not set, it
   * defaults to 200.
   */
  private int maxTasksPerProcess = 200;

  public boolean isEnabled() {
    return enabled;
  }

  public String getOfficeHome() {
    return officeHome;
  }

  public void setOfficeHome(final String officeHome) {
    this.officeHome = officeHome;
  }

  public String getPortNumbers() {
    return portNumbers;
  }

  public void setPortNumbers(final String portNumbers) {
    this.portNumbers = portNumbers;
  }

  public String getWorkingDir() {
    return workingDir;
  }

  public void setWorkingDir(final String workingDir) {
    this.workingDir = workingDir;
  }

  public String getTemplateProfileDir() {
    return templateProfileDir;
  }

  public void setTemplateProfileDir(final String templateProfileDir) {
    this.templateProfileDir = templateProfileDir;
  }

  public long getRetryTimeout() {
    return retryTimeout;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public void setRetryTimeout(final long retryTimeout) {
    this.retryTimeout = retryTimeout;
  }

  public long getRetryInterval() {
    return retryInterval;
  }

  public void setRetryInterval(final long retryInterval) {
    this.retryInterval = retryInterval;
  }

  public boolean isKillExistingProcess() {
    return killExistingProcess;
  }

  public void setKillExistingProcess(final boolean killExistingProcess) {
    this.killExistingProcess = killExistingProcess;
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
}
