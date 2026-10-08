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
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.util.OSUtils;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;

/** Contains tests for the {@link ProcessManager} implementations. */
class ProcessManagerTest {

  private static final int FIRST_ATTEMPT = 1;

  // A process that lives a few seconds and starts a child process: a shell running a sleep on
  // Unix (two commands, so that the shell does not exec the sleep in its place), cmd running a
  // ping on Windows.
  private static Process startProcessTree() throws IOException {
    return OSUtils.IS_OS_WINDOWS
        ? new ProcessBuilder("cmd", "/c", "ping 127.0.0.1 -n 30").start()
        : new ProcessBuilder("/bin/sh", "-c", "sleep 30; exit 0").start();
  }

  private static ProcessQuery queryOfProcessTree() {
    return OSUtils.IS_OS_WINDOWS
        ? new ProcessQuery("cmd", "ping 127.0.0.1 -n 30")
        : new ProcessQuery("sh", "sleep 30; exit 0");
  }

  private static void assertFindsAndKills(final ProcessManager manager) throws Exception {

    final var process = startProcessTree();
    try {
      final var query = queryOfProcessTree();
      assertThat(manager.find(query)).map(ProcessHandle::pid).hasValue(process.pid());

      // The child gets a moment to start, so the kill has a tree to deal with.
      TimeUnit.MILLISECONDS.sleep(500L);
      final var children = process.children().toList();
      assertThat(children).isNotEmpty();

      manager.kill(process.toHandle());
      assertThat(process.waitFor(10L, TimeUnit.SECONDS)).isTrue();
      for (final var child : children) {
        child.onExit().get(10L, TimeUnit.SECONDS);
        assertThat(child.isAlive()).isFalse();
      }
      assertThat(manager.find(query)).isEmpty();
    } finally {
      process.descendants().forEach(ProcessHandle::destroyForcibly);
      process.destroyForcibly();
    }
  }

  @Nested
  class Unix {

    @Test
    void find_ShouldFindTheProcessAndKillShouldKillItsTree() throws Exception {
      assumeTrue(OSUtils.IS_OS_UNIX);

      assertFindsAndKills(UnixProcessManager.getDefault());
    }

    @Test
    void findBest_ShouldReturnUnixProcessManager() {
      assumeTrue(OSUtils.IS_OS_UNIX);

      assertThat(LocalOfficeUtils.findBestProcessManager())
          .isSameAs(UnixProcessManager.getDefault());
    }
  }

  @Nested
  class Execute {

    // A manager that only runs commands.
    private final AbstractProcessManager manager =
        new AbstractProcessManager() {
          @Override
          protected String[] getRunningProcessesCommand(final String process) {
            return new String[0];
          }

          @Override
          protected Pattern getRunningProcessLinePattern() {
            return Pattern.compile(".*");
          }
        };

    private String[] shell(final String script) {
      return OSUtils.IS_OS_WINDOWS
          ? new String[] {"cmd", "/c", script}
          : new String[] {"/bin/sh", "-c", script};
    }

    @Test
    void whenTheCommandSucceeds_ShouldReturnItsOutput() throws IOException {

      assertThat(manager.execute(shell("echo hello"))).containsExactly("hello");
    }

    @Test
    void whenTheCommandFails_ShouldThrowIOExceptionWithItsErrorOutput() {

      assertThatIOException()
          .isThrownBy(() -> manager.execute(shell("echo oops 1>&2 & exit 3")))
          .withMessageContaining("exited with the status 3")
          .withMessageContaining("oops");
    }

    @Test
    void withRetries_WhenTheCommandFailsOnce_ShouldReturnTheOutputOfTheNextAttempt()
        throws IOException {

      final var attempts = new AtomicInteger();
      final var manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... command) throws IOException {
              if (attempts.incrementAndGet() == FIRST_ATTEMPT) {
                throw new IOException("Call cancelled");
              }
              return List.of("some process 42");
            }
          };

