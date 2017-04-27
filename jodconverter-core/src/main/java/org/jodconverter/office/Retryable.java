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

/**
 * Object that will attempt to execute a task until it succeeds or that a specific timeout is
 * reached.
 */
abstract class Retryable {

  /**
   * Attempt to execute the task, once.
   *
   * @throws TemporaryException for an error condition that can be temporary - i.e. retrying later
   *     could be successful
   * @throws Exception for all other error conditions
   */
  protected abstract void attempt() throws Exception; // NOSONAR

  /**
   * Executes the task without a starting delay.
   *
   * @param interval the interval between each attempt.
   * @param timeout the timeout after which no more attempt will be tried.
   * @throws RetryTimeoutException if we have reached the timeout.
   * @throws Exception for all other error conditions
   */
  public void execute(final long interval, final long timeout) // NOSONAR
      throws RetryTimeoutException, Exception { // NOSONAR

    execute(0L, interval, timeout);
  }

  /**
   * Executes the task with a specified starting delay.
   *
   * @param delay a delay to wait for before the fist attempt.
   * @param interval the interval between each attempt.
   * @param timeout the timeout after which no more attempt will be tried.
   * @throws RetryTimeoutException if we have reached the timeout.
   * @throws Exception for all other error conditions
   */
  public void execute(final long delay, final long interval, final long timeout) // NOSONAR
      throws RetryTimeoutException, Exception { // NOSONAR

    final long start = System.currentTimeMillis();
    if (delay > 0L) {
      sleep(delay);
    }
    while (true) {
      try {
        attempt();
        return;
      } catch (TemporaryException temporaryEx) { // NOSONAR
        if (System.currentTimeMillis() - start < timeout) {
          sleep(interval);
          // continue
        } else {
          throw new RetryTimeoutException(temporaryEx.getCause());
        }
      }
    }
  }

  private void sleep(final long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException interruptedEx) { // NOSONAR
      // continue
    }
  }
}
