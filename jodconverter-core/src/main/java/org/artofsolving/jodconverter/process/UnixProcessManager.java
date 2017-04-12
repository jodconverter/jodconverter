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
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@link ProcessManager} implementation for *nix systems. Uses the <tt>ps</tt> and <tt>kill</tt>
 * commands.
 *
 * <p>Works for Linux. Works for Solaris too, except that the command line string returned by
 * <tt>ps</tt> there is limited to 80 characters and this affects {@link #findPid(String)}.
 */
public class UnixProcessManager extends AbstractProcessManager {

  private static final Pattern PS_OUTPUT_LINE =
      Pattern.compile("^\\s*(?<Pid>\\d+)\\s+(?<CommanLine>.*)$");

  private String[] runAsArgs;

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
   * Sets the sudo command arguments.
   *
   * @param runAsArgs the sudo command arguments.
   */
  public void setRunAsArgs(final String[] runAsArgs) {
    this.runAsArgs = runAsArgs;
  }
}
