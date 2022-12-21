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

package org.jodconverter.core.task;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;

/** Task that only sleep a specified delay. */
public final class SimpleOfficeTask implements OfficeTask {

  private static final long NO_DELAY = 0L;

  private final long delayTime;
  private boolean completed;
  private final Exception thrownException;

  /** Create a new task with default values. */
  public SimpleOfficeTask() {
    this(NO_DELAY);
  }

  /**
   * Create a new task with the specified delay.
   *
   * @param delayTime The delay.
   */
  public SimpleOfficeTask(final long delayTime) {
    super();

    this.delayTime = delayTime;
    this.thrownException = null;
  }

  /**
   * Create a new task that will throw the specified exception.
   *
   * @param thrownException The thrown exception.
   */
  public SimpleOfficeTask(final Exception thrownException) {
    this(NO_DELAY, thrownException);
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
  public void execute(@SuppressWarnings("NullableProblems") final OfficeContext context)
      throws OfficeException {

    try {
      if (delayTime > NO_DELAY) {
        Thread.sleep(delayTime);
      }

      if (thrownException != null) {
        throw thrownException;
      }
      completed = true;

    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } catch (Exception ex) {
      throw new OfficeException("Failed to execute task", ex);
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
