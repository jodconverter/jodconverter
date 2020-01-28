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

public class SimpleOfficeTask implements OfficeTask {

  private final long delayTime;
  private boolean completed;
  private final Exception thrownException;

  /** Create a new task with default values. */
  public SimpleOfficeTask() {
    this(0L, null);
  }

  /**
   * Create a new task with the specified delay.
   *
   * @param delayTime The delay.
   */
  public SimpleOfficeTask(final long delayTime) {
    this(delayTime, null);
  }

  /**
   * Create a new task that will throw the specified exception.
   *
   * @param thrownException The thrown exception.
   */
  public SimpleOfficeTask(final Exception thrownException) {
    this(0L, thrownException);
  }

  /**
   * Create a new task with will throw the specified exception after the specified delay.
   *
   * @param delayTime The delay.
   * @param thrownException The thrown exception.
   */
  public SimpleOfficeTask(final long delayTime, final Exception thrownException) {
    super();

    this.delayTime = delayTime;
    this.thrownException = thrownException;
  }

  @Override
  public void execute(final OfficeContext context) throws OfficeException {

    try {
      if (delayTime > 0L) {
        Thread.sleep(delayTime);
      }

      if (thrownException != null) {
        throw thrownException;
      }
      completed = true;

    } catch (OfficeException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new OfficeException("Failed to execute task", exception);
    }
  }

  /**
   * Gets whether the task is completed or not.
   *
   * @return true if the task is completed, false otherwise.
   */
  public boolean isCompleted() {
    return completed;
  }
}
