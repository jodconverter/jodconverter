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

package org.jodconverter.local.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_EXECUTION_TIMEOUT;
import static org.jodconverter.local.office.ExternalOfficeManager.DEFAULT_CONNECT_FAIL_FAST;
import static org.jodconverter.local.office.ExternalOfficeManager.DEFAULT_CONNECT_ON_START;
import static org.jodconverter.local.office.ExternalOfficeManager.DEFAULT_CONNECT_RETRY_INTERVAL;
import static org.jodconverter.local.office.ExternalOfficeManager.DEFAULT_CONNECT_TIMEOUT;
import static org.jodconverter.local.office.ExternalOfficeManager.DEFAULT_MAX_TASKS_PER_CONNECTION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.OfficeTask;

/** Contains tests for the {@link ExternalOfficeManagerPoolEntry} class. */
class ExternalOfficeManagerPoolEntryTest {

  @Nested
  class DoExecute {

    @Test
    void whenNotConnected_ShouldConnectFirstThenExecuteTask() throws OfficeException {

      final OfficeUrl url = new OfficeUrl(2002);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final ExternalOfficeManagerPoolEntry entry =
          new ExternalOfficeManagerPoolEntry(
              DEFAULT_CONNECT_ON_START,
              DEFAULT_MAX_TASKS_PER_CONNECTION,
              DEFAULT_TASK_EXECUTION_TIMEOUT,
              new ExternalOfficeConnectionManager(
                  DEFAULT_CONNECT_TIMEOUT, DEFAULT_CONNECT_RETRY_INTERVAL, true, connection));
      entry.start();

      final OfficeTask task = mock(OfficeTask.class);
      entry.execute(task);

      assertThat(connection.getConnectCount()).isEqualTo(1);
      verify(task, times(1)).execute(any());
    }

    @Test
    void whenMaxTasksPerConnectionReached_ShouldReconnect()
        throws OfficeException, InterruptedException {

      final OfficeUrl url = new OfficeUrl(2002);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final ExternalOfficeManagerPoolEntry entry =
          new ExternalOfficeManagerPoolEntry(
              true,
              2,
              DEFAULT_TASK_EXECUTION_TIMEOUT,
              new ExternalOfficeConnectionManager(
                  DEFAULT_CONNECT_TIMEOUT, DEFAULT_CONNECT_RETRY_INTERVAL, true, connection));
      entry.start();

      final OfficeTask task = mock(OfficeTask.class);
      entry.execute(task);
      entry.execute(task);

      // Wait a bit since reconnect use "Executor.execute"
      Thread.sleep(100L);

      assertThat(connection.getConnectCount()).isEqualTo(2); // Initial and reconnect
      verify(task, times(2)).execute(any());
    }
  }

  @Nested
  class HandleExecuteTimeoutException {

    @Test
    void shouldReconnect() throws OfficeException, InterruptedException {

      final OfficeUrl url = new OfficeUrl(2002);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final ExternalOfficeManagerPoolEntry entry =
          new ExternalOfficeManagerPoolEntry(
              DEFAULT_CONNECT_ON_START,
              DEFAULT_MAX_TASKS_PER_CONNECTION,
              250L,
              new ExternalOfficeConnectionManager(
                  DEFAULT_CONNECT_TIMEOUT, DEFAULT_CONNECT_RETRY_INTERVAL, true, connection));
      entry.start();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(
              () ->
                  entry.execute(
                      context -> {
                        try {
                          Thread.sleep(500L);
                        } catch (InterruptedException ignored) {
                          // ignore
                        }
                      }));

      // Wait a bit since reconnect use "Executor.execute"
      Thread.sleep(250L);

      assertThat(connection.getConnectCount()).isEqualTo(2); // Initial and reconnect
    }
  }

  @Nested
  class IsRunning {

    @Test
    void whenNotStarted_ReturnFalse() {

      final OfficeUrl url = new OfficeUrl(2002);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final ExternalOfficeManagerPoolEntry entry =
          new ExternalOfficeManagerPoolEntry(
              DEFAULT_CONNECT_ON_START,
              DEFAULT_MAX_TASKS_PER_CONNECTION,
              DEFAULT_TASK_EXECUTION_TIMEOUT,
              new ExternalOfficeConnectionManager(
                  DEFAULT_CONNECT_TIMEOUT,
                  DEFAULT_CONNECT_RETRY_INTERVAL,
                  DEFAULT_CONNECT_FAIL_FAST,
                  connection));

      assertThat(entry.isRunning()).isFalse();
    }

