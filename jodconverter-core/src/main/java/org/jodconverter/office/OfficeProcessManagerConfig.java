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
 * This class holds the configuration of an {@link OfficeProcessManager}.
 *
 * @see OfficeProcessManager
 */
class OfficeProcessManagerConfig extends OfficeProcessConfig {

  /** The default timeout when executing a process call (start/terminate). */
  public static final long DEFAULT_PROCESS_TIMEOUT = 120000L; // 2 minutes
  /** The default delay between each try when executing a process call (start/terminate). */
  public static final long DEFAULT_PROCESS_RETRY_INTERVAL = 250L; // 0.25 secs.
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
}
