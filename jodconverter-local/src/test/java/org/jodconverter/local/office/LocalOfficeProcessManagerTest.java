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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.DisposedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.process.ProcessManager;
import org.jodconverter.local.process.ProcessQuery;

/** Contains tests for the {@link LocalOfficeProcessManager} class. */
class LocalOfficeProcessManagerTest {

  private static final String START_ERROR = "An error prevents us to start a process with --accept";

  /* default */
  @TempDir File workingDir;

  @BeforeEach
  void setUpOfficeHome() {
    System.setProperty("office.home", new File("src/test/resources/oohome").getPath());
  }

  @AfterEach
  void tearDown() {
    System.setProperty("office.home", "");
  }

  /** A process manager that finds no process, and records the processes it is asked to kill. */
  private static final class RecordingProcessManager implements ProcessManager {

    /* default */ final List<Long> killedPids = new CopyOnWriteArrayList<>();

    @Override
    public Optional<ProcessHandle> find(final ProcessQuery query) {
      return Optional.empty();
    }

    @Override
    public void kill(final ProcessHandle process) {
      killedPids.add(process.pid());
    }
  }

  // The office home of the tests is not a real office: no process can be started.
  private LocalOfficeProcessManager newManager(
      final OfficeUrl url,
      final OfficeConnection connection,
      final ProcessManager processManager,
      final boolean keepAliveOnShutdown) {

    return new LocalOfficeProcessManager(
        url,
        new File("src/test/resources/oohome"),
        workingDir,
        processManager,
        new ArrayList<>(),
        null,
        0L,
        0L,
        DEFAULT_AFTER_START_PROCESS_DELAY,
        DEFAULT_EXISTING_PROCESS_ACTION,
        keepAliveOnShutdown,
        connection);
  }

  private LocalOfficeProcessManager newManager(final ProcessManager processManager) {
    return newManager(new OfficeUrl(9999), processManager);
  }

  private LocalOfficeProcessManager newManager(
      final OfficeUrl url, final ProcessManager processManager) {
    return newManager(url, TestOfficeConnection.prepareTest(url), processManager, false);
  }

  // A handle standing for an office process, which only has to report its pid.
  private static ProcessHandle handleWithPid(final long pid) {
    final var handle = mock(ProcessHandle.class);
    given(handle.pid()).willReturn(pid);
    return handle;
  }

  private static File instanceProfileDirOf(final LocalOfficeProcessManager manager) {
    final var dir = (File) ReflectionTestUtils.getField(manager, "instanceProfileDir");
    assertThat(dir).isNotNull();
    return dir;
  }

  @Nested
  class GetConnection {

    @Test
    void shouldReturnExpectedConnection() {

      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url);

      final var manager = newManager(url, connection, new RecordingProcessManager(), false);

