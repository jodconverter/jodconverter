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
 * This class provides the configuration of an {@link AbstractOfficeManagerPool}.
 *
 * @see AbstractOfficeManagerPool
 */
interface OfficeManagerPoolConfig extends OfficeManagerConfig { // NOSONAR

  /** The default maximum living time of a task in the conversion queue. */
  long DEFAULT_TASK_QUEUE_TIMEOUT = 30000L; // 30 seconds

  /**
   * Sets the maximum living time of a task in the conversion queue. The task will be removed from
   * the queue if the waiting time is longer than this timeout.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 30000 (30 seconds)
   *
   * @return The task queue timeout, in milliseconds.
   */
  long getTaskQueueTimeout();

  /**
   * Sets the maximum living time of a task in the conversion queue. The task will be removed from
   * the queue if the waiting time is longer than this timeout.
   *
   * <p>&nbsp; <b><i>Default</i></b>: 30000 (30 seconds)
   *
   * @param taskQueueTimeout The task queue timeout, in milliseconds.
   */
  void setTaskQueueTimeout(final long taskQueueTimeout);
}
