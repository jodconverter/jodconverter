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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object that will attempt to execute a task until it succeeds or that a specific timeout is
 * reached.
 */
public abstract class AbstractRetryable<T extends Throwable> {

  private static final long NO_SLEEP = 0L;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** Initializes a new instance of the class. */
  protected AbstractRetryable() {
    super();
  }

  /**
   * Attempt to execute the task, once.
   *
   * @throws TemporaryException For an error condition that can be temporary - i.e., retrying later
   *     could be successful
   * @throws T For all other error conditions
   */
  protected abstract void attempt() throws TemporaryException, T;

  /**
   * Executes the task without a starting delay.
   *
   * @param interval The interval between each task execution attempt.
   * @param timeout The timeout after which we won't try again to execute the task.
   * @throws RetryTimeoutException If this Retryable fails to complete its task in the given time.
   * @throws T For all other error conditions.
   */
  public void execute(final long interval, final long timeout) throws RetryTimeoutException, T {

    execute(NO_SLEEP, interval, timeout);
  }

  /**
   * Executes the task without a starting delay.
   *
   * @param delay An initial delay to wait for before the first attempt.
   * @param interval The interval between each task execution attempt.
   * @param timeout The timeout after which we won't try again to execute the task.
   * @throws RetryTimeoutException If this Retryable fails to complete its task in the given time.
   * @throws T For all other error conditions.
   */
  public void execute(final long delay, final long interval, final long timeout)
      throws RetryTimeoutException, T {

    final long start = System.currentTimeMillis();
    int attempt = 0;

    if (delay > NO_SLEEP) {
      sleep(delay);
    }

    while (true) {
      attempt++;
      try {
        logger.debug("Execution attempt #{}", attempt);
        attempt();
        logger.debug("Execution succeeded on attempt #{}", attempt);
        return;
      } catch (TemporaryException temporaryException) {
        if (System.currentTimeMillis() - start < timeout) {
          if (interval > NO_SLEEP) {
            logger.debug(
                "Execution attempt #{} failed, retrying after sleep of {} ms", attempt, interval);
            sleep(interval);
          } else {
            logger.debug("Execution attempt #{} failed, retrying without sleep", attempt);
          }
        } else {
          logger.debug("Execution failed on attempt #{}", attempt);
          throw new RetryTimeoutException( // NOPMD - Only cause is relevant
              temporaryException.getCause());
        }
      }
    }
  }

  private void sleep(final long millis) throws RetryTimeoutException {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new RetryTimeoutException(ex);
    }
  }
}
