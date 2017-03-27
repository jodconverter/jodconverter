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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.testng.SkipException;
import org.testng.annotations.Test;

import org.artofsolving.jodconverter.util.PlatformUtils;

@Test
public class ProcessManagerTest {

  /**
   * Tests the UnixProcessManager class.
   *
   * @throws Exception if an error occurs.
   */
  public void unixProcessManager() throws Exception {
    if (PlatformUtils.isMac() || PlatformUtils.isWindows()) {
      throw new SkipException("UnixProcessManager only works on Unix");
    }
    final ProcessManager processManager = new UnixProcessManager();
    final Process process = new ProcessBuilder("sleep", "5s").start();
    final ProcessQuery query = new ProcessQuery("sleep", "5s");

    final long pid = processManager.findPid(query);
    assertFalse(pid == ProcessManager.PID_NOT_FOUND);
    final Number javaPid = (Number) FieldUtils.readDeclaredField(process, "pid", true);
    assertEquals(pid, javaPid.longValue());

    processManager.kill(process, pid);
    assertEquals(processManager.findPid(query), ProcessManager.PID_NOT_FOUND);
  }

  /**
   * Tests the MacProcessManager class.
   *
   * @throws Exception if an error occurs.
   */
  public void macProcessManager() throws Exception {
    if (!PlatformUtils.isMac()) {
      throw new SkipException("MacProcessManager only works on Mac");
    }
    final ProcessManager processManager = new MacProcessManager();
    final Process process = new ProcessBuilder("sleep", "5s").start();
    final ProcessQuery query = new ProcessQuery("sleep", "5s");

    final long pid = processManager.findPid(query);
    assertFalse(pid == ProcessManager.PID_NOT_FOUND);
    final Number javaPid = (Number) FieldUtils.readDeclaredField(process, "pid", true);

    assertEquals(pid, javaPid.longValue());

    processManager.kill(process, pid);
    assertEquals(processManager.findPid(query), ProcessManager.PID_NOT_FOUND);
  }

  /**
   * Tests the WindowsProcessManager class.
   *
   * @throws Exception if an error occurs.
   */
  public void windowsProcessManager() throws Exception {
    if (!PlatformUtils.isWindows()) {
      throw new SkipException("WindowsProcessManager only works on Windows");
    }
    final ProcessManager processManager = new WindowsProcessManager();
    final Process process = new ProcessBuilder("ping", "127.0.0.1", "-n", "5").start();
    final ProcessQuery query = new ProcessQuery("ping", "127.0.0.1 -n 5");

    final long pid = processManager.findPid(query);
    assertFalse(pid == ProcessManager.PID_NOT_FOUND);
    // Won't work on Windows, skit this assertion
    //Number javaPid = (Number) FieldUtils.readDeclaredField(process, "pid", true);
    //assertEquals(pid, javaPid.longValue());

    processManager.kill(process, pid);
    assertEquals(processManager.findPid(query), ProcessManager.PID_NOT_FOUND);
  }
}
