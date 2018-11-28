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

import org.jodconverter.task.OfficeTask;

/**
 * This class holds the configuration of an {@link ExternalOfficeManager}.
 *
 * @see ExternalOfficeManager
 */
class ExternalOfficeManagerConfig implements OfficeManagerConfig {

  private File workingDir;
  private final boolean connectOnStart;
  private final long connectTimeout;
  private final long retryInterval;

  /**
   * Creates configuration with the specified values.
   *
   * @param workingDir The working directory.
   * @param connectOnStart Should a connection be attempted on {@link #start()}? If <em>false</em>,
   *     a connection will only be attempted the first time an {@link OfficeTask} is executed.
   * @param connectTimeout Timeout after which a connection attempt will fail.
   * @param retryInterval Timeout between each try to connect.
   */
  public ExternalOfficeManagerConfig(
      final File workingDir,
      final boolean connectOnStart,
      final long connectTimeout,
      final long retryInterval) {

    this.workingDir =
        workingDir == null ? new File(System.getProperty("java.io.tmpdir")) : workingDir;
    this.connectOnStart = connectOnStart;
    this.connectTimeout = connectTimeout;
    this.retryInterval = retryInterval;
  }

  @Override
  public File getWorkingDir() {
    return workingDir;
  }

  /**
   * Gets whether a connection is attempted on start.
   *
   * @return @code true} if a connection should be attempted on start, {@code false} otherwise.
   */
  public boolean isConnectOnStart() {
    return connectOnStart;
  }

  /**
   * Gets the timeout after which a connection attempt will fail.
   *
   * @return The connect timeout.
   */
  public long getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Gets the delay, in milliseconds, between each try when trying to connect to office.
   *
   * @return The retry interval.
   */
  public long getRetryInterval() {
    return retryInterval;
  }

  @Override
  public void setWorkingDir(final File workingDir) {
    this.workingDir = workingDir;
  }
}