      assertThat(manager.getConnection()).isEqualTo(connection);
    }
  }

  @Nested
  class Start {

    @Test
    void whenCouldNotStart_ShouldThrowOfficeException() {

      final var manager = newManager(new RecordingProcessManager());

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(manager::start)
          .withMessageStartingWith(START_ERROR);
    }

    @Test
    void whenPortUsedByAnotherProgram_ShouldThrowOfficeException() throws IOException {

      // Another program listening on the port (Tomcat for example)
      try (ServerSocket otherProgram = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
        final var manager =
            newManager(new OfficeUrl(otherProgram.getLocalPort()), new RecordingProcessManager());

        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(manager::start)
            .withMessageContaining("is already used by another program");
      }
    }
  }

  @Nested
  class Restart {

    @Test
    void whenCouldNotRestart_ShouldThrowOfficeException() {

      final var manager = newManager(new RecordingProcessManager());

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(manager::restart)
          .withMessageStartingWith(START_ERROR);
    }

    @Test
    void shouldKeepTheInstanceProfileDir() {

      final var manager = newManager(new RecordingProcessManager());
      final var userDir = new File(instanceProfileDirOf(manager), "user");
      assertThat(userDir.mkdirs()).isTrue();

      assertThatExceptionOfType(OfficeException.class).isThrownBy(manager::restart);

      // The office process restarts faster with the profile it already has.
      assertThat(userDir).isDirectory();
    }
  }

  @Nested
  class RestartDueToLostConnection {

    @Test
    void whenCouldNotRestart_ShouldThrowOfficeException() {

      final var manager = newManager(new RecordingProcessManager());

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(manager::restartDueToLostConnection)
          .withMessageStartingWith(START_ERROR);
    }

    @Test
    void shouldDeleteTheInstanceProfileDir() {

      final var manager = newManager(new RecordingProcessManager());
      final var userDir = new File(instanceProfileDirOf(manager), "user");
      assertThat(userDir.mkdirs()).isTrue();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(manager::restartDueToLostConnection);

      // The office process may have crashed: it restarts with a clean profile.
      assertThat(userDir).doesNotExist();
    }

    @Test
    void whenNoProcessWasStartedAndPortUsedByAnotherProgram_ShouldThrowOfficeException()
        throws IOException {

      // A first start that failed because of the port must fail the same way when retried.
      try (ServerSocket otherProgram = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
        final var manager =
            newManager(new OfficeUrl(otherProgram.getLocalPort()), new RecordingProcessManager());

        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(manager::restartDueToLostConnection)
            .withMessageContaining("is already used by another program");
      }
    }

    @Test
    void whenAProcessWasStartedAndPortStillUsed_ShouldNotCheckThePort() throws IOException {

      // The office process that was just killed may still be releasing its port.
      try (ServerSocket otherProgram = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
        final var manager =
            newManager(new OfficeUrl(otherProgram.getLocalPort()), new RecordingProcessManager());
        ReflectionTestUtils.setField(manager, "processHandle", handleWithPid(1234L));

        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(manager::restartDueToLostConnection)
            .withMessageStartingWith(START_ERROR);
      }
    }
  }

  @Nested
  class Kill {

    @Test
    void whenNotStarted_ShouldDoNothing() {

      final var processManager = new RecordingProcessManager();
      final var manager = newManager(processManager);

      assertThatCode(manager::kill).doesNotThrowAnyException();

      assertThat(processManager.killedPids).isEmpty();
    }

    @Test
    void whenStarted_ShouldKillTheProcess() {

      final var processManager = new RecordingProcessManager();
      final var manager = newManager(processManager);
      ReflectionTestUtils.setField(manager, "processHandle", handleWithPid(1234L));

      manager.kill();

      assertThat(processManager.killedPids).containsExactly(1234L);
    }
  }

  @Nested
  class Stop {

    @Test
    void whenNotStarted_ShouldNotThrowAnyException() {

      final var processManager = new RecordingProcessManager();
      final var manager = newManager(processManager);

      assertThatCode(manager::stop).doesNotThrowAnyException();

      assertThat(processManager.killedPids).isEmpty();
    }

    @Test
    void whenNotConnected_ShouldKillTheProcessAndDeleteTheInstanceProfileDir() {

      final var processManager = new RecordingProcessManager();
      final var manager = newManager(processManager);
      ReflectionTestUtils.setField(manager, "processHandle", handleWithPid(1234L));
      final var instanceProfileDir = instanceProfileDirOf(manager);
      assertThat(instanceProfileDir.mkdirs()).isTrue();

      manager.stop();

      // Without a connection, the office process cannot be asked to terminate.
      assertThat(processManager.killedPids).containsExactly(1234L);
      assertThat(instanceProfileDir).doesNotExist();
    }

    @Test
    void whenConnected_ShouldAskTheProcessToTerminateWithoutKillingIt() {

      final var processManager = new RecordingProcessManager();
      final XDesktop desktop = mock(XDesktop.class);
      given(desktop.terminate()).willReturn(true);
      final var url = new OfficeUrl(9999);
      final OfficeConnection connection = mock(OfficeConnection.class);
      given(connection.getDesktop()).willReturn(desktop);
      final var manager = newManager(url, connection, processManager, false);

      manager.stop();

      verify(desktop).terminate();
      assertThat(processManager.killedPids).isEmpty();
    }

    @Test
    void whenSomethingPreventsTermination_ShouldNotThrowAnyException() {

      // The quickstarter for example.
      final XDesktop desktop = mock(XDesktop.class);
      given(desktop.terminate()).willReturn(false);
      final var url = new OfficeUrl(9999);
      final OfficeConnection connection = mock(OfficeConnection.class);
      given(connection.getDesktop()).willReturn(desktop);
      final var manager = newManager(url, connection, new RecordingProcessManager(), false);

      assertThatCode(manager::stop).doesNotThrowAnyException();

      verify(desktop).terminate();
    }

    @Test
    void whenTheConnectionIsDisposedWhileTerminating_ShouldNotThrowAnyException() {

      // The office process may close the connection before it answers.
      final XDesktop desktop = mock(XDesktop.class);
      given(desktop.terminate()).willThrow(new DisposedException("Disposed"));
      final var url = new OfficeUrl(9999);
      final OfficeConnection connection = mock(OfficeConnection.class);
      given(connection.getDesktop()).willReturn(desktop);
      final var manager = newManager(url, connection, new RecordingProcessManager(), false);

      assertThatCode(manager::stop).doesNotThrowAnyException();

      verify(desktop).terminate();
    }

    @Test
    void whenKeepAliveOnShutdown_ShouldOnlyDisconnect() {

      final var processManager = new RecordingProcessManager();
      final var url = new OfficeUrl(9999);
      final var connection = TestOfficeConnection.prepareTest(url);
      final var manager = newManager(url, connection, processManager, true);
      ReflectionTestUtils.setField(manager, "processHandle", handleWithPid(1234L));
      connection.connect();

      manager.stop();

      assertThat(connection.isConnected()).isFalse();
      assertThat(processManager.killedPids).isEmpty();
    }
  }
}