    @Test
    void whenStartedButNotConnected_ReturnFalse() throws OfficeException {

      final OfficeUrl url = new OfficeUrl(2002);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final ExternalOfficeManagerPoolEntry entry =
          new ExternalOfficeManagerPoolEntry(
              false,
              DEFAULT_MAX_TASKS_PER_CONNECTION,
              DEFAULT_TASK_EXECUTION_TIMEOUT,
              new ExternalOfficeConnectionManager(
                  DEFAULT_CONNECT_TIMEOUT, DEFAULT_CONNECT_RETRY_INTERVAL, true, connection));
      entry.start();

      assertThat(entry.isRunning()).isFalse();
    }

    @Test
    void whenStartedAndConnected_ReturnTrue() throws OfficeException {

      final OfficeUrl url = new OfficeUrl(2002);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final ExternalOfficeManagerPoolEntry entry =
          new ExternalOfficeManagerPoolEntry(
              true,
              DEFAULT_MAX_TASKS_PER_CONNECTION,
              DEFAULT_TASK_EXECUTION_TIMEOUT,
              new ExternalOfficeConnectionManager(
                  DEFAULT_CONNECT_TIMEOUT, DEFAULT_CONNECT_RETRY_INTERVAL, true, connection));
      entry.start();

      assertThat(entry.isRunning()).isTrue();
    }
  }

  @Nested
  class DoStart {
    // Already tested in the IsRunning class.
  }

  @Nested
  class DoStop {

    @Test
    void ShouldDisconnect() throws OfficeException {

      final OfficeUrl url = new OfficeUrl(2002);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final ExternalOfficeManagerPoolEntry entry =
          new ExternalOfficeManagerPoolEntry(
              true,
              DEFAULT_MAX_TASKS_PER_CONNECTION,
              DEFAULT_TASK_EXECUTION_TIMEOUT,
              new ExternalOfficeConnectionManager(
                  DEFAULT_CONNECT_TIMEOUT, DEFAULT_CONNECT_RETRY_INTERVAL, true, connection));
      entry.start();

      assertThat(entry.isRunning()).isTrue();
      entry.stop();
      assertThat(entry.isRunning()).isFalse();
    }
  }

  @Nested
  class Misc {

    @Test
    void whenUnexpectedDisconnection_ShouldCancelCurrentTaskAndReconnect()
        throws OfficeException, InterruptedException {

      final OfficeUrl url = new OfficeUrl(2002);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final ExternalOfficeManagerPoolEntry entry =
          new ExternalOfficeManagerPoolEntry(
              true,
              DEFAULT_MAX_TASKS_PER_CONNECTION,
              DEFAULT_TASK_EXECUTION_TIMEOUT,
              new ExternalOfficeConnectionManager(
                  DEFAULT_CONNECT_TIMEOUT, DEFAULT_CONNECT_RETRY_INTERVAL, true, connection));
      entry.start();

      // Ensure we are running.
      assertThat(entry.isRunning()).isTrue();

      // executed will hold whether the task has been fully executed or not.
      final AtomicBoolean executed = new AtomicBoolean(false);

      // Create a thread that will sleep 250 millisec and set the executed flag to true.
      final Runnable runnable =
          () -> {
            try {
              Thread.sleep(200L);
              executed.set(true);
            } catch (InterruptedException ignored) {
              // ignore
            }
          };
      final Thread thread = new Thread(runnable);

      // Execute a task that will only start the thread and return immediately.
      entry.execute(context -> thread.start());

      // Disconnect (this will be done before the sleep of the thread is done).
      connection.disconnect();

      // Check that the task was not fully executed (it has been cancelled).
      assertThat(executed).isFalse();

      // Wait a bit since reconnect use "Executor.execute"
      Thread.sleep(100L);

      // Check that the connection is back.
      assertThat(entry.isRunning()).isTrue();

      // Try again without the disconnection.
      entry.execute(context -> runnable.run());

      // Check that the task was fully executed
      assertThat(executed).isTrue();
    }
  }
}
