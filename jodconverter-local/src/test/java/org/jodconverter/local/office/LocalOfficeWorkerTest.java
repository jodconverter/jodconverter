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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.local.task.PasswordProtectedExceptionSupportTask;

/** Contains tests for the {@link LocalOfficeWorker} class. */
class LocalOfficeWorkerTest {

  private TestOfficeConnection connection;
  private LocalOfficeProcessManager processManager;

  @BeforeEach
  void setUp() {
    connection = TestOfficeConnection.prepareTest(new OfficeUrl(9999));
    processManager = mock(LocalOfficeProcessManager.class);
    given(processManager.getConnection()).willReturn(connection);
  }

  private LocalOfficeWorker newWorker(final int maxTasksPerProcess) {
    return new LocalOfficeWorker(maxTasksPerProcess, processManager);
  }

  /** A task that fails after a password interaction request, as LibreOffice 24+ makes it fail. */
  private static class PasswordTask implements OfficeTask, PasswordProtectedExceptionSupportTask {

    @Override
    public void execute(final OfficeContext context) throws OfficeException {
      throw new OfficeException("The document is password protected");
    }

    @Override
    public boolean hasPasswordInteractionRequest() {
      return true;
    }
  }

  /** A task that loses its connection while it is executed. */
  private class DisconnectedTask implements OfficeTask, PasswordProtectedExceptionSupportTask {

    private final boolean passwordInteraction;
    /* default */ boolean interrupted;

    /* default */ DisconnectedTask(final boolean passwordInteraction) {
      this.passwordInteraction = passwordInteraction;
    }

    @Override
    public void execute(final OfficeContext context) {
      connection.disconnect();
      // Also clears the interrupted status of the test thread.
      interrupted = Thread.interrupted();
    }

    @Override
    public boolean hasPasswordInteractionRequest() {
      return passwordInteraction;
    }
  }

  @Nested
  class Start {

    @Test
    void shouldStartTheOfficeProcess() throws OfficeException {

      newWorker(2).start();

      verify(processManager).start();
    }
  }

  @Nested
  class IsReady {

    @Test
    void whenNotConnected_ShouldReturnFalse() {

      assertThat(newWorker(2).isReady()).isFalse();
    }

    @Test
    void whenConnected_ShouldReturnTrue() {

      // Force a connection (it is usually done in LocalOfficeProcessManager#start).
      connection.connect();

      assertThat(newWorker(2).isReady()).isTrue();
    }

    @Test
    void whenMaxTasksPerProcessReached_ShouldReturnFalse() throws OfficeException {

      final var worker = newWorker(2);
      connection.connect();

      worker.execute(context -> {});
      assertThat(worker.isReady()).isTrue();
      worker.execute(context -> {});

      // The pool restarts the worker before giving it another task.
      assertThat(worker.isReady()).isFalse();
    }

    @Test
    void whenNoMaxTasksPerProcess_ShouldStayReady() throws OfficeException {

      final var worker = newWorker(0);
      connection.connect();

      for (int i = 0; i < 5; i++) {
        worker.execute(context -> {});
      }

      assertThat(worker.isReady()).isTrue();
    }
  }

  @Nested
  class Execute {

    @Test
    void shouldExecuteTheTaskWithTheConnection() throws OfficeException {

      final OfficeTask task = mock(OfficeTask.class);

      newWorker(2).execute(task);

      verify(task).execute(connection);
    }

    @Test
    void whenTheTaskFails_ShouldThrowItsExceptionAndNotCountTheTask() {

      final var worker = newWorker(1);
      connection.connect();
      final var failure = new OfficeException("The task failed");

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(
              () ->
                  worker.execute(
                      context -> {
                        throw failure;
                      }))
          .isSameAs(failure);

      // Only the tasks that succeed count for the maximum number of tasks.
      assertThat(worker.isReady()).isTrue();
    }
  }

  @Nested
  class Disconnected {

    @Test
    void whileExecutingATask_ShouldInterruptTheTask() throws OfficeException {

      final var worker = newWorker(2);
      connection.connect();
      final boolean[] interrupted = new boolean[1];

      // The task may be doing something else than waiting for the office process.
      worker.execute(
          context -> {
            connection.disconnect();
            interrupted[0] = Thread.interrupted();
          });

      assertThat(interrupted[0]).isTrue();
    }

