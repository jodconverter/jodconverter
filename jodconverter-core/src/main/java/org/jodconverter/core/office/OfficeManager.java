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

package org.jodconverter.core.office;

import org.jodconverter.core.task.OfficeTask;

/**
 * An office manager knows how to execute an {@link OfficeTask}. An office manager must be started
 * before performing conversion tasks and must be stopped once it is no longer required. Once
 * stopped, an office manager cannot be restarted.
 */
public interface OfficeManager {

  /**
   * Executes the specified task and blocks until the task terminates.
   *
   * @param task The task to execute.
   * @throws OfficeException If an error occurs.
   */
  void execute(OfficeTask task) throws OfficeException;

  /**
   * Gets whether the manager is running.
   *
   * @return {@code true} if the manager is running, {@code false} otherwise.
   */
  boolean isRunning();

  /**
   * Starts the manager.
   *
   * @throws OfficeException If the manager cannot be started.
   */
  void start() throws OfficeException;

  /**
   * Stops the manager.
   *
   * @throws OfficeException If the manager cannot be stopped.
   */
  void stop() throws OfficeException;
}
