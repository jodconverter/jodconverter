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

package org.artofsolving.jodconverter.office;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;

import com.sun.star.lib.uno.helper.UnoUrl;

import org.artofsolving.jodconverter.process.PureJavaProcessManager;

public class ExternalOfficeManagerTest {

  //TODO test auto-reconnection

  /**
   * Test a conversion task execution though the ExternalOfficeManager class.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void executeTask() throws Exception {
    final UnoUrl unoUrl = UnoUrlUtils.socket(2002);
    final OfficeProcess officeProcess =
        new OfficeProcess(
            OfficeUtils.getDefaultOfficeHome(),
            unoUrl,
            null,
            null,
            new File(System.getProperty("java.io.tmpdir")),
            new PureJavaProcessManager(),
            true);
    officeProcess.start();
    Thread.sleep(2000); // NOSONAR
    final Integer exitCode = officeProcess.getExitCode();
    if (exitCode != null && exitCode.equals(Integer.valueOf(81))) {
      officeProcess.start(true);
      Thread.sleep(2000); // NOSONAR
    }

    final ExternalOfficeManager manager = new ExternalOfficeManager(unoUrl, true);
    manager.start();

    final MockOfficeTask task = new MockOfficeTask();
    manager.execute(task);
    assertTrue(task.isCompleted());

    manager.stop();
    //TODO replace when OfficeProcess has a forciblyTerminate()
    final Process process = (Process) FieldUtils.readDeclaredField(officeProcess, "process", true);
    process.destroy();
  }
}
