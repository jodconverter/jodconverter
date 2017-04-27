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

package org.jodconverter.office;

import java.io.File;

import com.sun.star.lib.uno.helper.UnoUrl;

import org.jodconverter.process.ProcessManager;

/**
 * Settings for a {@code PooledOfficeManager}.
 *
 * @see PooledOfficeManager
 */
class PooledOfficeManagerSettings extends ManagedOfficeProcessSettings {

  public static final long DEFAULT_TASK_EXECUTION_TIMEOUT = 120000L; // 2 minutes
  public static final int DEFAULT_MAX_TASK_PER_PROCESS = 200;

  private long taskExecutionTimeout;
  private int maxTasksPerProcess;

  /**
   * Creates default settings for the specified UNO URL.
   *
   * @param unoUrl the UNO URL of the settings.
   */
  public PooledOfficeManagerSettings(final UnoUrl unoUrl) {
    super(unoUrl);

    this.taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;
    this.maxTasksPerProcess = DEFAULT_MAX_TASK_PER_PROCESS;
  }

  /** Constructs new settings instance for the specified URL and with the specified values. */
  public PooledOfficeManagerSettings(
      final UnoUrl unoUrl,
      final File officeHome,
      final File workingDir,
      final ProcessManager processManager) {
    super(unoUrl, officeHome, workingDir, processManager);

    this.taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;
    this.maxTasksPerProcess = DEFAULT_MAX_TASK_PER_PROCESS;
  }

  /**
   * Gets the maximum number of tasks an office process can execute before restarting. Default is
   * 200.
   *
   * @return the maximum value.
   */
  public int getMaxTasksPerProcess() {
    return maxTasksPerProcess;
  }

  /**
   * Gets the maximum time allowed to process a task. If the processing time of a task is longer
   * than this timeout, this task will be aborted and the next task is processed. Default is 120000
   * (2 minutes).
   *
   * @return the timeout value.
   */
  public long getTaskExecutionTimeout() {
    return taskExecutionTimeout;
  }

  /**
   * Sets the maximum number of tasks an office process can execute before restarting.
   *
   * @param maxTasksPerProcess the new value to set.
   */
  public void setMaxTasksPerProcess(final int maxTasksPerProcess) {
    this.maxTasksPerProcess = maxTasksPerProcess;
  }

  /**
   * Sets the maximum time allowed to process a task. If the processing time of a task is longer
   * than this timeout, this task will be aborted and the next task is processed. Default is 120000
   * (2 minutes).
   *
   * @param taskExecutionTimeout the new timeout value.
   */
  public void setTaskExecutionTimeout(final long taskExecutionTimeout) {
    this.taskExecutionTimeout = taskExecutionTimeout;
  }
}
