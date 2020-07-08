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
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.jodconverter.core.office.SimpleOfficeManager.DEFAULT_TASK_EXECUTION_TIMEOUT;

import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.task.SimpleOfficeTask;

/** Contains tests for the {@link SimpleOfficeManagerPoolEntry} class. */
class AbstractOfficeManagerPoolEntryTest {

  @Nested
  class Execute {

    @Test
    void whenTaskSuccessful_ShouldBeCompleted() {

      final SimpleOfficeManagerPoolEntry entry =
          new SimpleOfficeManagerPoolEntry(DEFAULT_TASK_EXECUTION_TIMEOUT);
      try {
        entry.start();
        assertThat(entry.isRunning()).isTrue();

        final SimpleOfficeTask task = new SimpleOfficeTask();
        assertThatCode(() -> entry.execute(task)).doesNotThrowAnyException();
        assertThat(task.isCompleted()).isTrue();

      } finally {
        entry.stop();
        assertThat(entry.isRunning()).isFalse();
      }
    }

    @Test
    void whenTaskExecutionTimeout_ShouldThrowOfficeException() {

      final SimpleOfficeManagerPoolEntry entry = new SimpleOfficeManagerPoolEntry(500L);
      try {
        entry.start();
        assertThat(entry.isRunning()).isTrue();

        final SimpleOfficeTask task = new SimpleOfficeTask(1_000L);
        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(() -> entry.execute(task))
            .withCauseExactlyInstanceOf(TimeoutException.class)
            .withMessageStartingWith("Task did not complete within timeout");

      } finally {
        entry.stop();
        assertThat(entry.isRunning()).isFalse();
      }
    }

    @Test
    void whenExecutionExceptionIsOfficeException_ShouldThrowSameOfficeException() {

      final SimpleOfficeManagerPoolEntry entry =
          new SimpleOfficeManagerPoolEntry(DEFAULT_TASK_EXECUTION_TIMEOUT);
      final IllegalStateException exception = new IllegalStateException("This is a test");
      try {
        entry.start();
        assertThat(entry.isRunning()).isTrue();

        final SimpleOfficeTask task = new SimpleOfficeTask(exception);
        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(() -> entry.execute(task))
            .withCauseExactlyInstanceOf(IllegalStateException.class)
            .satisfies(e -> assertThat(e.getCause()).hasMessage("This is a test"));

      } finally {
        entry.stop();
        assertThat(entry.isRunning()).isFalse();
      }
    }

    @Test
    void whenExecutionExceptionIsNotOfficeException_ShouldWrapCauseInOfficeException() {

      final SimpleOfficeManagerPoolEntry entry =
          new SimpleOfficeManagerPoolEntry(DEFAULT_TASK_EXECUTION_TIMEOUT);
      final OfficeException exception = new OfficeException("This is a test");
      try {
        entry.start();
        assertThat(entry.isRunning()).isTrue();

        final SimpleOfficeTask task = new SimpleOfficeTask(exception);
        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(() -> entry.execute(task))
            .withNoCause()
            .withMessage("This is a test");

      } finally {
        entry.stop();
        assertThat(entry.isRunning()).isFalse();
      }
    }

    @Test
    void whenEntryNotStarted_ShouldThrowOfficeExceptionAfterExecutionTimeout() {

      // If the entry has not been started, the SuspendableThreadPoolExecutor of the
      // entry would never has been made available. So, a task execution timeout will
      // occur.

      final SimpleOfficeManagerPoolEntry entry = new SimpleOfficeManagerPoolEntry(500L);
      try {
        final SimpleOfficeTask task = new SimpleOfficeTask(1_000L);

        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(() -> entry.execute(task))
            .withCauseExactlyInstanceOf(TimeoutException.class);

      } finally {
        entry.stop();
        assertThat(entry.isRunning()).isFalse();
      }
    }

    @Test
    void whenTaskInterrupted_ShouldThrowOfficeException() {

      final SimpleOfficeManagerPoolEntry entry =
          new SimpleOfficeManagerPoolEntry(DEFAULT_TASK_EXECUTION_TIMEOUT);
      try {
        final SimpleOfficeTask task = new SimpleOfficeTask(5_000L);
        final AtomicReference<OfficeException> ex = new AtomicReference<>();

        assertThatCode(
                () -> {
                  final Thread thread =
                      new Thread(
                          () -> {
                            try {
                              entry.execute(task);
                            } catch (OfficeException oe) {
                              ex.set(oe);
                            }
                          });

                  // Start the thread.
                  thread.start();
                  // Interrupt the thread.
                  thread.interrupt();
                  //  Wait for thread to complete.
                  thread.join();
                })
            .doesNotThrowAnyException();

        assertThat(ex.get())
            .isExactlyInstanceOf(OfficeException.class)
            .hasMessageStartingWith("Task was interrupted while executing")
            .hasCauseExactlyInstanceOf(InterruptedException.class);

      } finally {
        entry.stop();
        assertThat(entry.isRunning()).isFalse();
      }
    }

