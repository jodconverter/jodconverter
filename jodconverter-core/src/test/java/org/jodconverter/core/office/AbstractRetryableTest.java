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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Contains tests for the {@link AbstractRetryable} class. */
class AbstractRetryableTest {

  private static final long NO_SLEEP = 0L;

  @Nested
  class Execute {

    @Test
    void whenExecuteOnTime_ShouldNotThrowAnyException() {

      final SimpleRetryable retryable = new SimpleRetryable(1);
      assertThatCode(() -> retryable.execute(250L, 500L)).doesNotThrowAnyException();
    }

    @Test
    void withInitialDelay_ShouldApplyInitialDelayAndThrowRetryTimeoutException() {

      final SimpleRetryable retryable = new SimpleRetryable(2, 250L);
      assertThatExceptionOfType(RetryTimeoutException.class)
          .isThrownBy(() -> retryable.execute(250L, NO_SLEEP, 300L));
      assertThat(retryable.getAttempts()).isEqualTo(1);
    }

    @Test
    void withoutInitialDelay_ShouldNotApplyInitialDelay() {

      final SimpleRetryable retryable = new SimpleRetryable(2, 100L);
      assertThatCode(() -> retryable.execute(NO_SLEEP, 300L)).doesNotThrowAnyException();
      assertThat(retryable.getAttempts()).isEqualTo(2);
    }

    @Test
    void withInterval_ShouldApplyIntervalDelayAndThrowRetryTimeoutException() {

      final SimpleRetryable retryable = new SimpleRetryable(3, 250L);
      assertThatExceptionOfType(RetryTimeoutException.class)
          .isThrownBy(() -> retryable.execute(250L, 500L));
      assertThat(retryable.getAttempts()).isEqualTo(2);
    }

    @Test
    void withNoInterval_ShouldNotApplyIntervalDelay() {

      final SimpleRetryable retryable = new SimpleRetryable(3, 100L);
      assertThatCode(() -> retryable.execute(NO_SLEEP, 750L)).doesNotThrowAnyException();
      assertThat(retryable.getAttempts()).isEqualTo(3);
    }
  }

  @Nested
  class Sleep {

    @Test
    void whenInterrupted_ShouldNotApplyIntervalDelay() {

      final SimpleRetryable retryable = new SimpleRetryable(2);
      final AtomicReference<Exception> exep = new AtomicReference<>();
      assertThatCode(
              () -> {
                final Thread thread =
                    new Thread(
                        () -> {
                          try {
                            retryable.execute(1_000L, 2_000L);
                          } catch (Exception ex) {
                            exep.set(ex);
                          }
                        });

                // Start the thread.
                thread.start();
                // Let the execution begin.
                Thread.sleep(250L);
                // Interrupt the thread.
                thread.interrupt();
                //  Wait for thread to complete.
                thread.join();
              })
          .doesNotThrowAnyException();

      assertThat(retryable.getAttempts()).isEqualTo(1);
      assertThat(exep.get())
          .isExactlyInstanceOf(RetryTimeoutException.class)
          .hasCauseExactlyInstanceOf(InterruptedException.class);
    }
  }
}
