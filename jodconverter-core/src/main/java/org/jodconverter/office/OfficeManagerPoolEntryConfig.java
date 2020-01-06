/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

/**
 * This interface provides the configuration of an {@link OfficeProcessManagerPoolEntry}.
 *
 * @see OfficeProcessManagerPoolEntry
 */
interface OfficeManagerPoolEntryConfig {

  /** The default timeout when processing a task. */
  long DEFAULT_TASK_EXECUTION_TIMEOUT = 120000L; // 2 minutes

  /**
   * Gets the maximum time allowed to process a task. If the processing time of a task is longer
   * than this timeout, this task will be aborted and the next task is processed.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
   *
   * @return The task execution timeout, in milliseconds.
   */
  long getTaskExecutionTimeout();

  /**
   * Sets the maximum time allowed to process a task. If the processing time of a task is longer
   * than this timeout, this task will be aborted and the next task is processed.
   *
   * @param taskExecutionTimeout The new task execution timeout.
   */
  void setTaskExecutionTimeout(final long taskExecutionTimeout);
}
