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
 * This class holds the configuration of an {@link OfficeManagerPool}.
 *
 * @see OfficeManagerPool
 */
class OfficeManagerPoolConfig extends OfficeManagerPoolEntryConfig {

  /** The default maximum living time of a task in the conversion queue. */
  public static final long DEFAULT_TASK_QUEUE_TIMEOUT = 30000L; // 30 seconds

  private long taskQueueTimeout = DEFAULT_TASK_QUEUE_TIMEOUT;

  /**
   * Creates configuration with the specified values.
   *
   * @param officeHome The home directory of the office installation.
   * @param workingDir The working directory to set to office.
   * @param processManager The process manager to use to deal with created processes.
   */
  public OfficeManagerPoolConfig(
      final File officeHome, final File workingDir, final ProcessManager processManager) {
    super(officeHome, workingDir, processManager);
  }

  /**
   * Sets the maximum living time of a task in the conversion queue. The task will be removed from
   * the queue if the waiting time is longer than this timeout.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 30000 (30 seconds)
   *
   * @return The task queue timeout, in milliseconds.
   */
  public long getTaskQueueTimeout() {
    return taskQueueTimeout;
  }

  /**
   * Sets the maximum living time of a task in the conversion queue. The task will be removed from
   * the queue if the waiting time is longer than this timeout.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 30000 (30 seconds)
   *
   * @param taskQueueTimeout The task queue timeout, in milliseconds.
   */
  public void setTaskQueueTimeout(final long taskQueueTimeout) {
    this.taskQueueTimeout = taskQueueTimeout;
  }
}
