/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.process;

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
