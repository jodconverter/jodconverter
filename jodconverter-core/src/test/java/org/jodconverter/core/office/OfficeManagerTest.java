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
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.File;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.core.task.SimpleOfficeTask;

/** Contains tests for the default methods of the {@link OfficeManager} interface. */
class OfficeManagerTest {

  /** A manager that only knows how to execute a task, as a custom implementation would. */
  private static class BlockingOfficeManager implements OfficeManager {

    private final AtomicInteger executed = new AtomicInteger();

    @Override
    public void execute(final OfficeTask task) throws OfficeException {
      executed.incrementAndGet();
      task.execute(new SimpleOfficeContext());
    }

    @Override
    public boolean isRunning() {
      return true;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public File makeTemporaryFile(final String extension) {
      throw new UnsupportedOperationException();
    }
  }

  @Nested
  class Submit {

    @Test
    void whenTheTaskSucceeds_ShouldExecuteItBeforeReturningACompletedFuture() {

      final var manager = new BlockingOfficeManager();
      final var task = new SimpleOfficeTask();

      final var future = manager.submit(task);

      assertThat(manager.executed).hasValue(1);
      assertThat(task.isCompleted()).isTrue();
      assertThat(future).isCompleted();
      assertThat(future.join()).isNull();
    }

    @Test
    void whenTheTaskFails_ShouldReturnAFutureCompletedWithItsException() {

      final var manager = new BlockingOfficeManager();
      final var failure = new OfficeException("The task failed");

      final var future = manager.submit(new SimpleOfficeTask(failure));

      assertThat(future).isCompletedExceptionally();
      // The simple task wraps the exception it is given.
      assertThat(catchThrowable(future::join))
          .isInstanceOf(CompletionException.class)
          .hasCauseInstanceOf(OfficeException.class)
          .hasRootCause(failure);
    }
  }
}
