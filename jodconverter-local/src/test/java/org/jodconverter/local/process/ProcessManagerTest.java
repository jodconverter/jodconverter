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

package org.jodconverter.local.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;

public class ProcessManagerTest {

  private static void sleep(final long millisec) {
    try {
      Thread.sleep(millisec);
    } catch (InterruptedException ignore) {
      // ignore
    }
  }

  private static long waitForPidNotFound(
      final ProcessManager processManager, final ProcessQuery query) throws IOException {

    int tryCount = 0;
    long pid;
    do {
      tryCount++;
      pid = processManager.findPid(query);
      if (pid != ProcessManager.PID_NOT_FOUND) {
        sleep(250L);
      }
    } while (pid != ProcessManager.PID_NOT_FOUND && tryCount != 10);
    return pid;
  }

  @Test
  public void freeBsdProcessManager() throws IOException {
    assumeTrue(SystemUtils.IS_OS_FREE_BSD);

    final ProcessManager processManager = FreeBSDProcessManager.getDefault();
    final Process process = Runtime.getRuntime().exec("ping -c 5 127.0.0.1");
    final ProcessQuery query = new ProcessQuery("ping", "-c 5 127.0.0.1");

    final long pid = processManager.findPid(query);
    assertThat(pid).isNotEqualTo(ProcessManager.PID_NOT_FOUND);
    final Number javaPid = Whitebox.getInternalState(process, "pid");
    assertThat(pid).isEqualTo(javaPid.longValue());

    processManager.kill(process, pid);
    assertThat(waitForPidNotFound(processManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
  }

  @Test
  public void freeBsdPureJavaProcessManager() throws IOException {
    assumeTrue(SystemUtils.IS_OS_FREE_BSD);

    final ProcessManager defaultManager = LocalOfficeUtils.findBestProcessManager();
    final ProcessManager processManager = PureJavaProcessManager.getDefault();
    final Process process = Runtime.getRuntime().exec("ping -c 5 127.0.0.1");
    final ProcessQuery query = new ProcessQuery("ping", "-c 5 127.0.0.1");

    final long pid = processManager.findPid(query);
    assertThat(pid).isEqualTo(ProcessManager.PID_UNKNOWN);

    processManager.kill(process, pid);
    assertThat(waitForPidNotFound(defaultManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
  }

  @Test
  public void unixProcessManager() throws IOException {
    assumeTrue(SystemUtils.IS_OS_UNIX && !SystemUtils.IS_OS_MAC && !SystemUtils.IS_OS_FREE_BSD);

    final ProcessManager processManager = UnixProcessManager.getDefault();
    final Process process = Runtime.getRuntime().exec("sleep 5s");
    final ProcessQuery query = new ProcessQuery("sleep", "5s");

    final long pid = processManager.findPid(query);
    assertThat(pid).isNotEqualTo(ProcessManager.PID_NOT_FOUND);
    final Number javaPid = Whitebox.getInternalState(process, "pid");
    assertThat(pid).isEqualTo(javaPid.longValue());

    processManager.kill(process, pid);
    assertThat(waitForPidNotFound(processManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
  }

  @Test
  public void unixPureJavaProcessManager() throws IOException {
    assumeTrue(SystemUtils.IS_OS_UNIX && !SystemUtils.IS_OS_MAC && !SystemUtils.IS_OS_FREE_BSD);

    final ProcessManager defaultManager = LocalOfficeUtils.findBestProcessManager();
    final ProcessManager processManager = PureJavaProcessManager.getDefault();
    final Process process = Runtime.getRuntime().exec("sleep 5s");
    final ProcessQuery query = new ProcessQuery("sleep", "5s");

    final long pid = processManager.findPid(query);
    assertThat(pid).isEqualTo(ProcessManager.PID_UNKNOWN);

    processManager.kill(process, pid);
    assertThat(waitForPidNotFound(defaultManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
  }

  @Test
  public void macProcessManager() throws IOException {
    assumeTrue(SystemUtils.IS_OS_MAC);

    final ProcessManager processManager = MacProcessManager.getDefault();
    final Process process = Runtime.getRuntime().exec("sleep 5s");
    final ProcessQuery query = new ProcessQuery("sleep", "5s");

    final long pid = processManager.findPid(query);
    assertThat(pid).isNotEqualTo(ProcessManager.PID_NOT_FOUND);
    final Number javaPid = Whitebox.getInternalState(process, "pid");

    assertThat(pid).isEqualTo(javaPid.longValue());

    processManager.kill(process, pid);
    assertThat(waitForPidNotFound(processManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
  }

  @Test
  public void macPureJavaProcessManager() throws IOException {
    assumeTrue(SystemUtils.IS_OS_MAC);

    final ProcessManager defaultManager = LocalOfficeUtils.findBestProcessManager();
    final ProcessManager processManager = PureJavaProcessManager.getDefault();
    final Process process = Runtime.getRuntime().exec("sleep 5s");
    final ProcessQuery query = new ProcessQuery("sleep", "5s");

    final long pid = processManager.findPid(query);
    assertThat(pid).isEqualTo(ProcessManager.PID_UNKNOWN);

    processManager.kill(process, pid);
    assertThat(waitForPidNotFound(defaultManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
  }

  @Test
  public void windowsProcessManager() throws IOException {
    assumeTrue(SystemUtils.IS_OS_WINDOWS);

    final ProcessManager processManager = WindowsProcessManager.getDefault();
    final Process process = Runtime.getRuntime().exec("ping 127.0.0.1 -n 5");
    final ProcessQuery query = new ProcessQuery("ping", "127.0.0.1 -n 5");

    final long pid = processManager.findPid(query);
    assertThat(pid).isNotEqualTo(ProcessManager.PID_NOT_FOUND);
    // Won't work on Windows, skip this assertion
    // Number javaPid = Whitebox.getInternalState(process, "pid");
    // assertThat(pid).isEqualTo(javaPid.longValue());

    processManager.kill(process, pid);
    assertThat(waitForPidNotFound(processManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
  }

  @Test
  public void windowsPureJavaProcessManager() throws IOException {
    assumeTrue(SystemUtils.IS_OS_WINDOWS);

    final ProcessManager defaultManager = LocalOfficeUtils.findBestProcessManager();
    final ProcessManager processManager = PureJavaProcessManager.getDefault();
    final Process process = Runtime.getRuntime().exec("ping 127.0.0.1 -n 5");
    final ProcessQuery query = new ProcessQuery("ping", "127.0.0.1 -n 5");

    final long pid = processManager.findPid(query);
    assertThat(pid).isEqualTo(ProcessManager.PID_UNKNOWN);

    processManager.kill(process, pid);
    assertThat(waitForPidNotFound(defaultManager, query)).isEqualTo(ProcessManager.PID_NOT_FOUND);
  }

  /**
   * Tests that using an custom process manager that does not appear in the classpath will fail with
   * an IllegalArgumentException.
   */
  @Test
  public void customProcessManagerNotFound() {

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                LocalOfficeManager.builder()
                    .processManager("org.foo.fallback.ProcessManager")
                    .build());
  }
}
