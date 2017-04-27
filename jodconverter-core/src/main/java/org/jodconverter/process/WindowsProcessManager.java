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
import java.util.regex.Pattern;

/**
 * {@link ProcessManager} implementation for Windows.
 *
 * <p>Requires wmic.exe and taskkill.exe, that should be available at least on Windows XP, Windows
 * Vista, and Windows 7 (except Home versions).
 */
public class WindowsProcessManager extends AbstractProcessManager {

  private static final Pattern PROCESS_GET_LINE =
      Pattern.compile("^\\s*(?<CommanLine>.*?)\\s+(?<Pid>\\d+)\\s*$");

  @Override
  protected String[] getRunningProcessesCommand(final String process) {

    return new String[] {
      "cmd", "/c", "wmic process where(name like '" + process + "%') get commandline,processid"
    };
  }

  @Override
  protected Pattern getRunningProcessLinePattern() {

    return PROCESS_GET_LINE;
  }

  /**
   * Gets whether the commands we need are available for a Windows OS.
   *
   * @return {@code true} if the required commands are available, {@code false} otherwise.
   */
  public boolean isUsable() {

    try {
      execute(new String[] {"wmic", "quit"});
      execute(new String[] {"taskkill", "/?"});
      return true;
    } catch (IOException ioEx) { // NOSONAR
      return false;
    }
  }

  @Override
  public void kill(final Process process, final long pid) throws IOException {

    execute(new String[] {"taskkill", "/t", "/f", "/pid", String.valueOf(pid)});
  }
}
