/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.process;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

/**
 * {@link ProcessManager} implementation for *nix systems. Uses the <tt>ps</tt> and <tt>kill</tt>
 * commands.
 *
 * <p>Works for Linux. Works for Solaris too, except that the command line string returned by
 * <tt>ps</tt> there is limited to 80 characters and this affects {@link #findPid(ProcessQuery)}.
 */
public class UnixProcessManager extends AbstractProcessManager {

  private static final Pattern PS_OUTPUT_LINE =
      Pattern.compile("^\\s*(?<Pid>\\d+)\\s+(?<CommanLine>.*)$");

  private String[] runAsArgs;

  // This class is required in order to create the default UnixProcessManager
  // only on demand, as explained by the Initialization-on-demand holder idiom:
  // https://www.wikiwand.com/en/Initialization-on-demand_holder_idiom
  private static class DefaultHolder { // NOSONAR
    static final UnixProcessManager INSTANCE = new UnixProcessManager();
  }

  /**
   * Gets the default instance of {@code UnixProcessManager}.
   *
   * @return The default {@code UnixProcessManager} instance.
   */
  public static UnixProcessManager getDefault() {
    return DefaultHolder.INSTANCE;
  }

  @Override
  protected List<String> execute(final String[] cmdarray) throws IOException {

    if (runAsArgs == null) {
      return super.execute(cmdarray);
    }

    final String[] newarray = new String[runAsArgs.length + cmdarray.length];
    System.arraycopy(runAsArgs, 0, newarray, 0, runAsArgs.length);
    System.arraycopy(cmdarray, 0, newarray, runAsArgs.length, cmdarray.length);

    return super.execute(newarray);
  }

  @Override
  protected String[] getRunningProcessesCommand(final String process) {

    return new String[] {
      "/bin/bash", "-c", "/bin/ps -e -o pid,args | /bin/grep " + process + " | /bin/grep -v grep"
    };
  }

  @Override
  protected Pattern getRunningProcessLinePattern() {

    return PS_OUTPUT_LINE;
  }

  @Override
  public void kill(final Process process, final long pid) throws IOException {

    execute(new String[] {"/bin/kill", "-KILL", String.valueOf(pid)});
  }

  /**
   * Sets The sudo command arguments.
   *
   * @param runAsArgs The sudo command arguments.
   */
  public void setRunAsArgs(final String[] runAsArgs) {
    this.runAsArgs = ArrayUtils.clone(runAsArgs);
  }
}