    @Test
    void whenTerminated_ShouldThrowIllegalStateException() {

      final SimpleOfficeManagerPoolEntry entry =
          new SimpleOfficeManagerPoolEntry(DEFAULT_TASK_EXECUTION_TIMEOUT);
      entry.start();
      assertThat(entry.isRunning()).isTrue();
      entry.stop();
      assertThat(entry.isRunning()).isFalse();
      assertThatExceptionOfType(RejectedExecutionException.class)
          .isThrownBy(() -> entry.execute(new SimpleOfficeTask(0L)));
    }
  }

  @Nested
  class Start {

    @Test
    void whenAlreadyStarted_ShouldIgnoreSubsequentStart() {

      final SimpleOfficeManagerPoolEntry entry =
          new SimpleOfficeManagerPoolEntry(DEFAULT_TASK_EXECUTION_TIMEOUT);
      try {
        entry.start();
        assertThat(entry.isRunning()).isTrue();
        assertThatCode(entry::start).doesNotThrowAnyException();
        assertThat(entry.isRunning()).isTrue();

      } finally {
        entry.stop();
        assertThat(entry.isRunning()).isFalse();
      }
    }

    @Test
    void whenTerminated_ShouldThrowIllegalStateException() {

      final SimpleOfficeManagerPoolEntry entry =
          new SimpleOfficeManagerPoolEntry(DEFAULT_TASK_EXECUTION_TIMEOUT);
      entry.start();
      assertThat(entry.isRunning()).isTrue();
      entry.stop();
      assertThat(entry.isRunning()).isFalse();
      assertThatIllegalStateException().isThrownBy(entry::start);
    }
  }

  @Nested
  class Stop {
    @Test
    void whenNotStartedYet_ShouldStopAndInvalidateManager() {

      final SimpleOfficeManagerPoolEntry entry =
          new SimpleOfficeManagerPoolEntry(DEFAULT_TASK_EXECUTION_TIMEOUT);
      entry.stop();
      assertThat(entry.isRunning()).isFalse();
      assertThatIllegalStateException().isThrownBy(entry::start);
    }
  }

  @Nested
  class CancelTask {

    @Test
    void whenNoRunningTask_ShouldNoNothing() {

      final SimpleOfficeManagerPoolEntry entry =
          new SimpleOfficeManagerPoolEntry(DEFAULT_TASK_EXECUTION_TIMEOUT);
      try {
        entry.start();
        assertThat(entry.isRunning()).isTrue();
        assertThatCode(entry::cancelTask).doesNotThrowAnyException();

      } finally {
        entry.stop();
        assertThat(entry.isRunning()).isFalse();
      }
    }

    @Test
    void whenTaskRunning_ShouldCancelTask() {

      final SimpleOfficeManagerPoolEntry entry =
          new SimpleOfficeManagerPoolEntry(DEFAULT_TASK_EXECUTION_TIMEOUT);
      try {
        final SimpleOfficeTask task = new SimpleOfficeTask(5_000L);
        final AtomicReference<OfficeException> ex = new AtomicReference<>();

        assertThatCode(
                () -> {
                  final Thread thread =
                      new Thread(
                          () -> {
                            try {
                              entry.execute(task);
                            } catch (OfficeException oe) {
                              ex.set(oe);
                            }
                          });

                  // Start the thread.
                  thread.start();
                  // Let the task execution begin.
                  Thread.sleep(250L);
                  // Cancel the task.
                  entry.cancelTask();
                  //  Wait for thread to complete.
                  thread.join();
                })
            .doesNotThrowAnyException();

        assertThat(ex.get())
            .isExactlyInstanceOf(OfficeException.class)
            .hasMessageStartingWith("Task was cancelled")
            .hasCauseExactlyInstanceOf(CancellationException.class);

      } finally {
        entry.stop();
        assertThat(entry.isRunning()).isFalse();
      }
    }
  }
}
