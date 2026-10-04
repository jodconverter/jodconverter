/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

package org.jodconverter.core.office;

/**
 * An interface that provides capabilities to restart office processes in a pool. This is
 * particularly useful for managing the lifecycle of office processes, allowing for the restart of
 * idle processes to ensure optimal performance and resource management. The implementation of this
 * interface can vary based on the underlying office suite and the specific requirements of the
 * application, but it generally includes methods to restart idle processes and to retrieve the
 * count of idle and busy processes in the pool.
 */
public interface OfficeRestarter {

  /**
   * Restarts all idle processes in the pool. Only processes that are not currently handling tasks
   * will be restarted.
   *
   * <p>The restart behavior depends on the configured {@link
   * org.jodconverter.local.office.RestartStrategy}. With automatic restart strategy, processes
   * restart immediately in background threads. With manual restart strategy, restarts are queued
   * and must be triggered externally.
   *
   * @return The number of idle processes that were requested to restart.
   */
  int restartIdleProcesses();

  /**
   * Gets the number of idle (available) processes in the pool.
   *
   * @return The count of idle processes.
   */
  int getIdleCount();

  /**
   * Gets the number of busy (unavailable) processes in the pool.
   *
   * @return The count of busy processes.
   */
  int getBusyCount();
}
