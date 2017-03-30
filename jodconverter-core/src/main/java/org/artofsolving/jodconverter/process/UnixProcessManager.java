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

import org.apache.commons.io.IOUtils;

/**
 * {@link ProcessManager} implementation for *nix systems. Uses the <tt>ps</tt> and <tt>kill</tt>
 * commands.
 *
 * <p>Works for Linux. Works for Solaris too, except that the command line string returned by
 * <tt>ps</tt> there is limited to 80 characters and this affects {@link #findPid(String)}.
 */
public class UnixProcessManager extends AbstractProcessManager {

  private static final Pattern PS_OUTPUT_LINE = Pattern.compile("^\\s*(\\d+)\\s+(.*)$");

  private String runAsArgs;

  @Override
  protected List<String> execute(final String command) throws IOException {

    String cmd;
    if (runAsArgs == null) {
      cmd = command;
    } else {
      cmd = runAsArgs + " " + command;
    }
    final Process process = Runtime.getRuntime().exec(cmd);
    return IOUtils.readLines(process.getInputStream(), "UTF-8");
  }

  @Override
  protected String getCurrentProcessesCommand(final String process) {

    return "/bin/ps -e -o pid,args | grep " + process;
  }

  @Override
  protected Pattern getProcessLinePattern() {

    return PS_OUTPUT_LINE;
  }

  @Override
  public void kill(final Process process, final long pid) throws IOException {

    execute("/bin/kill -KILL " + pid);
  }

  /**
   * Sets the sudo command arguments.
   *
   * @param runAsArgs the sudo command arguments.
   */
  public void setRunAsArgs(final String runAsArgs) {
    this.runAsArgs = runAsArgs;
  }
}
