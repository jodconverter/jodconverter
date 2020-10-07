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

package org.jodconverter.local.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_EXECUTION_TIMEOUT;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_MAX_TASKS_PER_PROCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.OfficeTask;

/** Contains tests for the {@link LocalOfficeManagerPoolEntry} class. */
class LocalOfficeManagerPoolEntryTest {

  @Nested
  class DoExecute {

    @Test
    void whenMaxTasksPerConnectionReached_ShouldRestart()
        throws OfficeException, InterruptedException {

      final OfficeUrl url = new OfficeUrl(9999);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final LocalOfficeProcessManager processManager = mock(LocalOfficeProcessManager.class);
      given(processManager.getConnection()).willReturn(connection);
      final LocalOfficeManagerPoolEntry entry =
          new LocalOfficeManagerPoolEntry(2, DEFAULT_TASK_EXECUTION_TIMEOUT, processManager);
      entry.start();

      // Force a connection (it is usually done in LocalOfficeProcessManager#start).
      connection.connect();

      final OfficeTask task = mock(OfficeTask.class);
      entry.execute(task);
      entry.execute(task);

      // Wait a bit since reconnect use "Executor.execute"
      Thread.sleep(100L);

      verify(processManager, times(1)).restart();
      verify(task, times(2)).execute(any());
    }
  }

  @Nested
  class HandleExecuteTimeoutException {

    @Test
    void shouldRestartDueToTaskTimeout() throws OfficeException, InterruptedException {

      final OfficeUrl url = new OfficeUrl(9999);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final LocalOfficeProcessManager processManager = mock(LocalOfficeProcessManager.class);
      given(processManager.getConnection()).willReturn(connection);
      final LocalOfficeManagerPoolEntry entry =
          new LocalOfficeManagerPoolEntry(DEFAULT_MAX_TASKS_PER_PROCESS, 100L, processManager);
      entry.start();

      // Force a connection (it is usually done in LocalOfficeProcessManager#start).
      connection.connect();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(
              () ->
                  entry.execute(
                      context -> {
                        try {
                          Thread.sleep(250L);
                        } catch (InterruptedException ignored) {
                          // ignore
                        }
                      }));

      // Wait a bit since reconnect use "Executor.execute"
      Thread.sleep(100L);

      verify(processManager, times(1)).restartDueToTaskTimeout();
    }
  }

  @Nested
  class IsRunning {

    @Test
    void whenNotStarted_ReturnFalse() {

      final OfficeUrl url = new OfficeUrl(9999);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final LocalOfficeProcessManager processManager = mock(LocalOfficeProcessManager.class);
      given(processManager.getConnection()).willReturn(connection);
      final LocalOfficeManagerPoolEntry entry =
          new LocalOfficeManagerPoolEntry(
              DEFAULT_MAX_TASKS_PER_PROCESS, DEFAULT_TASK_EXECUTION_TIMEOUT, processManager);

      assertThat(entry.isRunning()).isFalse();
    }

    @Test
    void whenStartedButNotConnected_ReturnFalse() throws OfficeException {

      final OfficeUrl url = new OfficeUrl(9999);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final LocalOfficeProcessManager processManager = mock(LocalOfficeProcessManager.class);
      given(processManager.getConnection()).willReturn(connection);
      final LocalOfficeManagerPoolEntry entry =
          new LocalOfficeManagerPoolEntry(
              DEFAULT_MAX_TASKS_PER_PROCESS, DEFAULT_TASK_EXECUTION_TIMEOUT, processManager);
      entry.start();

      assertThat(entry.isRunning()).isFalse();
    }

    @Test
    void whenStartedAndConnected_ReturnTrue() throws OfficeException {

      final OfficeUrl url = new OfficeUrl(9999);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final LocalOfficeProcessManager processManager = mock(LocalOfficeProcessManager.class);
      given(processManager.getConnection()).willReturn(connection);
      final LocalOfficeManagerPoolEntry entry =
          new LocalOfficeManagerPoolEntry(
              DEFAULT_MAX_TASKS_PER_PROCESS, DEFAULT_TASK_EXECUTION_TIMEOUT, processManager);
      entry.start();

      // Force a connection (it is usually done in LocalOfficeProcessManager#start).
      connection.connect();

      assertThat(entry.isRunning()).isTrue();
    }
  }

  @Nested
  class DoStart {

    @Test
    void ShouldCallOfficeManagerStart() throws OfficeException {

      final OfficeUrl url = new OfficeUrl(9999);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final LocalOfficeProcessManager processManager = mock(LocalOfficeProcessManager.class);
      given(processManager.getConnection()).willReturn(connection);
      final LocalOfficeManagerPoolEntry entry =
          new LocalOfficeManagerPoolEntry(
              DEFAULT_MAX_TASKS_PER_PROCESS, DEFAULT_TASK_EXECUTION_TIMEOUT, processManager);
      entry.start();

      verify(processManager, times(1)).start();
    }
  }

  @Nested
  class DoStop {

    @Test
    void ShouldCallOfficeManagerStop() throws OfficeException {

      final OfficeUrl url = new OfficeUrl(9999);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final LocalOfficeProcessManager processManager = mock(LocalOfficeProcessManager.class);
      given(processManager.getConnection()).willReturn(connection);
      final LocalOfficeManagerPoolEntry entry =
          new LocalOfficeManagerPoolEntry(
              DEFAULT_MAX_TASKS_PER_PROCESS, DEFAULT_TASK_EXECUTION_TIMEOUT, processManager);
      entry.start();
      entry.stop();

      verify(processManager, times(1)).stop();
      assertThat(entry.isRunning()).isFalse();
    }
  }

  @Nested
  class Misc {

    @Test
    void whenUnexpectedDisconnection_ShouldRestartDueToLostConnection() throws OfficeException {

      final OfficeUrl url = new OfficeUrl(9999);
      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url);
      final LocalOfficeProcessManager processManager = mock(LocalOfficeProcessManager.class);
      given(processManager.getConnection()).willReturn(connection);
      final LocalOfficeManagerPoolEntry entry =
          new LocalOfficeManagerPoolEntry(
              DEFAULT_MAX_TASKS_PER_PROCESS, DEFAULT_TASK_EXECUTION_TIMEOUT, processManager);
      entry.start();

      // Force a connection (it is usually done in LocalOfficeProcessManager#start).
      connection.connect();

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

      // Check that restartDueToLostConnection has been called.
      verify(processManager, times(1)).restartDueToLostConnection();
    }
  }
}
