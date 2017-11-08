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

/**
 * This class provides the configuration of an {@link AbstractOfficeManagerPool} when no office
 * instance are required to execute conversion.
 *
 * @see AbstractOfficeManagerPool
 */
class SimpleOfficeManagerPoolConfig extends SimpleOfficeManagerPoolEntryConfig
    implements OfficeManagerPoolConfig {

  private long taskQueueTimeout = DEFAULT_TASK_QUEUE_TIMEOUT;
  private File workingDir;

  /**
   * Creates configuration with the specified values.
   *
   * @param workingDir The working directory to set to office.
   */
  public SimpleOfficeManagerPoolConfig(final File workingDir) {
    super();

    this.workingDir = workingDir;
  }

  @Override
  public long getTaskQueueTimeout() {
    return taskQueueTimeout;
  }

  @Override
  public File getWorkingDir() {
    return workingDir;
  }

  @Override
  public void setTaskQueueTimeout(final long taskQueueTimeout) {
    this.taskQueueTimeout = taskQueueTimeout;
  }

  @Override
  public void setWorkingDir(final File workingDir) {
    this.workingDir = workingDir;
  }
}
