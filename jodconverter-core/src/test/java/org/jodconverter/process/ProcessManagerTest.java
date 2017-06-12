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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;

import org.jodconverter.OfficeDocumentConverter;
import org.jodconverter.office.DefaultOfficeManagerBuilder;
import org.jodconverter.office.OfficeManager;

public class ProcessManagerTest {

  /**
   * Tests the UnixProcessManager class.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void unixProcessManager() throws Exception {
    assumeTrue(SystemUtils.IS_OS_UNIX && !SystemUtils.IS_OS_MAC);

    final ProcessManager processManager = new UnixProcessManager();
    final Process process = Runtime.getRuntime().exec("sleep 5s");
    final ProcessQuery query = new ProcessQuery("sleep", "5s");

    final long pid = processManager.findPid(query);
    assertNotEquals(pid, ProcessManager.PID_NOT_FOUND);
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
  @Test
  public void macProcessManager() throws Exception {
    assumeTrue(SystemUtils.IS_OS_MAC);

    final ProcessManager processManager = new MacProcessManager();
    final Process process = Runtime.getRuntime().exec("sleep 5s");
    final ProcessQuery query = new ProcessQuery("sleep", "5s");

    final long pid = processManager.findPid(query);
    assertNotEquals(pid, ProcessManager.PID_NOT_FOUND);
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
  @Test
  public void windowsProcessManager() throws Exception {
    assumeTrue(SystemUtils.IS_OS_WINDOWS);

    final ProcessManager processManager = new WindowsProcessManager();
    final Process process = Runtime.getRuntime().exec("ping 127.0.0.1 -n 5");
    final ProcessQuery query = new ProcessQuery("ping", "127.0.0.1 -n 5");

    final long pid = processManager.findPid(query);
    assertNotEquals(pid, ProcessManager.PID_NOT_FOUND);
    // Won't work on Windows, skip this assertion
    //Number javaPid = (Number) FieldUtils.readDeclaredField(process, "pid", true);
    //assertEquals(pid, javaPid.longValue());

    processManager.kill(process, pid);
    assertEquals(processManager.findPid(query), ProcessManager.PID_NOT_FOUND);
  }

  /**
   * Tests that using an custom process manager that appears in the classpath will be used.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void customProcessManager() throws Exception {

    final File dir = new File("src/test/resources/documents");
    final File inputFile = dir.listFiles((FileFilter) FileFileFilter.FILE)[0];
    final File outputFile = File.createTempFile("test", ".pdf");

    final OfficeManager officeManager =
        new DefaultOfficeManagerBuilder()
            .setProcessManager("org.jodconverter.process.CustomProcessManager")
            .build();
    final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

    officeManager.start();
    try {
      converter.convert(inputFile, outputFile);
      assertTrue(outputFile.isFile() && outputFile.length() > 0);
    } finally {
      officeManager.stop();
    }
  }

  /**
   * Tests that using an custom process manager that does not appear in the classpath will fall back
   * to an auto-detect process manager.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void customProcessManagerFallback() throws Exception {

    final File dir = new File("src/test/resources/documents");
    final File inputFile = dir.listFiles((FileFilter) FileFileFilter.FILE)[0];
    final File outputFile = File.createTempFile("test", ".pdf");

    final OfficeManager officeManager =
        new DefaultOfficeManagerBuilder()
            .setProcessManager("org.foo.fallback.ProcessManager")
            .build();
    final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

    officeManager.start();
    try {
      converter.convert(inputFile, outputFile);
      assertTrue(outputFile.isFile() && outputFile.length() > 0);
    } finally {
      officeManager.stop();
    }
  }
}
