/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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
 * This class holds the configuration of an {@link OfficeProcessManager}.
 *
 * @see OfficeProcessManager
 */
class OfficeProcessManagerConfig extends OfficeProcessConfig {

  /** The default timeout when executing a process call (start/terminate). */
  public static final long DEFAULT_PROCESS_TIMEOUT = 120000L; // 2 minutes
  /** The default delay between each try when executing a process call (start/terminate). */
  public static final long DEFAULT_PROCESS_RETRY_INTERVAL = 250L; // 0.25 secs.
  /** The default maximum number of tasks an office process can execute before restarting. */
  public static final int DEFAULT_MAX_TASKS_PER_PROCESS = 200;
  /** The default behavior when an office process is started regarding to OpenGL usage. */
  public static final boolean DEFAULT_DISABLE_OPENGL = false;

  /**
   * The minimum value for the delay between each try when executing a process call
   * (start/terminate).
   */
  public static final long MIN_PROCESS_RETRY_INTERVAL = 0L; // No delay.
  /**
   * The maximum value for the delay between each try when executing a process call
   * (start/terminate).
   */
  public static final long MAX_PROCESS_RETRY_INTERVAL = 10000L; // 10 sec.

  private long processTimeout = DEFAULT_PROCESS_TIMEOUT;
  private long processRetryInterval = DEFAULT_PROCESS_RETRY_INTERVAL;
  private int maxTasksPerProcess = DEFAULT_MAX_TASKS_PER_PROCESS;
  private boolean disableOpengl = DEFAULT_DISABLE_OPENGL;

  /** Creates configuration with default values. */
  public OfficeProcessManagerConfig() {
    super();
  }

  /**
   * Creates configuration with the specified values.
   *
   * @param officeHome The home directory of the office installation.
   * @param workingDir The working directory to set to office.
   * @param processManager The process manager to use to deal with created processes.
   */
  public OfficeProcessManagerConfig(
      final File officeHome, final File workingDir, final ProcessManager processManager) {
    super(officeHome, workingDir, processManager);
  }

  /**
   * Gets the timeout, in milliseconds, when trying to execute an office process call
   * (start/terminate).
   *
   * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
   *
   * @return The process timeout, in milliseconds.
   */
  public long getProcessTimeout() {
    return processTimeout;
  }

  /**
   * Gets the delay, in milliseconds, between each try when trying to execute an office process call
   * (start/terminate).
   *
   * <p>&nbsp; <b><i>Default</i></b>: 250 (0.25 seconds)
   *
   * @return The retry interval, in milliseconds.
   */
  public long getProcessRetryInterval() {
    return processRetryInterval;
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
   * Gets whether OpenGL must be disabled when starting a new office process.
   *
   * <p>&nbsp; <b><i>Default</i></b>: false
   *
   * @return {@code true} to disable OpenGL, {@code false} otherwise.
   */
  public boolean isDisableOpengl() {
    return disableOpengl;
  }

  /**
   * Sets the timeout, in milliseconds, when trying to execute an office process call
   * (start/terminate).
   *
   * @param processTimeout The new process timeout, in milliseconds.
   */
  public void setProcessTimeout(final long processTimeout) {
    this.processTimeout = processTimeout;
  }

  /**
   * Sets the delay, in milliseconds, between each try when trying to execute an office process call
   * (start/terminate).
   *
   * @param processRetryInterval The new process retry interval, in milliseconds.
   */
  public void setProcessRetryInterval(final long processRetryInterval) {
    this.processRetryInterval = processRetryInterval;
  }

  /**
   * Sets the maximum number of tasks an office process can execute before restarting.
   *
   * @param maxTasksPerProcess The new number of tasks an office process can execute.
   */
  public void setMaxTasksPerProcess(final int maxTasksPerProcess) {
    this.maxTasksPerProcess = maxTasksPerProcess;
  }

  /**
   * Gets whether OpenGL must be disabled when starting a new office process. Nothing will be done
   * if OpenGL is already disabled according to the user profile used with the office process. If
   * the option is changed, then office must be restarted.
   *
   * @param disableOpengl {@code true} to disable OpenGL, {@code false} otherwise.
   */
  public void setDisableOpengl(final boolean disableOpengl) {
    this.disableOpengl = disableOpengl;
  }
}
