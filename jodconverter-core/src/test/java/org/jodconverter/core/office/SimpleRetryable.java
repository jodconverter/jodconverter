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

/** Retryable for testing purposes. */
public final class SimpleRetryable extends AbstractRetryable<Exception> {

  private static final long NO_SLEEP = 0L;

  private int attempts;
  private final long sleepms;
  private final int maxAttempts;

  /** Retryable ctor. */
  public SimpleRetryable(final int maxAttempts) {
    this(maxAttempts, NO_SLEEP);
  }

  /** Retryable ctor. */
  public SimpleRetryable(final int maxAttempts, final long sleepms) {
    super();

    this.maxAttempts = maxAttempts;
    this.sleepms = sleepms;
  }

  @Override
  protected void attempt() throws Exception {

    attempts++;
    if (sleepms > NO_SLEEP) {
      Thread.sleep(sleepms);
    }
    if (attempts >= maxAttempts) {
      return;
    }
    throw new TemporaryException("attempt failed");
  }

  public int getAttempts() {
    return attempts;
  }
}
