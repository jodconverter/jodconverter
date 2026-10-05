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
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_AFTER_START_PROCESS_DELAY;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_EXISTING_PROCESS_ACTION;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_KEEP_ALIVE_ON_SHUTDOWN;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_PROCESS_RETRY_INTERVAL;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_PROCESS_TIMEOUT;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_START_FAIL_FAST;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
  void restartDueToLostConnection_WhenProcessStillRunning_ShouldKillItWithoutWaitingProcessTimeout(
      final @TempDir File testFolder) throws InterruptedException {

    final OfficeUrl url = new OfficeUrl(9999);
    final OfficeConnection connection = TestOfficeConnection.prepareTest(url);
    final CountDownLatch killed = new CountDownLatch(1);
    final LocalOfficeProcessManager manager =
        new LocalOfficeProcessManager(
            url,
            // No office here: the restart that follows the kill fails right away
            testFolder,
            testFolder,
            new ProcessManager() {
              @Override
              public void kill(final Process process, final long pid) {
                killed.countDown();
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

    // A process that never exits by itself (no exit code)
    final VerboseProcess verboseProcess = mock(VerboseProcess.class);
    given(verboseProcess.getExitCode()).willReturn(null);
    ReflectionTestUtils.setField(manager, "pid", 0L);
    ReflectionTestUtils.setField(manager, "process", verboseProcess);

    ReflectionTestUtils.invokeMethod(manager, "restartDueToLostConnection");

    // Killed after the short grace period, far before the process timeout
    assertThat(DEFAULT_PROCESS_TIMEOUT).isGreaterThan(10_000L);
    assertThat(killed.await(10L, TimeUnit.SECONDS)).isTrue();
  }

  @Test
  void prepareProcessBuilder_ShouldPointTempDirectoriesIntoInstanceProfileDir(
      final @TempDir File testFolder) {

    final OfficeUrl url = new OfficeUrl(9999);
    final LocalOfficeProcessManager manager =
        new LocalOfficeProcessManager(
            url,
            testFolder,
            testFolder,
            LocalOfficeUtils.findBestProcessManager(),
            new ArrayList<>(),
            null,
            DEFAULT_PROCESS_TIMEOUT,
            DEFAULT_PROCESS_RETRY_INTERVAL,
            DEFAULT_AFTER_START_PROCESS_DELAY,
            DEFAULT_EXISTING_PROCESS_ACTION,
            DEFAULT_START_FAIL_FAST,
            DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
            TestOfficeConnection.prepareTest(url));
    ReflectionTestUtils.setField(
        manager, "descriptor", OfficeDescriptor.fromExecutablePath("soffice"));

    final ProcessBuilder processBuilder =
        ReflectionTestUtils.invokeMethod(manager, "prepareProcessBuilder", "acceptString");

    final File tempDir = manager.getInstanceTempDir();
    assertThat(tempDir)
        .isDirectory()
        .hasParent((File) ReflectionTestUtils.getField(manager, "instanceProfileDir"));
    assertThat(processBuilder)
        .isNotNull()
        .extracting(ProcessBuilder::environment)
        .satisfies(
            env ->
                assertThat(env)
                    .containsEntry("TMPDIR", tempDir.getAbsolutePath())
                    .containsEntry("TMP", tempDir.getAbsolutePath())
                    .containsEntry("TEMP", tempDir.getAbsolutePath()));
  }

  @Test
  void checkPortAvailable_WhenAnotherProgramListens_ShouldThrowOfficeException(
      final @TempDir File testFolder) throws IOException {

    // Another program listening on the port (Tomcat for example)
    try (ServerSocket otherProgram = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
      final int port = otherProgram.getLocalPort();
      final LocalOfficeProcessManager manager = newManager(new OfficeUrl(port), testFolder);

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(
              () -> {
                try {
                  ReflectionTestUtils.invokeMethod(manager, "checkPortAvailable", "acceptString");
                } catch (UndeclaredThrowableException e) {
                  throw e.getUndeclaredThrowable();
                }
              })
          .withMessageStartingWith(
              "Port " + port + " on host '127.0.0.1' is already used by another program");
    }
  }

  @Test
  void checkPortAvailable_WhenPortFree_ShouldDoNothing(final @TempDir File testFolder)
      throws IOException {

    final int port;
    try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
      port = socket.getLocalPort();
    }
    final LocalOfficeProcessManager manager = newManager(new OfficeUrl(port), testFolder);

    assertThatCode(
            () -> ReflectionTestUtils.invokeMethod(manager, "checkPortAvailable", "acceptString"))
        .doesNotThrowAnyException();
  }

  @Test
  void checkPortAvailable_WhenPipe_ShouldDoNothing(final @TempDir File testFolder) {

    final LocalOfficeProcessManager manager = newManager(new OfficeUrl("jodconverter"), testFolder);

    assertThatCode(
            () -> ReflectionTestUtils.invokeMethod(manager, "checkPortAvailable", "acceptString"))
        .doesNotThrowAnyException();
  }

  private static LocalOfficeProcessManager newManager(final OfficeUrl url, final File folder) {
    return new LocalOfficeProcessManager(
        url,
        folder,
        folder,
        LocalOfficeUtils.findBestProcessManager(),
        new ArrayList<>(),
        null,
        DEFAULT_PROCESS_TIMEOUT,
        DEFAULT_PROCESS_RETRY_INTERVAL,
        DEFAULT_AFTER_START_PROCESS_DELAY,
        DEFAULT_EXISTING_PROCESS_ACTION,
        DEFAULT_START_FAIL_FAST,
        DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
        TestOfficeConnection.prepareTest(url));
  }

  @Test
  void prepareProcessBuilder_WithOfficeExecutable_ShouldStartTheExecutable(
      final @TempDir File testFolder) {

    final OfficeUrl url = new OfficeUrl(9999);
    final File launcher = new File(testFolder, "libreoffice");
    final LocalOfficeProcessManager manager =
        new LocalOfficeProcessManager(
            url,
            null,
            launcher,
            testFolder,
            LocalOfficeUtils.findBestProcessManager(),
            new ArrayList<>(),
            null,
            DEFAULT_PROCESS_TIMEOUT,
            DEFAULT_PROCESS_RETRY_INTERVAL,
            DEFAULT_AFTER_START_PROCESS_DELAY,
            DEFAULT_EXISTING_PROCESS_ACTION,
            DEFAULT_START_FAIL_FAST,
            DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
            TestOfficeConnection.prepareTest(url));
    ReflectionTestUtils.setField(
        manager, "descriptor", ReflectionTestUtils.invokeMethod(manager, "detectOfficeDescriptor"));

    final ProcessBuilder processBuilder =
        ReflectionTestUtils.invokeMethod(manager, "prepareProcessBuilder", "acceptString");

    assertThat(processBuilder).isNotNull();
    assertThat(processBuilder.command())
        .startsWith(launcher.getAbsolutePath(), "--accept=acceptString");
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
