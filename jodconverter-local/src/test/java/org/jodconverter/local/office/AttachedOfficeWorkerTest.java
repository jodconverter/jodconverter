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
import static org.jodconverter.local.office.AttachedOfficeManager.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.OfficeTask;

/** Contains tests for the {@link AttachedOfficeWorker} class. */
class AttachedOfficeWorkerTest {

  private TestOfficeConnection connection;

  @BeforeEach
  void setUp() {
    connection = TestOfficeConnection.prepareTest(new OfficeUrl(2002));
  }

  private AttachedOfficeWorker newWorker(
      final boolean connectOnStart, final int maxTasksPerConnection) {
    return new AttachedOfficeWorker(
        connectOnStart,
        maxTasksPerConnection,
        new AttachedOfficeConnectionManager(
            DEFAULT_CONNECT_TIMEOUT, DEFAULT_CONNECT_RETRY_INTERVAL, connection));
  }

  private AttachedOfficeWorker newWorker(final boolean connectOnStart) {
    return newWorker(connectOnStart, DEFAULT_MAX_TASKS_PER_CONNECTION);
  }

  @Nested
  class Start {

    @Test
    void whenConnectOnStart_ShouldConnect() throws OfficeException {

      final var worker = newWorker(true);

      worker.start();

      assertThat(connection.isConnected()).isTrue();
      assertThat(worker.isReady()).isTrue();
    }

    @Test
    void whenNotConnectOnStart_ShouldNotConnectAndBeReady() throws OfficeException {

      final var worker = newWorker(false);

      worker.start();

      assertThat(connection.isConnected()).isFalse();
      assertThat(worker.isReady()).isTrue();
    }

    @Test
    void whenCouldNotConnect_ShouldThrowOfficeException() {

      final var failing =
          new OfficeConnection(new OfficeUrl(2002)) {
            @Override
            public void connect() throws OfficeConnectionException {
              throw new OfficeConnectionException("Test", "Test");
            }
          };
      final var worker =
          new AttachedOfficeWorker(
              true,
              DEFAULT_MAX_TASKS_PER_CONNECTION,
              new AttachedOfficeConnectionManager(0L, 0L, failing));

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(worker::start)
          .withMessage("Could not establish connection to external process.");
      assertThat(worker.isReady()).isFalse();
    }
  }

  @Nested
  class IsReady {

    @Test
    void whenNotStarted_ShouldReturnFalse() {

      assertThat(newWorker(true).isReady()).isFalse();
    }

    @Test
    void whenConnectionLost_ShouldReturnFalse() throws OfficeException {

      final var worker = newWorker(true);
      worker.start();

      connection.disconnect();

      assertThat(worker.isReady()).isFalse();
    }

    @Test
    void whenMaxTasksPerConnectionReached_ShouldReturnFalse() throws OfficeException {

      final var worker = newWorker(true, 2);
      worker.start();

      worker.execute(context -> {});
      assertThat(worker.isReady()).isTrue();
      worker.execute(context -> {});

      // The pool reconnects the worker before giving it another task.
      assertThat(worker.isReady()).isFalse();
    }

    @Test
    void whenNoMaxTasksPerConnection_ShouldStayReady() throws OfficeException {

      final var worker = newWorker(true, 0);
      worker.start();

      for (var i = 0; i < 5; i++) {
        worker.execute(context -> {});
      }

      assertThat(worker.isReady()).isTrue();
    }
  }

  @Nested
  class Execute {

    @Test
    void whenNotConnected_ShouldConnectFirstThenExecuteTask() throws OfficeException {

      final var worker = newWorker(false);
      worker.start();
      final var task = mock(OfficeTask.class);

      worker.execute(task);

      assertThat(connection.getConnectCount()).isEqualTo(1);
      verify(task).execute(connection);
    }

    @Test
    void whenConnected_ShouldNotConnectAgain() throws OfficeException {

      final var worker = newWorker(true);
      worker.start();

      worker.execute(context -> {});

      assertThat(connection.getConnectCount()).isEqualTo(1);
    }

    @Test
    void whenTheTaskFails_ShouldThrowItsExceptionAndNotCountTheTask() throws OfficeException {

      final var worker = newWorker(true, 1);
      worker.start();
      final var failure = new OfficeException("The task failed");

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(
              () ->
                  worker.execute(
                      context -> {
                        throw failure;
                      }))
          .isSameAs(failure);

      assertThat(worker.isReady()).isTrue();
    }

    @Test
    void whenConnectionLostWhileExecuting_ShouldInterruptTheTask() throws OfficeException {

      final var worker = newWorker(true);
      worker.start();
      final var interrupted = new boolean[1];

      worker.execute(
          context -> {
            connection.disconnect();
            // Also clears the interrupted status of the test thread.
            interrupted[0] = Thread.interrupted();
          });

      assertThat(interrupted[0]).isTrue();
    }

    @Test
    void whenConnectionLostWhileIdle_ShouldNotInterruptAnything() throws OfficeException {

      final var worker = newWorker(true);
      worker.start();
      worker.execute(context -> {});

      connection.disconnect();

      assertThat(Thread.interrupted()).isFalse();
    }
  }

  @Nested
  class Restart {

    @Test
    void whenMaxTasksPerConnectionReached_ShouldReconnectAndCountAgain() throws OfficeException {

      final var worker = newWorker(true, 1);
      worker.start();
      worker.execute(context -> {});
      assertThat(worker.isReady()).isFalse();

      worker.restart();

      assertThat(connection.getConnectCount()).isEqualTo(2);
      assertThat(connection.getDisconnectCount()).isEqualTo(1);
      assertThat(worker.isReady()).isTrue();
    }

    @Test
    void whenConnectionLost_ShouldReconnect() throws OfficeException {

      final var worker = newWorker(true);
      worker.start();
      connection.disconnect();

      worker.restart();

      assertThat(connection.getConnectCount()).isEqualTo(2);
      assertThat(worker.isReady()).isTrue();
    }

    @Test
    void whenNotConnectOnStartAndNeverConnected_ShouldConnect() throws OfficeException {

      final var worker = newWorker(false);
      worker.start();

      worker.restart();

      assertThat(connection.isConnected()).isTrue();
      assertThat(worker.isReady()).isTrue();
    }
  }

  @Nested
  class Abort {

    @Test
    void shouldDisconnect() throws OfficeException {

      final var worker = newWorker(true);
      worker.start();

      worker.abort();

      assertThat(connection.isConnected()).isFalse();
      assertThat(worker.isReady()).isFalse();
    }
  }

  @Nested
  class Stop {

    @Test
    void shouldDisconnect() throws OfficeException {

      final var worker = newWorker(true);
      worker.start();

      worker.stop();

      assertThat(connection.isConnected()).isFalse();
      assertThat(connection.getDisconnectCount()).isEqualTo(1);
    }

    @Test
    void whenNotConnected_ShouldDoNothing() {

      newWorker(true).stop();

      assertThat(connection.getDisconnectCount()).isZero();
    }
  }
}
