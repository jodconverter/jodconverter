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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.TimeoutException;

import org.junit.Test;

public class OnlineOfficeManagerPoolEntryTest {

  /**
   * Tests the execution of a task.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void execute_UnproblematicTask_TaskShouldBeExecutedSuccessfully() throws Exception {

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
  public void execute_WhenTimeoutExceptionOccured_ThrowsOfficeException() throws Exception {

    final SimpleOfficeManagerPoolEntryConfig config = new SimpleOfficeManagerPoolEntryConfig();
    config.setTaskExecutionTimeout(500L);
    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry(config);

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();

      final SimpleOfficeTask task = new SimpleOfficeTask(1000);
      try {
        officeManager.execute(task);
        fail("task should be timed out");
      } catch (Exception ex) {
        assertThat(ex).isExactlyInstanceOf(OfficeException.class);
        assertThat(ex.getCause()).isExactlyInstanceOf(TimeoutException.class);
      }

    } finally {

      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void execute_WhenUnknownExecutionExceptionOccured_ThrowsOfficeExceptionWithCause()
      throws Exception {

    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry();
    final IllegalStateException exception = new IllegalStateException("this is a test");

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();

      final SimpleOfficeTask task = new SimpleOfficeTask(exception);
      try {
        officeManager.execute(task);
        fail("task should have failed");
      } catch (Exception ex) {
        assertThat(ex).isExactlyInstanceOf(OfficeException.class);
        assertThat(ex.getCause())
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("this is a test");
      }

    } finally {
      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void execute_WhenOfficeExecutionExceptionOccured_ThrowsOnlyOfficeException()
      throws Exception {

    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry();
    final OfficeException exception = new OfficeException("this is a test");

    try {
      officeManager.start();
      assertThat(officeManager.isRunning()).isTrue();

      final SimpleOfficeTask task = new SimpleOfficeTask(exception);
      try {
        officeManager.execute(task);
        fail("task should have failed");
      } catch (Exception ex) {
        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasNoCause()
            .hasMessage("this is a test");
      }

    } finally {
      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void execute_WhithoutHavingBeenStarted_ThrowsOfficeExceptionAfterTimeout()
      throws Exception {

    final SimpleOfficeManagerPoolEntryConfig config = new SimpleOfficeManagerPoolEntryConfig();
    config.setTaskExecutionTimeout(500L);
    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry(config);

    try {
      final SimpleOfficeTask task = new SimpleOfficeTask(1000);
      try {
        officeManager.execute(task);
        fail("task should be timed out");
      } catch (OfficeException officeEx) {
        assertThat(officeEx.getCause()).isInstanceOf(TimeoutException.class);
      }

    } finally {
      officeManager.stop();
      assertThat(officeManager.isRunning()).isFalse();
    }
  }

  @Test
  public void start_WhenStartedTwice_ShouldHaveNoEffect() throws Exception {

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

  @Test(expected = IllegalStateException.class)
  public void start_WhenStartedAfterItHasBeenStopped_ShouldThrowIllegalStateException()
      throws Exception {

    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry();

    officeManager.start();
    assertThat(officeManager.isRunning()).isTrue();
    officeManager.stop();
    assertThat(officeManager.isRunning()).isFalse();
    officeManager.start();
  }

  @Test(expected = IllegalStateException.class)
  public void stop_WhenNeverStartedBefore_ShouldStopAndInvalidateManager() throws Exception {

    final SimpleOfficeManagerPoolEntry officeManager = new SimpleOfficeManagerPoolEntry();

    officeManager.stop();
    assertThat(officeManager.isRunning()).isFalse();
    officeManager.start();
  }
}
