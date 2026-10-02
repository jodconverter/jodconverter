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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link org.jodconverter.local.process.ProcessManager} implementation for Windows.
 *
 * <p>Requires taskkill.exe, and either wmic.exe or powershell.exe to query the running processes.
 * wmic.exe is used when it is available. It has been removed from recent versions of Windows
 * (Windows 11 24H2, Windows Server 2025), where powershell.exe is used instead.
 */
public class WindowsProcessManager extends AbstractProcessManager {

  private static final Pattern PROCESS_GET_LINE =
      Pattern.compile("^\\s*(?<CommandLine>.*?)\\s+(?<Pid>\\d+)\\s*$");

  // Whether wmic.exe is available. null means that it has not been checked yet.
  private final AtomicReference<Boolean> wmicAvailable = new AtomicReference<>();

  // Whether the running processes can be queried through powershell.exe. null means that it has
  // not been checked yet.
  private final AtomicReference<Boolean> powershellQueryWorking = new AtomicReference<>();

  /**
   * This class is required in order to create the default WindowsProcessManager only on demand, as
   * explained by the Initialization-on-demand holder idiom:
   * https://www.wikiwand.com/en/Initialization-on-demand_holder_idiom
   */
  private static class DefaultHolder { // NOPMD - Disable utility class name rule violation
    /* default */ static final WindowsProcessManager INSTANCE = new WindowsProcessManager();
  }

  /**
   * Gets the default instance of {@code WindowsProcessManager}.
   *
   * @return The default {@code WindowsProcessManager} instance.
   */
  public static @NonNull WindowsProcessManager getDefault() {
    return DefaultHolder.INSTANCE;
  }

  @Override
  protected @NonNull String[] getRunningProcessesCommand(final @NonNull String process) {

    if (isWmicAvailable()) {
      return new String[] {
        "cmd", "/c", "wmic process where(name like '" + process + "%') get commandline,processid"
      };
    }

    // Each line of the output is the command line of a process followed by its pid, as wmic does.
    // The progress records are disabled since powershell writes them to the error stream.
    final String script =
        "$ProgressPreference = 'SilentlyContinue'; "
            + "Get-CimInstance Win32_Process -Filter \"Name like '"
            + process.replace("'", "''")
            + "%'\" | ForEach-Object { \"$($_.CommandLine) $($_.ProcessId)\" }";

    // The script is encoded since the quotes it contains would not survive the way the arguments
    // of a command are quoted on Windows.
    return new String[] {
      "powershell",
      "-NoProfile",
      "-NonInteractive",
      "-EncodedCommand",
      Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE))
    };
  }

  @Override
  protected @NonNull Pattern getRunningProcessLinePattern() {

    return PROCESS_GET_LINE;
  }

  /**
   * Gets whether the commands we need are available for a Windows OS.
   *
   * @return {@code true} If the required commands are available, {@code false} otherwise.
   */
  public boolean isUsable() {

    try {
      if (!isWmicAvailable() && !isPowershellQueryWorking()) {
        return false;
      }
      execute(new String[] {"taskkill", "/?"});
      return true;
    } catch (IOException ioEx) {
      return false;
    }
  }

  private boolean isPowershellQueryWorking() {

    Boolean working = powershellQueryWorking.get();
    if (working == null) {
      try {
        // Being able to start powershell.exe is not enough, since a policy may prevent the query
        // from working. So we execute the query for real: the powershell process executing it must
        // be found in its own output.
        working =
            execute(getRunningProcessesCommand("powershell")).stream()
                .anyMatch(line -> PROCESS_GET_LINE.matcher(line).matches());
      } catch (IOException ioEx) {
        working = false;
      }
      powershellQueryWorking.set(working);
    }
    return working;
  }

  private boolean isWmicAvailable() {

    Boolean available = wmicAvailable.get();
    if (available == null) {
      try {
        execute(new String[] {"wmic", "quit"});
        available = true;
      } catch (IOException ioEx) {
        available = false;
      }
      wmicAvailable.set(available);
    }
    return available;
  }

  @Override
  public void kill(final @Nullable Process process, final long pid) throws IOException {
    if (pid > PID_UNKNOWN) {
      execute(new String[] {"taskkill", "/t", "/f", "/pid", String.valueOf(pid)});
    } else {
      super.kill(process, pid);
    }
  }
}
