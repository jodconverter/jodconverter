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

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.RejectedExecutionException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeException;

/** Contains tests for the {@link ExternalOfficeConnectionManager} class. */
class ExternalOfficeConnectionManagerTest {

  @Nested
  class GetConnection {

    @Test
    void shouldReturnExpectedConnection() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url, false);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, true, connection);

      assertThat(manager.getConnection()).isEqualTo(connection);
    }
  }

  @Nested
  class Connect {

    @Test
    void whenConnectFailFastIsTrueAndCouldNotConnect_ShouldThrowOfficeException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareFailingConnectTest(url, false);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, true, connection);

      assertThatExceptionOfType(OfficeException.class).isThrownBy(manager::connect);
    }

    @Test
    void whenConnectFailFastIsTrueAndCouldConnect_ShouldNotThrowAnyException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url, false);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, true, connection);

      assertThatCode(manager::connect).doesNotThrowAnyException();
    }

    @Test
    void whenConnectFailFastIsTrueAndAlreadyConnected_ShouldNotThrowAnyException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareFailingConnectTest(url, true);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, true, connection);

      assertThatCode(manager::connect).doesNotThrowAnyException();
    }

    @Test
    void whenDisconnectedAndConnectFailFastIsTrue_ShouldThrowRejectedExecutionException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url, false);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, true, connection);

      assertThatCode(manager::disconnect).doesNotThrowAnyException();
      assertThatExceptionOfType(RejectedExecutionException.class).isThrownBy(manager::connect);
    }

    @Test
    void whenConnectFailFastIsFalseAndCouldNotConnect_ShouldNotThrowAnyException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareFailingConnectTest(url, false);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, false, connection);

      assertThatCode(manager::connect).doesNotThrowAnyException();
    }

    @Test
    void whenConnectFailFastIsFalseAndCouldConnect_ShouldNotThrowAnyException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url, false);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, false, connection);

      assertThatCode(manager::connect).doesNotThrowAnyException();
    }

    @Test
    void whenDisconnectedAndConnectFailFastIsFalse_ShouldThrowRejectedExecutionException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url, false);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, false, connection);

      assertThatCode(manager::disconnect).doesNotThrowAnyException();
      assertThatExceptionOfType(RejectedExecutionException.class).isThrownBy(manager::connect);
    }
  }

  @Nested
  class Disconnect {

    @Test
    void whenNotConnected_ShouldNotThrowAnyException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url, false);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, false, connection);

      assertThatCode(manager::disconnect).doesNotThrowAnyException();
    }

    @Test
    void whenConnected_ShouldDisconnect() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url, true);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, false, connection);

      assertThatCode(manager::disconnect).doesNotThrowAnyException();
    }

    //    @Test
    //    void whenTaskInterrupted_ShouldThrowOfficeException() {
    //
    //      final OfficeUrl url = new OfficeUrl(9999);
    //      final TestOfficeConnection connection = TestOfficeConnection.prepareTest(url, true);
    //      connection.setDisconnectSleep(1500L);
    //
    //      final ExternalOfficeConnectionManager manager =
    //          new ExternalOfficeConnectionManager(1000L, 1000L, false, connection);
    //
    //      final AtomicReference<OfficeException> ex = new AtomicReference<>();
    //
    //      assertThatCode(
    //              () -> {
    //                final Thread thread =
    //                    new Thread(
    //                        () -> {
    //                          try {
    //                            manager.disconnect();
    //                          } catch (OfficeException oe) {
    //                            ex.set(oe);
    //                          }
    //                        });
    //
    //                // Start the thread.
    //                thread.start();
    //                // Interrupt the thread.
    //                thread.interrupt();
    //                //  Wait for thread to complete.
    //                thread.join();
    //              })
    //          .doesNotThrowAnyException();
    //
    //      assertThat(ex.get())
    //          .isExactlyInstanceOf(OfficeException.class)
    //          .hasMessageStartingWith("Interruption while disconnecting from external office
    // process.")
    //          .hasCauseExactlyInstanceOf(InterruptedException.class);
    //    }
  }

  @Nested
  class Reconnect {

    @Test
    void whenCouldReconnect_ShouldNotThrowAnyException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url, true);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, false, connection);

      assertThatCode(manager::reconnect).doesNotThrowAnyException();
    }

    @Test
    void whenCouldNotReconnect_ShouldNotThrowAnyException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareFailingConnectTest(url, true);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, false, connection);

      assertThatCode(manager::reconnect).doesNotThrowAnyException();
    }
  }

  @Nested
  class EnsureConnected {

    @Test
    void whenNotConnected_ShouldConnect() throws OfficeException {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url, false);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, false, connection);

      manager.ensureConnected();
      assertThat(connection.isConnected()).isTrue();
    }

    @Test
    void whenAlreadyConnected_ShouldStayConnected() throws OfficeException {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url, true);

      final var manager = new ExternalOfficeConnectionManager(0L, 0L, false, connection);

      manager.ensureConnected();
      assertThat(connection.isConnected()).isTrue();
    }
  }

  static class TestOfficeConnection extends OfficeConnection {

    private final OfficeUrl url;
    private boolean isConnected;
    private boolean throwConnectException;

    static TestOfficeConnection prepareTest(final OfficeUrl url, final boolean isConnected) {

      final var conn = new TestOfficeConnection(url);
      conn.isConnected = isConnected;
      return conn;
    }

    static TestOfficeConnection prepareFailingConnectTest(
        final OfficeUrl url, final boolean isConnected) {

      final var conn = new TestOfficeConnection(url);
      conn.isConnected = isConnected;
      conn.throwConnectException = true;
      return conn;
    }

    private TestOfficeConnection(final OfficeUrl url) {
      super(url);

      this.url = url;
    }

    @Override
    public boolean isConnected() {
      return this.isConnected;
    }

    @Override
    public void connect() throws OfficeConnectionException {
      if (throwConnectException) {
        throw new OfficeConnectionException("Could not connect.", url.getConnectString());
      }
      this.isConnected = true;
    }

    @Override
    public void disconnect() {
      this.isConnected = false;
    }
  }
}
