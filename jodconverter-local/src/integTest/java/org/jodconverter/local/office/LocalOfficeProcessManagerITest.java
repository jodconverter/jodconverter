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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.local.office.LocalOfficeManager.*;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.test.util.TestUtil;
import org.jodconverter.local.process.IntegTestProcessManager;

/** Contains tests for the {@link LocalOfficeProcessManager} class. */
class LocalOfficeProcessManagerITest {

  private static final OfficeUrl CONNECT_URL = new OfficeUrl(2002);
  private static final long START_INITIAL_WAIT = 2_000L; // 2 Seconds.
  private static final long START_WAIT_TIMEOUT = 15_000L; // 30 Seconds.
  private static final long STOP_INITIAL_WAIT = 2_000L; // 2 Seconds.
  private static final long STOP_WAIT_TIMEOUT = 15_000L; // 30 Seconds.

  @Nested
  class Start {

    @Test
    void whenProcessAlreadyExistsAnExistingProcessActionIsKill_ShouldKillExistingProcess()
        throws Exception {

      // Starts an office process
      final LocalOfficeProcessManager existingManager = startOfficeProcess();

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
              ExistingProcessAction.KILL,
              true,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              DEFAULT_DISABLE_OPENGL,
              new OfficeConnection(CONNECT_URL));
      try {
        manager.start();
        assertStartedAndConnected(manager);

      } finally {

        manager.stop();
        assertStoppedAndDisconnected(manager);
        assertStoppedAndDisconnected(existingManager);
      }
    }

