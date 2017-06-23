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
 * This class holds the configuration of a {@code OfficeProcessManager}.
 *
 * @see OfficeProcessManager
 */
class OfficeProcessManagerConfig extends OfficeProcessConfig {

  public static final long DEFAULT_INTERVAL = 250L; // 0.25 secs.
  public static final long DEFAULT_TIMEOUT = 120000L; // 2 minutes

  public static final long MAX_RETRY_INTERVAL = 10000L; // 10 sec.

  private long interval = DEFAULT_INTERVAL;
  private long timeout = DEFAULT_TIMEOUT;

  /**
   * Creates configuration for the specified URL and with default values.
   *
   * @param officeUrl the office URL for the configuration.
   */
  public OfficeProcessManagerConfig(final OfficeUrl officeUrl) {
    super(officeUrl);
  }

  /**
   * Creates configuration for the specified URL and with the specified values.
   *
   * @param officeUrl the office URL for the configuration.
   * @param officeHome home directory of the office installation.
   * @param workingDir working directory to set to office.
   * @param processManager process manager to use to deal with created processes.
   */
  public OfficeProcessManagerConfig(
      final OfficeUrl officeUrl,
      final File officeHome,
      final File workingDir,
      final ProcessManager processManager) {
    super(officeUrl, officeHome, workingDir, processManager);
  }

  /**
   * Get the retry interval (milliseconds). Used for waiting between task execution tries (e.g,
   * start/terminate office process). Default is 250.
   *
   * @return the retry interval, in milliseconds.
   */
  public long getRetryInterval() {
    return interval;
  }

  /**
   * Set the retry timeout (milliseconds). Used for retrying task execution (e.g, start/terminate
   * office process). If not set, it defaults to 2 minutes.
   *
   * @return the retry timeout, in milliseconds.
   */
  public long getRetryTimeout() {
    return timeout;
  }

  /**
   * Set the retry interval (milliseconds).Used for waiting between task execution tries (e.g,
   * start/terminate office process).
   *
   * @param retryInterval the retry interval, in milliseconds.
   */
  public void setRetryInterval(final long retryInterval) {
    this.interval = retryInterval;
  }

  /**
   * Set the retry timeout (milliseconds). Used for retrying task execution (e.g, start/terminate
   * office process).
   *
   * @param retryTimeout the retry timeout, in milliseconds.
   */
  public void setRetryTimeout(final long retryTimeout) {
    this.timeout = retryTimeout;
  }
}
