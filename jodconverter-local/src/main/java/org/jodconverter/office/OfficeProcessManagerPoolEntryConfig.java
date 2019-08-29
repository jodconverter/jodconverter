/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2019 Simon Braconnier and contributors
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
 * This class holds the configuration of an {@link OfficeProcessManagerPoolEntry} when an office
 * instance is required to execute a conversion.
 *
 * @see OfficeProcessManagerConfig
 * @see OfficeProcessManagerPoolEntry
 */
class OfficeProcessManagerPoolEntryConfig extends OfficeProcessManagerConfig
    implements OfficeManagerPoolEntryConfig {

  private long taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;

  /** Creates configuration with default values. */
  public OfficeProcessManagerPoolEntryConfig() {
    super();
  }

  /**
   * Creates configuration with the specified values.
   *
   * @param officeHome The home directory of the office installation.
   * @param workingDir The working directory to set to office.
   * @param processManager The process manager to use to deal with created processes.
   */
  public OfficeProcessManagerPoolEntryConfig(
      final File officeHome, final File workingDir, final ProcessManager processManager) {
    super(officeHome, workingDir, processManager);
  }

  @Override
  public long getTaskExecutionTimeout() {
    return taskExecutionTimeout;
  }

  @Override
  public void setTaskExecutionTimeout(final long taskExecutionTimeout) {
    this.taskExecutionTimeout = taskExecutionTimeout;
  }
}
