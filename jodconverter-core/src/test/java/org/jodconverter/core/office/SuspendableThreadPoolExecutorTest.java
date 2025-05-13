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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Contains tests for the {@link SuspendableThreadPoolExecutor} class. */
class SuspendableThreadPoolExecutorTest {

  private TestExecutor executor;

  static class TestExecutor extends SuspendableThreadPoolExecutor {
    final AtomicReference<Thread> thread = new AtomicReference<>();

    TestExecutor(final ThreadFactory threadFactory) {
      super(threadFactory);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void beforeExecute(final Thread thread, final Runnable task) {
      this.thread.set(thread);
      try {
        super.beforeExecute(thread, task);
      } finally {
        this.thread.set(null);
      }
    }
  }

  private void sleep() {
    try {
      Thread.sleep(500L);
    } catch (InterruptedException ignored) {
    }
  }

  @BeforeEach
  void setup() {
    executor = new TestExecutor(new NamedThreadFactory("TestExecutor"));
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    executor.shutdownNow();
    executor.awaitTermination(10_000L, TimeUnit.MILLISECONDS);
  }

  @Nested
  class BeforeExecute {

    @Test
    void whenNotAvailable_ShouldNotExecuteTask() {

      final AtomicBoolean executed = new AtomicBoolean();
      executor.execute(() -> executed.set(true));
      sleep();
      assertThat(executed).isFalse();
    }

    @Test
    void whenAvailable_ShouldExecuteTask() {

      final AtomicBoolean executed = new AtomicBoolean();
      executor.setAvailable(true);
      executor.execute(() -> executed.set(true));
      sleep();
      assertThat(executed).isTrue();
    }

    @Test
    void whenSetAvailableTrueWhileWaiting_ShouldExecuteTask() {

      final AtomicBoolean executed = new AtomicBoolean();
      executor.execute(() -> executed.set(true));
      sleep();
      executor.setAvailable(true);
      assertThat(executed).isTrue();
    }

    // @Test // TODO: Investigate why this doesn't always work on cirrus ci
    void whenInterruptedWhileWaiting_ShouldNotExecuteTask() {

      final AtomicBoolean executed = new AtomicBoolean();
      final Future<?> task = executor.submit(() -> executed.set(true));
      sleep();
      Thread.currentThread().interrupt();
      assertThatExceptionOfType(InterruptedException.class).isThrownBy(task::get);
      assertThat(executed).isFalse();
    }
  }
}
