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

/**
 * The process manager of Windows, where the JVM does not read the command lines of the other
 * processes: the running processes are listed with a PowerShell query.
 */
public class WindowsProcessManager extends AbstractProcessManager {

  private static final Pattern PROCESS_GET_LINE =
      Pattern.compile("^\\s*(?<CommandLine>.*?)\\s+(?<Pid>\\d+)\\s*$");

  // Whether the running processes can be queried through powershell.exe. null means that it has
  // not been checked yet.
  private final AtomicReference<Boolean> powershellQueryWorking = new AtomicReference<>();

  private static class DefaultHolder { // NOPMD - Disable utility class name rule violation
    /* default */ static final WindowsProcessManager INSTANCE = new WindowsProcessManager();
  }

  /**
   * Gets the default instance of this manager.
   *
   * @return The default instance.
   */
  public static @NonNull WindowsProcessManager getDefault() {
    return DefaultHolder.INSTANCE;
  }

  @Override
  protected @NonNull String[] getRunningProcessesCommand(final @NonNull String process) {

    // Each line of the output is the command line of a process followed by its pid. The progress
    // records are disabled since powershell writes them to the error stream.
    final var script =
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
   * Gets whether this manager can list the running processes on the current machine.
   *
   * @return {@code true} if the PowerShell query works, {@code false} otherwise.
   */
  public boolean isUsable() {

    var working = powershellQueryWorking.get();
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
}
