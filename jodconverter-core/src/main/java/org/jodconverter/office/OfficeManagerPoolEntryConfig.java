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

import org.jodconverter.process.ProcessManager;

/**
 * This class holds the configuration of an {@link OfficeManagerPoolEntry}.
 *
 * @see OfficeManagerPoolEntry
 */
class OfficeManagerPoolEntryConfig extends OfficeProcessManagerConfig {

  /** The default timeout when processing a task. */
  public static final long DEFAULT_TASK_EXECUTION_TIMEOUT = 120000L; // 2 minutes
  /** The default maximum number of tasks an office process can execute before restarting. */
  public static final int DEFAULT_MAX_TASKS_PER_PROCESS = 200;

  private long taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;
  private int maxTasksPerProcess = DEFAULT_MAX_TASKS_PER_PROCESS;

  /** Creates configuration with default values. */
  public OfficeManagerPoolEntryConfig() {
    super();
  }

  /**
   * Creates configuration with the specified values.
   *
   * @param officeHome The home directory of the office installation.
   * @param workingDir The working directory to set to office.
   * @param processManager The process manager to use to deal with created processes.
   */
  public OfficeManagerPoolEntryConfig(
      final File officeHome, final File workingDir, final ProcessManager processManager) {
    super(officeHome, workingDir, processManager);
  }

  /**
   * Gets the maximum time allowed to process a task. If the processing time of a task is longer
   * than this timeout, this task will be aborted and the next task is processed.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
   *
   * @return The task execution timeout, in milliseconds.
   */
  public long getTaskExecutionTimeout() {
    return taskExecutionTimeout;
  }

  /**
   * Gets the maximum number of tasks an office process can execute before restarting.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 200
   *
   * @return The number of tasks an office process can execute.
   */
  public int getMaxTasksPerProcess() {
    return maxTasksPerProcess;
  }

  /**
   * Sets the maximum time allowed to process a task. If the processing time of a task is longer
   * than this timeout, this task will be aborted and the next task is processed.
   *
   * @param taskExecutionTimeout The new task execution timeout.
   */
  public void setTaskExecutionTimeout(final long taskExecutionTimeout) {
    this.taskExecutionTimeout = taskExecutionTimeout;
  }

  /**
   * Sets the maximum number of tasks an office process can execute before restarting.
   *
   * @param maxTasksPerProcess The new number of tasks an office process can execute.
   */
  public void setMaxTasksPerProcess(final int maxTasksPerProcess) {
    this.maxTasksPerProcess = maxTasksPerProcess;
  }
}
