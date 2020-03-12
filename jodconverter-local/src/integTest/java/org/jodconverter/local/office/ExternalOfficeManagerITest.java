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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.office.SimpleOfficeTask;
import org.jodconverter.core.test.util.TestUtil;

/** Contains tests for the {@link ExternalOfficeManager} class. */
public class ExternalOfficeManagerITest {

  private static final OfficeUrl CONNECT_URL = new OfficeUrl(2002);

  private static OfficeProcess process;

  private static OfficeProcess startOfficeProcess() throws OfficeException {

    // Starts an office process
    final OfficeProcess process =
        new OfficeProcess(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            LocalOfficeUtils.findBestProcessManager(),
            null,
            null,
            null);
    process.start();
    TestUtil.sleepQuietly(2_000L);
    final Integer exitCode = Objects.requireNonNull(process.getProcess()).getExitCode();
    if (exitCode != null && exitCode.equals(81)) {
      process.start(true);
      TestUtil.sleepQuietly(2_000L);
    }
    return process;
  }

  @BeforeAll
  public static void setUp() throws OfficeException {

    // Starts an office process
    process = startOfficeProcess();
  }

  @AfterAll
  public static void tearDown() {

    process.forciblyTerminate();
    process.deleteInstanceProfileDir();
  }

  @Test
  public void execute_WhenProcessDoesNotExist_ShouldThrowOfficeException() throws OfficeException {

    final OfficeManager manager =
        ExternalOfficeManager.builder()
            .portNumber(65_530)
            .connectOnStart(false)
            .connectTimeout(3_000L)
            .retryInterval(1_000L)
            .build();
    try {
      manager.start();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> manager.execute(new SimpleOfficeTask()))
          .withMessage("Could not establish connection to external office process");

    } finally {
      manager.stop();
    }
  }

  @Test
  public void execute_WhenProcessDoesNotExistAndConnectOnStart_ShouldThrowOfficeException() {

    final OfficeManager manager =
        ExternalOfficeManager.builder()
            .portNumber(65_530)
            .connectTimeout(3_000L)
            .retryInterval(1_000L)
            .build();

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(manager::start)
        .withMessage("Could not establish connection to external office process");
  }

  @Test
  public void execute_WhenProcessExists_ShouldSucceed() {

    final OfficeManager manager =
        ExternalOfficeManager.builder().portNumber(2002).connectOnStart(false).build();

    assertThatCode(
            () -> {
              try {
                manager.start();
                manager.execute(new SimpleOfficeTask());
              } finally {
                manager.stop();
              }
            })
        .doesNotThrowAnyException();
  }

  @Test
  public void execute_WhenProcessExistsAndConnectOnStart_ShouldSucceed() {

    final OfficeManager manager = ExternalOfficeManager.builder().portNumber(2002).build();

    assertThatCode(
            () -> {
              try {
                manager.start();
                manager.execute(new SimpleOfficeTask());
              } finally {
                manager.stop();
              }
            })
        .doesNotThrowAnyException();
  }

  @Test
  public void stop_WhenNotStarted_DoNothing() {

    final OfficeManager manager =
        ExternalOfficeManager.builder()
            .portNumber(65_530)
            .connectTimeout(3_000L)
            .retryInterval(1_000L)
            .build();

    assertThatCode(manager::stop).doesNotThrowAnyException();
  }
}
