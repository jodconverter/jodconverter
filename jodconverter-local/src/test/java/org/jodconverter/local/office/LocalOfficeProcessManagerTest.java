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
import static org.jodconverter.local.office.LocalOfficeManager.*;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;

/** Contains tests for the {@link LocalOfficeProcessManager} class. */
class LocalOfficeProcessManagerTest {

  @BeforeEach
  void setUpOfficeHome() {
    System.setProperty("office.home", new File("src/test/resources/oohome").getPath());
  }

  @AfterEach
  void tearDown() {
    System.setProperty("office.home", "");
  }

  @Nested
  class GetConnection {

    @Test
    void shouldReturnExpectedConnection() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url);

      final var manager =
          new LocalOfficeProcessManager(
              url,
              LocalOfficeUtils.getDefaultOfficeHome(),
              OfficeUtils.getDefaultWorkingDir(),
              LocalOfficeUtils.findBestProcessManager(),
              new ArrayList<>(),
              null,
              DEFAULT_PROCESS_TIMEOUT,
              DEFAULT_PROCESS_RETRY_INTERVAL,
              DEFAULT_AFTER_START_PROCESS_DELAY,
              DEFAULT_EXISTING_PROCESS_ACTION,
              DEFAULT_START_FAIL_FAST,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              connection);

      assertThat(manager.getConnection()).isEqualTo(connection);
    }
  }

  @Nested
  class Start {

    @Test
    void whenStartFailFastIsTrueAndCouldNotStart_ShouldThrowOfficeException() {

      final var url = new OfficeUrl(9999);

      final var manager =
          new LocalOfficeProcessManager(
              url,
              LocalOfficeUtils.getDefaultOfficeHome(),
              OfficeUtils.getDefaultWorkingDir(),
              LocalOfficeUtils.findBestProcessManager(),
              new ArrayList<>(),
              null,
              1000L,
              1000L,
              DEFAULT_AFTER_START_PROCESS_DELAY,
              DEFAULT_EXISTING_PROCESS_ACTION,
              true,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              new OfficeConnection(url) {
                @Override
                public void connect() throws OfficeConnectionException {
                  throw new OfficeConnectionException("Test", "Test");
                }
              });

      assertThatExceptionOfType(OfficeException.class).isThrownBy(manager::start);
    }

    @Test
    void whenStoppedAndStartFailFastIsTrue_ShouldThrowRejectedExecutionException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url);

      final var manager =
          new LocalOfficeProcessManager(
              url,
              LocalOfficeUtils.getDefaultOfficeHome(),
              OfficeUtils.getDefaultWorkingDir(),
              LocalOfficeUtils.findBestProcessManager(),
              new ArrayList<>(),
              null,
              0L,
              0L,
              DEFAULT_AFTER_START_PROCESS_DELAY,
              DEFAULT_EXISTING_PROCESS_ACTION,
              true,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              connection);

      assertThatCode(manager::stop).doesNotThrowAnyException();
      assertThatExceptionOfType(RejectedExecutionException.class).isThrownBy(manager::start);
    }

    @Test
    void whenStartFailFastIsFalseAndCouldNotStart_ShouldNotThrowAnyException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url);

      final var manager =
          new LocalOfficeProcessManager(
              url,
              LocalOfficeUtils.getDefaultOfficeHome(),
              OfficeUtils.getDefaultWorkingDir(),
              LocalOfficeUtils.findBestProcessManager(),
              new ArrayList<>(),
              null,
              0L,
              0L,
              DEFAULT_AFTER_START_PROCESS_DELAY,
              DEFAULT_EXISTING_PROCESS_ACTION,
              false,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              connection);

      assertThatCode(manager::start).doesNotThrowAnyException();
    }

    @Test
    void whenStoppedAndStartFailFastIsFalse_ShouldThrowRejectedExecutionException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url);

      final var manager =
          new LocalOfficeProcessManager(
              url,
              LocalOfficeUtils.getDefaultOfficeHome(),
              OfficeUtils.getDefaultWorkingDir(),
              LocalOfficeUtils.findBestProcessManager(),
              new ArrayList<>(),
              null,
              0L,
              0L,
              DEFAULT_AFTER_START_PROCESS_DELAY,
              DEFAULT_EXISTING_PROCESS_ACTION,
              false,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              connection);

      assertThatCode(manager::stop).doesNotThrowAnyException();
      assertThatExceptionOfType(RejectedExecutionException.class).isThrownBy(manager::start);
    }
  }

  @Nested
  class Stopped {

    @Test
    void whenNotStarted_ShouldNotThrowAnyException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url);

      final var manager =
          new LocalOfficeProcessManager(
              url,
              LocalOfficeUtils.getDefaultOfficeHome(),
              OfficeUtils.getDefaultWorkingDir(),
              LocalOfficeUtils.findBestProcessManager(),
              new ArrayList<>(),
              null,
              0L,
              0L,
              DEFAULT_AFTER_START_PROCESS_DELAY,
              DEFAULT_EXISTING_PROCESS_ACTION,
              false,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              connection);

      assertThatCode(manager::stop).doesNotThrowAnyException();
    }
  }

  @Nested
  class Reconnect {

    @Test
    void whenCouldNotRestart_ShouldNotThrowAnyException() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url);

      final var manager =
          new LocalOfficeProcessManager(
              url,
              LocalOfficeUtils.getDefaultOfficeHome(),
              OfficeUtils.getDefaultWorkingDir(),
              LocalOfficeUtils.findBestProcessManager(),
              new ArrayList<>(),
              null,
              0L,
              0L,
              DEFAULT_AFTER_START_PROCESS_DELAY,
              DEFAULT_EXISTING_PROCESS_ACTION,
              false,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              connection);

      assertThatCode(manager::restart).doesNotThrowAnyException();
    }
  }
}
