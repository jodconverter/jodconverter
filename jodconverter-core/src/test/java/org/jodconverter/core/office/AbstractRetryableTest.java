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

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Contains tests for the {@link AbstractRetryable} class. */
class AbstractRetryableTest {

  private static final long NO_SLEEP = 0L;

  @Nested
  class Execute {

    @Test
    void whenExecuteOnTime_ShouldNotThrowAnyException() {

      final var retryable = new SimpleRetryable(1);
      assertThatCode(() -> retryable.execute(250L, 500L)).doesNotThrowAnyException();
    }

    @Test
    void withInitialDelay_ShouldApplyInitialDelayAndThrowRetryTimeoutException() {

      final var retryable = new SimpleRetryable(2, 250L);
      assertThatExceptionOfType(RetryTimeoutException.class)
          .isThrownBy(() -> retryable.execute(250L, NO_SLEEP, 300L));
      assertThat(retryable.getAttempts()).isEqualTo(1);
    }

    @Test
    void withoutInitialDelay_ShouldNotApplyInitialDelay() {

      final var retryable = new SimpleRetryable(2, 100L);
      assertThatCode(() -> retryable.execute(NO_SLEEP, 300L)).doesNotThrowAnyException();
      assertThat(retryable.getAttempts()).isEqualTo(2);
    }

    @Test
    void withInterval_ShouldApplyIntervalDelayAndThrowRetryTimeoutException() {

      final var retryable = new SimpleRetryable(3, 250L);
      assertThatExceptionOfType(RetryTimeoutException.class)
          .isThrownBy(() -> retryable.execute(250L, 500L));
      assertThat(retryable.getAttempts()).isEqualTo(2);
    }

    @Test
    void withNoInterval_ShouldNotApplyIntervalDelay() {

      final var retryable = new SimpleRetryable(3, 100L);
      assertThatCode(() -> retryable.execute(NO_SLEEP, 750L)).doesNotThrowAnyException();
      assertThat(retryable.getAttempts()).isEqualTo(3);
    }

    @Test
    void whenInterruptedWithNoInterval_ShouldThrowRetryTimeoutException() {

      final var retryable = new SimpleRetryable(Integer.MAX_VALUE);

      Thread.currentThread().interrupt();
      try {
        assertThatExceptionOfType(RetryTimeoutException.class)
            .isThrownBy(() -> retryable.execute(NO_SLEEP, 60_000L))
            .withCauseExactlyInstanceOf(InterruptedException.class);
      } finally {
        // Clear the interrupted status of the test thread.
        assertThat(Thread.interrupted()).isTrue();
      }
      // The first attempt is made: only the retries are given up.
      assertThat(retryable.getAttempts()).isEqualTo(1);
    }
  }

  @Nested
  class Sleep {

    @Test
    void whenInterruptedWhileWaitingForTheNextAttempt_ShouldThrowRetryTimeoutException() {

      // The thread is interrupted during the first attempt, which fails.
      final AbstractRetryable<RuntimeException> retryable =
          new AbstractRetryable<>() {
            @Override
            protected void attempt() throws TemporaryException {
              Thread.currentThread().interrupt();
              throw new TemporaryException("attempt failed");
            }
          };

      try {
        assertThatExceptionOfType(RetryTimeoutException.class)
            .isThrownBy(() -> retryable.execute(60_000L, 120_000L))
            .withCauseExactlyInstanceOf(InterruptedException.class);
      } finally {
        // The interrupted status is kept for the caller; clear it for the next tests.
        assertThat(Thread.interrupted()).isTrue();
      }
    }
  }
}