    @Test
    void whileExecutingATaskWithoutPasswordInteraction_ShouldInterruptTheTask()
        throws OfficeException {

      final var worker = newWorker(2);
      connection.connect();
      final var task = new DisconnectedTask(false);

      worker.execute(task);

      assertThat(task.interrupted).isTrue();
    }

    @Test
    void whileExecutingATaskWithPasswordInteraction_ShouldNotInterruptTheTask()
        throws OfficeException {

      final var worker = newWorker(2);
      connection.connect();
      final var task = new DisconnectedTask(true);

      // The task fails by itself, with a PasswordProtectedException.
      worker.execute(task);

      assertThat(task.interrupted).isFalse();
    }

    @Test
    void whileIdle_ShouldNotInterruptAnything() throws OfficeException {

      final var worker = newWorker(2);
      connection.connect();
      worker.execute(context -> {});

      connection.disconnect();
      connection.connect();

      assertThat(Thread.interrupted()).isFalse();
      assertThat(worker.isReady()).isTrue();
    }
  }

  @Nested
  class Restart {

    @Test
    void whenMaxTasksPerProcessReached_ShouldRestartKeepingTheProfileAndCountAgain()
        throws OfficeException {

      final var worker = newWorker(1);
      connection.connect();
      worker.execute(context -> {});
      assertThat(worker.isReady()).isFalse();

      worker.restart();

      verify(processManager).restart();
      verify(processManager, never()).restartDueToLostConnection();
      assertThat(worker.isReady()).isTrue();
    }

    @Test
    void whenConnectionLost_ShouldRestartDueToLostConnection() throws OfficeException {

      final var worker = newWorker(2);
      connection.connect();
      connection.disconnect();
      assertThat(worker.isReady()).isFalse();

      worker.restart();

      verify(processManager).restartDueToLostConnection();
      verify(processManager, never()).restart();
    }

    @Test
    void whenStartFailed_ShouldRestartDueToLostConnection() throws OfficeException {

      final var worker = newWorker(2);

      // Never connected: whatever the start left must be cleaned up.
      worker.restart();

      verify(processManager).restartDueToLostConnection();
    }

    @Test
    void afterAbort_ShouldRestartDueToLostConnectionEvenIfStillConnected() throws OfficeException {

      final var worker = newWorker(2);
      connection.connect();

      // The process was just killed, and the connection is not closed yet.
      worker.abort();
      worker.restart();
      // The next restart is not concerned by the abort anymore.
      worker.restart();

      final var inOrder = inOrder(processManager);
      inOrder.verify(processManager).kill();
      inOrder.verify(processManager).restartDueToLostConnection();
      inOrder.verify(processManager).restart();
    }

    @Test
    void whenDisconnectedByPasswordInteraction_ShouldRestartKeepingTheProfile()
        throws OfficeException {

      final var worker = newWorker(2);
      connection.connect();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> worker.execute(new PasswordTask()));
      // LibreOffice 24+ closes the connection when a password is requested and not provided.
      connection.disconnect();
      assertThat(worker.isReady()).isFalse();

      worker.restart();
      // The next restart is not concerned by the password interaction anymore.
      worker.restart();

      final var inOrder = inOrder(processManager);
      inOrder.verify(processManager).restart();
      inOrder.verify(processManager).restartDueToLostConnection();
    }

    @Test
    void whenConnectionLostAfterAPasswordInteractionTheProcessSurvived_ShouldNotKeepTheProfile()
        throws OfficeException {

      final var worker = newWorker(2);
      connection.connect();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> worker.execute(new PasswordTask()));
      // Older versions stay connected after a password interaction.
      assertThat(worker.isReady()).isTrue();
      // Later, the office process crashes.
      connection.disconnect();

      worker.restart();

      verify(processManager).restartDueToLostConnection();
      verify(processManager, never()).restart();
    }

    @Test
    void whenAbortedDuringAPasswordInteraction_ShouldRestartDueToLostConnection()
        throws OfficeException {

      final var worker = newWorker(2);
      connection.connect();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> worker.execute(new PasswordTask()));
      worker.abort();
      connection.disconnect();

      worker.restart();

      verify(processManager).restartDueToLostConnection();
      verify(processManager, never()).restart();
    }
  }

  @Nested
  class Abort {

    @Test
    void shouldKillTheOfficeProcess() {

      newWorker(2).abort();

      verify(processManager).kill();
    }
  }

  @Nested
  class Stop {

    @Test
    void shouldStopTheOfficeProcess() {

      newWorker(2).stop();

      verify(processManager).stop();
    }
  }
}