      assertThat(manager.executeWithRetries("whatever")).containsExactly("some process 42");
      assertThat(attempts).hasValue(2);
    }

    @Test
    void getRetryDelay_ShouldDoubleAtEachFailure() {

      assertThat(AbstractProcessManager.ATTEMPTS).isEqualTo(5);
      assertThat(
              IntStream.range(1, AbstractProcessManager.ATTEMPTS)
                  .mapToLong(manager::getRetryDelay)
                  .toArray())
          .containsExactly(250L, 500L, 1_000L, 2_000L);
      assertThat(manager.getRetryDelay(0)).isEqualTo(250L);
    }

    @Test
    void withRetries_WhenTheCommandKeepsFailing_ShouldThrowTheLastIOException() {

      final var attempts = new AtomicInteger();
      final var manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... command) throws IOException {
              throw new IOException("Call cancelled #" + attempts.incrementAndGet());
            }

            @Override
            protected long getRetryDelay(final int failures) {
              return 0L;
            }
          };

      assertThatIOException()
          .isThrownBy(() -> manager.find(new ProcessQuery("soffice", "port=2002")))
          .withMessage("Call cancelled #" + AbstractProcessManager.ATTEMPTS);
      assertThat(attempts).hasValue(AbstractProcessManager.ATTEMPTS);
    }
  }

  @Nested
  class Windows {

    @Test
    void find_ShouldFindTheProcessAndKillShouldKillItsTree() throws Exception {
      assumeTrue(OSUtils.IS_OS_WINDOWS);

      assertFindsAndKills(WindowsProcessManager.getDefault());
    }

    @Test
    void findBest_ShouldReturnWindowsProcessManager() {
      assumeTrue(OSUtils.IS_OS_WINDOWS);

      assertThat(LocalOfficeUtils.findBestProcessManager())
          .isSameAs(WindowsProcessManager.getDefault());
    }

    @Test
    void isUsable_WhenIOExceptionCatched_ShouldReturnFalse() {

      final var manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... command) throws IOException {
              throw new IOException();
            }

            @Override
            protected long getRetryDelay(final int failures) {
              return 0L;
            }
          };
      assertThat(manager.isUsable()).isFalse();
    }

    @Test
    void isUsable_WhenThePowershellQueryFailsOnce_ShouldReturnTrue() {

      final var attempts = new AtomicInteger();
      final var manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... command) throws IOException {
              if (attempts.incrementAndGet() == FIRST_ATTEMPT) {
                throw new IOException("Call cancelled");
              }
              return List.of("powershell -NoProfile -NonInteractive 1234");
            }
          };
      assertThat(manager.isUsable()).isTrue();
      assertThat(attempts).hasValue(2);
    }

    @Test
    void isUsable_WhenThePowershellQueryWorks_ShouldReturnTrue() {

      final var manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... command) {
              return List.of("powershell -NoProfile -NonInteractive 1234");
            }
          };
      assertThat(manager.isUsable()).isTrue();
    }

    @Test
    void isUsable_WhenThePowershellQueryReturnsNothing_ShouldReturnFalse() {

      final var manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... command) {
              return new ArrayList<>();
            }
          };
      assertThat(manager.isUsable()).isFalse();
    }

    @Test
    void getRunningProcessesCommand_ShouldUseAnEncodedPowershellQuery() {

      final var command = WindowsProcessManager.getDefault().getRunningProcessesCommand("soffice");
      assertThat(command)
          .startsWith("powershell", "-NoProfile", "-NonInteractive", "-EncodedCommand");
      assertThat(command).hasSize(5);
    }

    @Test
    void find_WhenTheOutputNamesAProcessThatIsGone_ShouldReturnEmpty() throws IOException {

      // A pid that no process has: the largest pid is far below.
      final var manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... command) {
              return List.of("soffice.bin --accept=socket,host=127.0.0.1,port=2002 2147483646");
            }
          };
      assertThat(manager.find(new ProcessQuery("soffice", "port=2002"))).isEmpty();
    }

    @Test
    void find_WhenTheOutputMatches_ShouldReturnTheProcess() throws IOException {

      final var manager =
          new WindowsProcessManager() {
            @Override
            protected List<String> execute(final String... command) {
              return List.of(
                  "some other process 42",
                  "soffice.bin --accept=socket,host=127.0.0.1,port=2002 "
                      + ProcessHandle.current().pid());
            }
          };
      assertThat(manager.find(new ProcessQuery("soffice", "port=2002")))
          .map(ProcessHandle::pid)
          .hasValue(ProcessHandle.current().pid());
      assertThat(manager.find(new ProcessQuery("soffice", "port=2003"))).isEmpty();
    }
  }

  @Nested
  class PureJava {

    @Test
    void find_ShouldReturnEmpty() {

      assertThat(PureJavaProcessManager.getDefault().find(new ProcessQuery("sh", "sleep")))
          .isEmpty();
    }

    @Test
    void kill_ShouldKillTheProcessTree() throws Exception {

      final var process = startProcessTree();
      try {
        TimeUnit.MILLISECONDS.sleep(500L);
        final var children = process.children().toList();
        PureJavaProcessManager.getDefault().kill(process.toHandle());
        assertThat(process.waitFor(10L, TimeUnit.SECONDS)).isTrue();
        for (final var child : children) {
          child.onExit().get(10L, TimeUnit.SECONDS);
        }
      } finally {
        process.descendants().forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
      }
    }
  }

  @Nested
  class Custom {

    @Test
    void customProcessManagerNotFound_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  LocalOfficeManager.builder()
                      .processManager("org.foo.fallback.ProcessManager")
                      .build());
    }

    @Test
    void customProcessManager_FindReturnsEmptyByContract() throws IOException {

      final ProcessManager manager = query -> Optional.empty();
      assertThat(manager.find(new ProcessQuery("a", "b"))).isEmpty();
    }
  }
}