    @Test
    void whenProcessAlreadyExistsAndExistingProcessActionIsFail_ShouldThrowOfficeException()
        throws Exception {

      // Starts an office process
      final LocalOfficeProcessManager existingManager = startOfficeProcess();

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
              10L,
              ExistingProcessAction.FAIL,
              true,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              DEFAULT_DISABLE_OPENGL,
              new OfficeConnection(CONNECT_URL));
      try {

        // Find a way to assert that an exception is thrown (check the log).
        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(manager::start)
            .withMessageMatching("A process with --accept.*is already running.*");

      } finally {

        assertStoppedAndDisconnected(manager);

        existingManager.stop();
        assertStoppedAndDisconnected(existingManager);
      }
    }

    @Test
    void whenProcessAlreadyExistsAndExistingProcessActionIsConnect_ShouldConnectToExistingProcess()
        throws Exception {

      // Starts an office process
      final LocalOfficeProcessManager existingManager = startOfficeProcess();

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
              ExistingProcessAction.CONNECT,
              true,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              DEFAULT_DISABLE_OPENGL,
              new OfficeConnection(CONNECT_URL));
      try {

        manager.start();
        assertStartedAndConnected(manager);

      } finally {

        manager.stop();
        assertStoppedAndDisconnected(manager);
        assertStoppedAndDisconnected(existingManager);
      }
    }

    @Test
    void
        whenProcessAlreadyExistsAndExistingProcessActionIsConnectOrKill_ShouldConnectToExistingProcess()
            throws Exception {

      // Starts an office process
      final LocalOfficeProcessManager existingManager = startOfficeProcess();

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
              ExistingProcessAction.CONNECT_OR_KILL,
              true,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              DEFAULT_DISABLE_OPENGL,
              new OfficeConnection(CONNECT_URL));
      try {
        manager.start();
        assertStartedAndConnected(manager);

      } finally {

        manager.stop();
        assertStoppedAndDisconnected(manager);
        assertStoppedAndDisconnected(existingManager);
      }
    }

    @Test
    void
        whenProcessAlreadyExistsAndExistingProcessActionIsConnectOrKillAndCannotConnect_ShouldKillExistingProcess()
            throws Exception {

      // Starts an office process
      final LocalOfficeProcessManager existingManager = startOfficeProcess();

      final AtomicReference<LocalOfficeProcessManager> managerRef = new AtomicReference<>();
      final AtomicInteger connectAttempt = new AtomicInteger();
      final OfficeConnection connection =
          new OfficeConnection(CONNECT_URL) {
            @Override
            public void connect() throws OfficeConnectionException {
              // Ensure we throw an exception for the connectToExistingProcess attempt.
              // According to our setting, the number of tries would be 2
              // (counting the initial try).
              final int attempt = connectAttempt.getAndIncrement();
              if (attempt < 2) {
                throw new OfficeConnectionException("Test", "Test");
              } else if (attempt == 2) {
                // Also change the setting for connection after the kill.
                Whitebox.setInternalState(
                    managerRef.get(), "processTimeout", DEFAULT_PROCESS_TIMEOUT);
                Whitebox.setInternalState(
                    managerRef.get(), "processRetryInterval", DEFAULT_PROCESS_RETRY_INTERVAL);
                throw new OfficeConnectionException("Test", "Test");
              }

              super.connect();
            }
          };
      final LocalOfficeProcessManager manager =
          new LocalOfficeProcessManager(
              CONNECT_URL,
              LocalOfficeUtils.getDefaultOfficeHome(),
              OfficeUtils.getDefaultWorkingDir(),
              new IntegTestProcessManager(),
              new ArrayList<>(),
              null,
              2000L,
              1000L,
              DEFAULT_AFTER_START_PROCESS_DELAY,
              ExistingProcessAction.CONNECT_OR_KILL,
              true,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              DEFAULT_DISABLE_OPENGL,
              connection);
      managerRef.set(manager);
      try {
        manager.start();
        assertStartedAndConnected(manager);

        assertThat(manager)
            .extracting("processManager.killCount")
            .asInstanceOf(InstanceOfAssertFactories.ATOMIC_INTEGER)
            .hasValue(1);

      } finally {

        manager.stop();
        assertStoppedAndDisconnected(manager);
        assertStoppedAndDisconnected(existingManager);
      }
    }

    @Test
    void withCustomProfileDir_ShouldCopyProfileDirToWorkingDir() throws OfficeException {

      final OfficeConnection connection = new OfficeConnection(CONNECT_URL);
      final LocalOfficeProcessManager manager =
          new LocalOfficeProcessManager(
              CONNECT_URL,
              LocalOfficeUtils.getDefaultOfficeHome(),
              OfficeUtils.getDefaultWorkingDir(),
              LocalOfficeUtils.findBestProcessManager(),
              new ArrayList<>(),
              new File("src/integTest/resources/templateProfileDir"),
              DEFAULT_PROCESS_TIMEOUT,
              DEFAULT_PROCESS_RETRY_INTERVAL,
              DEFAULT_AFTER_START_PROCESS_DELAY,
              DEFAULT_EXISTING_PROCESS_ACTION,
              true,
              DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
              DEFAULT_DISABLE_OPENGL,
              connection);
      try {
        manager.start();
        assertStartedAndConnected(manager);

        // Check the profile dir existence
        final File instanceProfileDir = Whitebox.getInternalState(manager, "instanceProfileDir");
        assertThat(new File(instanceProfileDir, "user/customFile")).isFile();

      } finally {

        manager.stop();
        assertStoppedAndDisconnected(manager);
      }
    }
  }

  @Nested
  class Stop {

    @Test
    void whenKeepAliveOnShutdown_ShouldKeepProcessAlive() throws Exception {

      // Starts an office process
      final OfficeConnection connection = new OfficeConnection(CONNECT_URL);
      LocalOfficeProcessManager manager =
          new LocalOfficeProcessManager(
              CONNECT_URL,
              LocalOfficeUtils.getDefaultOfficeHome(),
              OfficeUtils.getDefaultWorkingDir(),
              new IntegTestProcessManager(),
              new ArrayList<>(),
              null,
              DEFAULT_PROCESS_TIMEOUT,
              DEFAULT_PROCESS_RETRY_INTERVAL,
              DEFAULT_AFTER_START_PROCESS_DELAY,
              ExistingProcessAction.KILL,
              true,
              true,
              DEFAULT_DISABLE_OPENGL,
              connection);
      try {
        manager.start();
        assertStartedAndConnected(manager);

        manager.stop();

        // Create another manager that would fail if a process already exists
        manager =
            new LocalOfficeProcessManager(
                CONNECT_URL,
                LocalOfficeUtils.getDefaultOfficeHome(),
                OfficeUtils.getDefaultWorkingDir(),
                new IntegTestProcessManager(),
                new ArrayList<>(),
                null,
                DEFAULT_PROCESS_TIMEOUT,
                DEFAULT_PROCESS_RETRY_INTERVAL,
                DEFAULT_AFTER_START_PROCESS_DELAY,
                ExistingProcessAction.FAIL,
                true,
                DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
                DEFAULT_DISABLE_OPENGL,
                connection);

        // Find a way to assert that an exception is thrown (check the log).
        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(manager::start)
            .withMessageMatching("A process with --accept.*is already running.*");

        // Then create again another manager, just to kill the existing process.
        manager =
            new LocalOfficeProcessManager(
                CONNECT_URL,
                LocalOfficeUtils.getDefaultOfficeHome(),
                OfficeUtils.getDefaultWorkingDir(),
                new IntegTestProcessManager(),
                new ArrayList<>(),
                null,
                DEFAULT_PROCESS_TIMEOUT,
                DEFAULT_PROCESS_RETRY_INTERVAL,
                DEFAULT_AFTER_START_PROCESS_DELAY,
                ExistingProcessAction.CONNECT_OR_KILL,
                true,
                DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
                DEFAULT_DISABLE_OPENGL,
                connection);

      } finally {

        manager.stop();
        assertStoppedAndDisconnected(manager);
      }
    }
  }

  private static LocalOfficeProcessManager startOfficeProcess() throws OfficeException {

    final long start = System.currentTimeMillis();

    // Starts an office process
    final OfficeConnection connection = new OfficeConnection(CONNECT_URL);
    final LocalOfficeProcessManager processManager =
        new LocalOfficeProcessManager(
            CONNECT_URL,
            LocalOfficeUtils.getDefaultOfficeHome(),
            OfficeUtils.getDefaultWorkingDir(),
            new IntegTestProcessManager(),
            new ArrayList<>(),
            null,
            DEFAULT_PROCESS_TIMEOUT,
            DEFAULT_PROCESS_RETRY_INTERVAL,
            DEFAULT_AFTER_START_PROCESS_DELAY,
            DEFAULT_EXISTING_PROCESS_ACTION,
            DEFAULT_START_FAIL_FAST,
            DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
            DEFAULT_DISABLE_OPENGL,
            connection);
    processManager.start();
    final long limit = start + START_WAIT_TIMEOUT;
    while (System.currentTimeMillis() < limit) {
      if (connection.isConnected()) {
        break;
      }

      // Wait a sec
      TestUtil.sleepQuietly(1_000L);
    }
    return processManager;
  }

  private static void assertStartedAndConnected(final LocalOfficeProcessManager manager) {

    final long start = System.currentTimeMillis();

    TestUtil.sleepQuietly(STOP_INITIAL_WAIT);

    final long limit = start + STOP_WAIT_TIMEOUT;
    while (System.currentTimeMillis() < limit) {
      if (manager.getConnection().isConnected()) {
        return;
      }

      // Wait a sec
      TestUtil.sleepQuietly(1_000L);
    }

    // Times out or connected...
    assertThat(manager.getConnection().isConnected()).isTrue();
    assertThat(manager).extracting("process.exitCode").isNull();
  }

  private static void assertStoppedAndDisconnected(final LocalOfficeProcessManager manager) {

    final long start = System.currentTimeMillis();

    TestUtil.sleepQuietly(START_INITIAL_WAIT);

    final long limit = start + START_WAIT_TIMEOUT;
    while (System.currentTimeMillis() < limit) {
      if (!manager.getConnection().isConnected()) {
        return;
      }

      // Wait a sec
      TestUtil.sleepQuietly(1_000L);
    }

    // Times out or disconnected...
    assertThat(manager.getConnection().isConnected()).isFalse();
    assertThat(manager).extracting("process.exitCode").isEqualTo(0);
  }
}
