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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeException;

/** Contains tests for the {@link AttachedOfficeConnectionManager} class. */
class AttachedOfficeConnectionManagerTest {

  private static final OfficeUrl URL = new OfficeUrl(9999);

  @Nested
  class GetConnection {

    @Test
    void shouldReturnExpectedConnection() {

      final var connection = TestOfficeConnection.prepareTest(URL, false);

      final var manager = new AttachedOfficeConnectionManager(0L, 0L, connection);

      assertThat(manager.getConnection()).isEqualTo(connection);
    }
  }

  @Nested
  class Connect {

    @Test
    void whenCouldNotConnect_ShouldThrowOfficeException() {

      final var connection = TestOfficeConnection.prepareFailingConnectTest(URL, false);

      final var manager = new AttachedOfficeConnectionManager(0L, 0L, connection);

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(manager::connect)
          .withMessage("Could not establish connection to external process.");
    }

    @Test
    void whenCouldConnect_ShouldConnect() throws OfficeException {

      final var connection = TestOfficeConnection.prepareTest(URL, false);

      final var manager = new AttachedOfficeConnectionManager(0L, 0L, connection);

      manager.connect();

      assertThat(connection.isConnected()).isTrue();
      assertThat(connection.connectCount).isEqualTo(1);
    }

    @Test
    void whenAlreadyConnected_ShouldNotConnectAgain() throws OfficeException {

      final var connection = TestOfficeConnection.prepareFailingConnectTest(URL, true);

      final var manager = new AttachedOfficeConnectionManager(0L, 0L, connection);

      manager.connect();

      assertThat(connection.isConnected()).isTrue();
      assertThat(connection.connectCount).isZero();
    }
  }

  @Nested
  class Disconnect {

    @Test
    void whenNotConnected_ShouldDoNothing() {

      final var connection = TestOfficeConnection.prepareTest(URL, false);

      final var manager = new AttachedOfficeConnectionManager(0L, 0L, connection);

      assertThatCode(manager::disconnect).doesNotThrowAnyException();
      assertThat(connection.disconnectCount).isZero();
    }

    @Test
    void whenConnected_ShouldDisconnect() {

      final var connection = TestOfficeConnection.prepareTest(URL, true);

      final var manager = new AttachedOfficeConnectionManager(0L, 0L, connection);

      manager.disconnect();

      assertThat(connection.isConnected()).isFalse();
      assertThat(connection.disconnectCount).isEqualTo(1);
    }
  }

  @Nested
  class Reconnect {

    @Test
    void whenCouldReconnect_ShouldDisconnectThenConnect() throws OfficeException {

      final var connection = TestOfficeConnection.prepareTest(URL, true);

      final var manager = new AttachedOfficeConnectionManager(0L, 0L, connection);

      manager.reconnect();

      assertThat(connection.isConnected()).isTrue();
      assertThat(connection.disconnectCount).isEqualTo(1);
      assertThat(connection.connectCount).isEqualTo(1);
    }

    @Test
    void whenCouldNotReconnect_ShouldThrowOfficeException() {

      final var connection = TestOfficeConnection.prepareFailingConnectTest(URL, true);

      final var manager = new AttachedOfficeConnectionManager(0L, 0L, connection);

      assertThatExceptionOfType(OfficeException.class).isThrownBy(manager::reconnect);
      assertThat(connection.isConnected()).isFalse();
    }
  }

  /** A connection that does not connect to anything. */
  static final class TestOfficeConnection extends OfficeConnection {

    private final OfficeUrl url;
    private boolean isConnected;
    private boolean throwConnectException;
    /* default */ int connectCount;
    /* default */ int disconnectCount;

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
      connectCount++;
      if (throwConnectException) {
        throw new OfficeConnectionException("Could not connect.", url.getConnectString());
      }
      this.isConnected = true;
    }

    @Override
    public void disconnect() {
      disconnectCount++;
      this.isConnected = false;
    }
  }
}
