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

package org.jodconverter.office;

import java.io.File;

import com.sun.star.lib.uno.helper.UnoUrl;

import org.jodconverter.process.ProcessManager;

/**
 * Configuration of a {@code OfficeManagerPoolEntry}.
 *
 * @see OfficeManagerPoolEntry
 */
class OfficeManagerPoolEntryConfig extends OfficeProcessManagerConfig {

  public static final long DEFAULT_TASK_EXECUTION_TIMEOUT = 120000L; // 2 minutes
  public static final int DEFAULT_MAX_TASK_PER_PROCESS = 200;

  private long taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;
  private int maxTasksPerProcess = DEFAULT_MAX_TASK_PER_PROCESS;

  /**
   * Creates configuration for the specified URL and with default values.
   *
   * @param unoUrl the UNO URL for the configuration.
   */
  public OfficeManagerPoolEntryConfig(final UnoUrl unoUrl) {
    super(unoUrl);
  }

  /**
   * Creates configuration for the specified URL and with the specified values.
   *
   * @param unoUrl the UNO URL for the configuration.
   * @param officeHome home directory of the office installation.
   * @param workingDir working directory to set to office.
   * @param processManager process manager to use to deal with created processes.
   */
  public OfficeManagerPoolEntryConfig(
      final UnoUrl unoUrl,
      final File officeHome,
      final File workingDir,
      final ProcessManager processManager) {
    super(unoUrl, officeHome, workingDir, processManager);
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
   * Gets the maximum number of tasks an office process can execute before restarting. Default is
   * 200.
   *
   * @return the maximum value.
   */
  public int getMaxTasksPerProcess() {
    return maxTasksPerProcess;
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

  /**
   * Sets the maximum number of tasks an office process can execute before restarting.
   *
   * @param maxTasksPerProcess the new value to set.
   */
  public void setMaxTasksPerProcess(final int maxTasksPerProcess) {
    this.maxTasksPerProcess = maxTasksPerProcess;
  }
}
