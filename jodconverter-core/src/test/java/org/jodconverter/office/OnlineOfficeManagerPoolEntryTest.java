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

package org.jodconverter.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

public class OnlineOfficeManagerPoolEntryTest {

  @Test
  public void execute_UnproblematicTask_TaskShouldBeExecutedSuccessfully() throws OfficeException {

    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry();

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();

      final SimpleOfficeTask task = new SimpleOfficeTask();
      officeManager.execute(task);
      assertThat(task.isCompleted()).isTrue();

    } finally {

      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void execute_WhenTimeoutExceptionOccured_ThrowsOfficeException() throws OfficeException {

    final SimpleOfficeManagerPoolEntryConfig config = new SimpleOfficeManagerPoolEntryConfig();
    config.setTaskExecutionTimeout(500L);
    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry(config);

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();

      final SimpleOfficeTask task = new SimpleOfficeTask(1000);
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> officeManager.execute(task))
          .withCauseExactlyInstanceOf(TimeoutException.class);

    } finally {

      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void execute_WhenUnknownExecutionExceptionOccured_ThrowsOfficeExceptionWithCause()
      throws OfficeException {

    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry();
    final IllegalStateException exception = new IllegalStateException("this is a test");

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();

      final SimpleOfficeTask task = new SimpleOfficeTask(exception);
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> officeManager.execute(task))
          .withCauseExactlyInstanceOf(IllegalStateException.class)
          .satisfies(e -> assertThat(e.getCause()).hasMessage("this is a test"));

    } finally {
      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void execute_WhenOfficeExecutionExceptionOccured_ThrowsOnlyOfficeException()
      throws OfficeException {

    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry();
    final OfficeException exception = new OfficeException("this is a test");

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();

      final SimpleOfficeTask task = new SimpleOfficeTask(exception);
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> officeManager.execute(task))
          .withNoCause()
          .withMessage("this is a test");

    } finally {
      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void execute_WhithoutHavingBeenStarted_ThrowsOfficeExceptionAfterTimeout()
      throws OfficeException {

    final SimpleOfficeManagerPoolEntryConfig config = new SimpleOfficeManagerPoolEntryConfig();
    config.setTaskExecutionTimeout(500L);
    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry(config);

    try {
      final SimpleOfficeTask task = new SimpleOfficeTask(1000);

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> officeManager.execute(task))
          .withCauseExactlyInstanceOf(TimeoutException.class);

    } finally {
      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void start_WhenStartedTwice_ShouldHaveNoEffect() throws OfficeException {

    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry();

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();

    } finally {

      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void start_WhenStartedAfterItHasBeenStopped_ShouldThrowIllegalStateException()
      throws OfficeException {

    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry();

    officeManager.start();
    assertThat(officeManager.isRunning()).isTrue();
    officeManager.stop();
    assertThat(officeManager.isRunning()).isFalse();
    assertThatIllegalStateException().isThrownBy(officeManager::start);
  }

  @Test
  public void stop_WhenNeverStartedBefore_ShouldStopAndInvalidateManager() throws OfficeException {

    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry();

    officeManager.stop();
    assertThat(officeManager.isRunning()).isFalse();
    assertThatIllegalStateException().isThrownBy(officeManager::start);
  }
}
