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

/**
 * Object that will attempt to execute a task until it succeeds or that a specific timeout is
 * reached.
 */
abstract class AbstractRetryable {

  /** Initializes a new instance of the class. */
  protected AbstractRetryable() {
    super();
  }

  /**
   * Attempt to execute the task, once.
   *
   * @throws TemporaryException For an error condition that can be temporary - i.e. retrying later
   *     could be successful.
   * @throws Exception If an error occurs.
   */
  protected abstract void attempt() throws Exception; // NOSONAR

  /**
   * Executes the task without a starting delay.
   *
   * @param interval The interval between each task execution attempt.
   * @param timeout The timeout after which we won't try again to execute the task.
   * @throws RetryTimeoutException If we have reached the timeout.
   * @throws InterruptedException If any thread has interrupted the current thread. The interrupted
   *     status of the current thread is cleared when this exception is thrown.
   * @throws Exception For all other error conditions.
   */
  public void execute(final long interval, final long timeout) throws Exception {

    final long start = System.currentTimeMillis();
    while (true) {
      try {
        attempt();
        return;
      } catch (TemporaryException temporaryEx) { // NOSONAR
        if (System.currentTimeMillis() - start < timeout) {
          Thread.sleep(interval);
          // retryConfig
        } else {
          throw new RetryTimeoutException(temporaryEx.getCause());
        }
      }
    }
  }
}
