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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_AFTER_START_PROCESS_DELAY;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_DISABLE_OPENGL;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_EXISTING_PROCESS_ACTION;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_KEEP_ALIVE_ON_SHUTDOWN;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_PROCESS_RETRY_INTERVAL;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_PROCESS_TIMEOUT;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.task.SimpleOfficeTask;
import org.jodconverter.core.test.util.TestUtil;

/** Contains tests for the {@link ExternalOfficeManager} class. */
class ExternalOfficeManagerITest {

  private static final OfficeUrl CONNECT_URL = new OfficeUrl(2002);
  private static final long START_WAIT_TIMEOUT = 15_000L; // 30 Seconds.

  private static LocalOfficeProcessManager manager;

  private static LocalOfficeProcessManager startOfficeProcess() throws OfficeException {

    final long start = System.currentTimeMillis();

    // Starts an office process
    final OfficeConnection connection = new OfficeConnection(CONNECT_URL);

    final LocalOfficeProcessManager manager =
        new LocalOfficeProcessManager(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            new ArrayList<>(),
            null,
            DEFAULT_PROCESS_TIMEOUT,
            DEFAULT_PROCESS_RETRY_INTERVAL,
            DEFAULT_AFTER_START_PROCESS_DELAY,
            DEFAULT_EXISTING_PROCESS_ACTION,
            true,
            DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
            DEFAULT_DISABLE_OPENGL,
            connection);
    manager.start();
    final OfficeConnection conn =
        (OfficeConnection) ReflectionTestUtils.getField(manager, "connection");
    final long limit = start + START_WAIT_TIMEOUT;
    while (System.currentTimeMillis() < limit) {
      if (conn.isConnected()) {
        break;
      }

      // Wait a sec
      TestUtil.sleepQuietly(1_000L);
    }
    return manager;
  }

  @BeforeAll
  public static void setUp() throws OfficeException {

    // Starts an office process
    manager = startOfficeProcess();
  }

  @AfterAll
  public static void tearDown() throws OfficeException {

    manager.stop();
  }

  @Test
  void execute_WhenProcessDoesNotExist_ShouldFailed() {

    final OfficeManager manager =
        ExternalOfficeManager.builder()
            .portNumbers(65_530)
            .connectTimeout(3_000L)
            .connectRetryInterval(1_000L)
            .connectFailFast(true)
            .build();

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(manager::start)
        .withMessage("Could not establish connection to external process.");
  }

  @ParameterizedTest
  @ValueSource(strings = {"localhost", "127.0.0.1"})
  void execute_WhenProcessExists_ShouldSucceed(final String host) {
    final OfficeManager manager =
        ExternalOfficeManager.builder()
            .hostName(host)
            .portNumbers(2002)
            .connectFailFast(true)
            .build();

    final SimpleOfficeTask task = new SimpleOfficeTask();
    assertThatCode(
            () -> {
              try {
                manager.start();
                manager.execute(task);
              } finally {
                manager.stop();
              }
            })
        .doesNotThrowAnyException();
    assertThat(task.isCompleted()).isTrue();
  }
}
