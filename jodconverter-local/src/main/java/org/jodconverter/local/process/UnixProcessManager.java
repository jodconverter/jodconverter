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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link org.jodconverter.local.process.ProcessManager} implementation for *nix systems. Uses the
 * {@code ps} and {@code kill} commands.
 *
 * <p>Works for Linux. Works for Solaris too, except that the command line string returned by {@code
 * ps} there is limited to 80 characters and this affects {@link #findPid(ProcessQuery)}.
 */
public class UnixProcessManager extends AbstractProcessManager {

  private static final Pattern PS_OUTPUT_LINE =
      Pattern.compile("^\\s*(?<Pid>\\d+)\\s+(?<CommandLine>.*)$");

  private String[] runAsArgs;

  /**
   * This class is required in order to create the default UnixProcessManager only on demand, as
   * explained by the Initialization-on-demand holder idiom:
   * https://www.wikiwand.com/en/Initialization-on-demand_holder_idiom
   */
  private static class DefaultHolder { // NOPMD - Disable utility class name rule violation
    /* default */ static final UnixProcessManager INSTANCE = new UnixProcessManager();
  }

  /**
   * Gets the default instance of {@code UnixProcessManager}.
   *
   * @return The default {@code UnixProcessManager} instance.
   */
  public static @NonNull UnixProcessManager getDefault() {
    return DefaultHolder.INSTANCE;
  }

  @Override
  protected @NonNull List<@NonNull String> execute(final @NonNull String[] cmdarray)
      throws IOException {

    if (runAsArgs == null) {
      return super.execute(cmdarray);
    }

    final String[] newarray = new String[runAsArgs.length + cmdarray.length];
    System.arraycopy(runAsArgs, 0, newarray, 0, runAsArgs.length);
    System.arraycopy(cmdarray, 0, newarray, runAsArgs.length, cmdarray.length);

    return super.execute(newarray);
  }

  @Override
  protected @NonNull String[] getRunningProcessesCommand(final @NonNull String process) {

    return new String[] {
      "/bin/sh", "-c", "/bin/ps -e -o pid,args | /bin/grep " + process + " | /bin/grep -v grep"
    };
  }

  @Override
  protected @NonNull Pattern getRunningProcessLinePattern() {

    return PS_OUTPUT_LINE;
  }

  @Override
  public void kill(final @Nullable Process process, final long pid) throws IOException {
    if (pid > PID_UNKNOWN) {
      execute(new String[] {"/bin/kill", "-KILL", String.valueOf(pid)});
    } else {
      super.kill(process, pid);
    }
  }

  /**
   * Sets The sudo command arguments.
   *
   * @param runAsArgs The sudo command arguments.
   */
  public void setRunAsArgs(final @NonNull String[] runAsArgs) {
    this.runAsArgs = Arrays.copyOf(runAsArgs, runAsArgs.length);
  }
}
