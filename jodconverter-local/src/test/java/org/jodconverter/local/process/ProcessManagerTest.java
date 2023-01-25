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

package org.jodconverter.local.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.jodconverter.local.process.ProcessManager.PID_UNKNOWN;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.test.util.TestUtil;
import org.jodconverter.core.util.OSUtils;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;

/** Contains tests for the {@link ProcessManager} classes */
class ProcessManagerTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessManagerTest.class);

  private static long waitForPidNotFound(
      final ProcessManager processManager, final ProcessQuery query) throws IOException {

    int tryCount = 0;
    long pid;
    do {
      tryCount++;
      pid = processManager.findPid(query);
      if (pid != ProcessManager.PID_NOT_FOUND) {
        TestUtil.sleepQuietly(250L);
      }
    } while (pid != ProcessManager.PID_NOT_FOUND && tryCount != 10);
    return pid;
  }

  @Nested
  class FreeBSD {

    @Test
    void canFindPid_ShouldReturnTrue() {
      assertThat(FreeBSDProcessManager.getDefault().canFindPid()).isTrue();
    }

    @Test
    void shouldFindPidAndBeAbleToKillProcess() throws IOException {
      assumeTrue(OSUtils.IS_OS_FREE_BSD);

      final ProcessManager processManager = FreeBSDProcessManager.getDefault();
      final Process process = Runtime.getRuntime().exec("ping -c 5 127.0.0.1");
      final ProcessQuery query = new ProcessQuery("ping", "-c 5 127.0.0.1");

      final long pid = processManager.findPid(query);
      assertThat(pid).isNotEqualTo(ProcessManager.PID_NOT_FOUND);
      assertThat(process)
          .extracting("pid")
          .isInstanceOfSatisfying(
              Number.class, number -> assertThat(number.longValue()).isEqualTo(pid));

      processManager.kill(process, pid);
      assertThat(waitForPidNotFound(processManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
    }

    @Test
    void pureJavaShouldReturnPidUnknown() throws IOException {
      assumeTrue(OSUtils.IS_OS_FREE_BSD);

      final ProcessManager defaultManager = LocalOfficeUtils.findBestProcessManager();
      final ProcessManager processManager = PureJavaProcessManager.getDefault();
      final Process process = Runtime.getRuntime().exec("ping -c 5 127.0.0.1");
      final ProcessQuery query = new ProcessQuery("ping", "-c 5 127.0.0.1");

      assertThat(processManager.canFindPid()).isEqualTo(false);

      final long pid = processManager.findPid(query);
      assertThat(pid).isEqualTo(PID_UNKNOWN);

      processManager.kill(process, pid);
      assertThat(waitForPidNotFound(defaultManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
    }
  }

  @Nested
  class Unix {

    @Test
    void canFindPid_ShouldReturnTrue() {
      assertThat(UnixProcessManager.getDefault().canFindPid()).isTrue();
    }

    @Test
    void shouldFindPidAndBeAbleToKillProcess() throws IOException {
      assumeTrue(OSUtils.IS_OS_UNIX && !OSUtils.IS_OS_MAC && !OSUtils.IS_OS_FREE_BSD);

      final ProcessManager processManager = UnixProcessManager.getDefault();
      final Process process = Runtime.getRuntime().exec("sleep 5s");
      final ProcessQuery query = new ProcessQuery("sleep", "5s");

      final long pid = processManager.findPid(query);
      assertThat(pid).isNotEqualTo(ProcessManager.PID_NOT_FOUND);
      assertThat(process)
          .extracting("pid")
          .isInstanceOfSatisfying(
              Number.class, number -> assertThat(number.longValue()).isEqualTo(pid));

      processManager.kill(process, pid);
      assertThat(waitForPidNotFound(processManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
    }

    @Test
    void pureJavaShouldReturnPidUnknown() throws IOException {
      assumeTrue(OSUtils.IS_OS_UNIX && !OSUtils.IS_OS_MAC && !OSUtils.IS_OS_FREE_BSD);

      final ProcessManager defaultManager = LocalOfficeUtils.findBestProcessManager();
      final ProcessManager processManager = PureJavaProcessManager.getDefault();
      final Process process = Runtime.getRuntime().exec("sleep 5s");
      final ProcessQuery query = new ProcessQuery("sleep", "5s");

      assertThat(processManager.canFindPid()).isEqualTo(false);

      final long pid = processManager.findPid(query);
      assertThat(pid).isEqualTo(PID_UNKNOWN);

      processManager.kill(process, pid);
      assertThat(waitForPidNotFound(defaultManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
    }

    @Test
    void kill_withKnownPid_ShouldCallExecute() throws IOException {

      final AtomicBoolean executed = new AtomicBoolean();
      final UnixProcessManager manager =
          new UnixProcessManager() {
            @Override
            protected List<String> execute(final String... cmdarray) {
              executed.set(true);
              return new ArrayList<>();
            }
          };
      manager.kill(null, 1);
      assertThat(executed).isTrue();
    }

    @Test
    void kill_withUnknownPid_ShouldCallProcessDestroy() throws IOException {

      final Process process = mock(Process.class);
      final AtomicBoolean executed = new AtomicBoolean();
      final UnixProcessManager manager =
          new UnixProcessManager() {
            @Override
            protected List<String> execute(final String... cmdarray) {
              executed.set(true);
              return new ArrayList<>();
            }
          };
      manager.kill(process, PID_UNKNOWN);
      verify(process, times(1)).destroy();
    }
  }

  @Nested
  class Mac {

    @Test
    void canFindPid_ShouldReturnTrue() {
      assertThat(MacProcessManager.getDefault().canFindPid()).isTrue();
    }

    @Test
    void shouldFindPidAndBeAbleToKillProcess() throws IOException {
      assumeTrue(OSUtils.IS_OS_MAC);

      final ProcessManager processManager = MacProcessManager.getDefault();

      // final Process process = Runtime.getRuntime().exec("sleep 5s");
      final Process process = execute("sleep 5s");
      final ProcessQuery query = new ProcessQuery("sleep", "5s");

      final long pid = processManager.findPid(query);
      assertThat(pid).isNotEqualTo(ProcessManager.PID_NOT_FOUND);
      //      assertThat(process)
      //          .extracting("pid")
      //          .isInstanceOfSatisfying(
      //              Number.class, number -> assertThat(number.longValue()).isEqualTo(pid));

      processManager.kill(process, pid);
      assertThat(waitForPidNotFound(processManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
    }

    private String buildOutput(final List<String> lines) {
      Objects.requireNonNull(lines, "lines must not be null");

      // Ignore empty lines
      return lines.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("\n"));
    }

    protected Process execute(final String... cmdarray) throws IOException {

      final Process process = Runtime.getRuntime().exec(cmdarray);

      final LinesPumpStreamHandler streamsHandler =
          new LinesPumpStreamHandler(process.getInputStream(), process.getErrorStream());

      streamsHandler.start();
      try {
        process.waitFor();
        streamsHandler.stop();
      } catch (InterruptedException ex) {

        // Log the interruption
        LOGGER.warn(
            "The current thread was interrupted while waiting for command execution output.", ex);
        // Restore the interrupted status
        Thread.currentThread().interrupt();
      }

      final List<String> outLines = streamsHandler.getOutputPumper().getLines();

      if (LOGGER.isTraceEnabled()) {
        final String out = buildOutput(outLines);
        final String err = buildOutput(streamsHandler.getErrorPumper().getLines());

        if (!StringUtils.isBlank(out)) {
          LOGGER.trace("Command Output: {}", out);
        }

        if (!StringUtils.isBlank(err)) {
          LOGGER.trace("Command Error: {}", err);
        }
      }

      return process;
    }

    @Test
    void pureJavaShouldReturnPidUnknown() throws IOException {
      assumeTrue(OSUtils.IS_OS_MAC);

      final ProcessManager defaultManager = LocalOfficeUtils.findBestProcessManager();
      final ProcessManager processManager = PureJavaProcessManager.getDefault();
      final Process process = Runtime.getRuntime().exec("sleep 5s");
      final ProcessQuery query = new ProcessQuery("sleep", "5s");

      assertThat(processManager.canFindPid()).isEqualTo(false);

      final long pid = processManager.findPid(query);
      assertThat(pid).isEqualTo(PID_UNKNOWN);

      processManager.kill(process, pid);
      assertThat(waitForPidNotFound(defaultManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
    }
  }

  @Nested
  @SuppressWarnings("NullableProblems")
  class Windows {

    @Test
    void canFindPid_ShouldReturnTrue() {
      assertThat(WindowsProcessManager.getDefault().canFindPid()).isTrue();
    }

    @Test
    void shouldFindPidAndBeAbleToKillProcess() throws IOException {
      assumeTrue(OSUtils.IS_OS_WINDOWS);

      final ProcessManager processManager = WindowsProcessManager.getDefault();
      final Process process = Runtime.getRuntime().exec("ping 127.0.0.1 -n 5");
      final ProcessQuery query = new ProcessQuery("ping", "127.0.0.1 -n 5");

      final long pid = processManager.findPid(query);
      assertThat(pid).isNotEqualTo(ProcessManager.PID_NOT_FOUND);
      // Won't work on Windows, skip this assertion
      // assertThat(process).extracting("pid")
      //        .isInstanceOfSatisfying(
      //            Number.class, number -> assertThat(number.longValue()).isEqualTo(pid));

      processManager.kill(process, pid);
      assertThat(waitForPidNotFound(processManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
    }

    @Test
    void pureJavaShouldReturnPidUnknown() throws IOException {
      assumeTrue(OSUtils.IS_OS_WINDOWS);

      final ProcessManager defaultManager = LocalOfficeUtils.findBestProcessManager();
      final ProcessManager processManager = PureJavaProcessManager.getDefault();
      final Process process = Runtime.getRuntime().exec("ping 127.0.0.1 -n 5");
      final ProcessQuery query = new ProcessQuery("ping", "127.0.0.1 -n 5");

      assertThat(processManager.canFindPid()).isEqualTo(false);

      final long pid = processManager.findPid(query);
      assertThat(pid).isEqualTo(PID_UNKNOWN);

      processManager.kill(process, pid);
      assertThat(waitForPidNotFound(defaultManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
    }

    @Test
    void isUsable_WhenIOExceptionCatched_ShouldReturnFalse() {

      final WindowsProcessManager manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... cmdarray) throws IOException {
              throw new IOException();
            }
          };
      assertThat(manager.isUsable()).isFalse();
    }

    @Test
    void isUsable_WhenNoExceptionCatched_ShouldReturnTrue() {

      final WindowsProcessManager manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... cmdarray) {
              return new ArrayList<>();
            }
          };
      assertThat(manager.isUsable()).isTrue();
    }

    @Test
    void kill_withKnownPid_ShouldCallExecute() throws IOException {

      final AtomicBoolean executed = new AtomicBoolean();
      final WindowsProcessManager manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... cmdarray) {
              executed.set(true);
              return new ArrayList<>();
            }
          };
      manager.kill(null, 1);
      assertThat(executed).isTrue();
    }

    @Test
    void kill_withUnknownPid_ShouldCallProcessDestroy() throws IOException {

      final Process process = mock(Process.class);
      final AtomicBoolean executed = new AtomicBoolean();
      final WindowsProcessManager manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... cmdarray) {
              executed.set(true);
              return new ArrayList<>();
            }
          };
      manager.kill(process, PID_UNKNOWN);
      verify(process, times(1)).destroy();
    }
  }

  @Nested
  class Custom {

    /**
     * Tests that using a custom process manager that does not appear in the classpath will fail
     * with an IllegalArgumentException.
     */
    @Test
    void customProcessManagerNotFound_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  LocalOfficeManager.builder()
                      .processManager("org.foo.fallback.ProcessManager")
                      .build());
    }
  }
}
