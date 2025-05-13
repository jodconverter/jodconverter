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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_AFTER_START_PROCESS_DELAY;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_EXISTING_PROCESS_ACTION;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_KEEP_ALIVE_ON_SHUTDOWN;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_PROCESS_RETRY_INTERVAL;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_PROCESS_TIMEOUT;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_START_FAIL_FAST;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.process.ProcessManager;
import org.jodconverter.local.process.ProcessQuery;

/** Contains tests that use reflection for the {@link LocalOfficeProcessManager} class. */
class LocalOfficeProcessManagerReflectTest {

  @Test
  void checkForExistingProcess_WhenIOExceptionCatched_ShouldTrowOfficeException() {

    final OfficeUrl url = new OfficeUrl(9999);
    final OfficeConnection connection = TestOfficeConnection.prepareTest(url);
    final LocalOfficeProcessManager manager =
        new LocalOfficeProcessManager(
            url,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            new ProcessManager() {
              @Override
              public void kill(final Process process, final long pid) throws IOException {
                throw new IOException();
              }

              @Override
              @SuppressWarnings("NullableProblems")
              public long findPid(final ProcessQuery query) throws IOException {
                throw new IOException();
              }
            },
            new ArrayList<>(),
            null,
            DEFAULT_PROCESS_TIMEOUT,
            DEFAULT_PROCESS_RETRY_INTERVAL,
            DEFAULT_AFTER_START_PROCESS_DELAY,
            DEFAULT_EXISTING_PROCESS_ACTION,
            DEFAULT_START_FAIL_FAST,
            DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
            connection);

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(
            () -> {
              try {
                ReflectionTestUtils.invokeMethod(
                    manager,
                    "checkForExistingProcess",
                    new ProcessQuery("command", "acceptString"));
              } catch (UndeclaredThrowableException e) {
                throw e.getUndeclaredThrowable();
              }
            })
        .withMessage(
            "Could not check if there is already an existing process with --accept 'acceptString'")
        .withCauseExactlyInstanceOf(IOException.class);
  }

  @Test
  void forciblyTerminateProcess_WhenIoExceptionCatched_ShouldLogError() {

    final OfficeUrl url = new OfficeUrl(9999);
    final OfficeConnection connection = TestOfficeConnection.prepareTest(url);
    final LocalOfficeProcessManager manager =
        new LocalOfficeProcessManager(
            url,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            new ProcessManager() {
              @Override
              public void kill(final Process process, final long pid) throws IOException {
                throw new IOException();
              }
            },
            new ArrayList<>(),
            null,
            DEFAULT_PROCESS_TIMEOUT,
            DEFAULT_PROCESS_RETRY_INTERVAL,
            DEFAULT_AFTER_START_PROCESS_DELAY,
            DEFAULT_EXISTING_PROCESS_ACTION,
            DEFAULT_START_FAIL_FAST,
            DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
            connection);

    // TODO: Check that the error message if properly logged.
    assertThatCode(
            () -> {
              final VerboseProcess verboseProcess = mock(VerboseProcess.class);
              ReflectionTestUtils.setField(manager, "pid", 0L);
              ReflectionTestUtils.setField(manager, "process", verboseProcess);
              ReflectionTestUtils.invokeMethod(manager, "forciblyTerminateProcess");
            })
        .doesNotThrowAnyException();
  }

  @Test
  void forciblyTerminateProcess_WhenNotStarted_ShouldDoNothing() {

    final OfficeUrl url = new OfficeUrl(9999);
    final OfficeConnection connection = TestOfficeConnection.prepareTest(url);
    final LocalOfficeProcessManager manager =
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

    assertThatCode(() -> ReflectionTestUtils.invokeMethod(manager, "forciblyTerminateProcess"))
        .doesNotThrowAnyException();
  }
}
