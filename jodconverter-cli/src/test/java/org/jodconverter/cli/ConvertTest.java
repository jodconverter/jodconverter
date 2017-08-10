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

package org.jodconverter.cli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.jodconverter.cli.util.ExitException;
import org.jodconverter.cli.util.NoExitSecurityManager;
import org.jodconverter.cli.util.SystemLogHandler;
import org.jodconverter.office.OfficeUtils;

public class ConvertTest {

  /**
   * Redirects the console output and also changes the security manager so we can trap the exit code
   * of the application.
   */
  @BeforeClass
  public static void setUpClass() {

    // Don't allow the program to exit the VM and redirect
    // console streams.
    System.setOut(new SystemLogHandler(System.out));
    System.setErr(new SystemLogHandler(System.err));
    System.setSecurityManager(new NoExitSecurityManager());
  }

  /** Resets the security manager. */
  @AfterClass
  public static void tearDownClass() {

    // Restore security manager
    System.setSecurityManager(null);
  }

  @Before
  public void setUp() {

    ExitException.INSTANCE.reset();
  }

  @Test
  public void main_WithOptionHelp_PrintHelpAndExitWithCode0() throws Exception {

    try {
      SystemLogHandler.startCapture();
      Convert.main(new String[] {"-h"});

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains("jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 0);
    }
  }

  @Test
  public void main_WithOptionHelp_PrintVersionAndExitWithCode0() throws Exception {

    try {
      SystemLogHandler.startCapture();
      Convert.main(new String[] {"-v"});

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog).contains("jodconverter-cli version");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 0);
    }
  }

  @Test
  public void main_WithUnknownArgument_PrintErrorHelpAndExitWithCode2() throws Exception {

    try {
      SystemLogHandler.startCapture();
      Convert.main(new String[] {"-xyz"});

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains(
              "Unrecognized option: -xyz",
              "jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 2);
    }
  }

  @Test
  public void main_WithMissingsFilenames_PrintErrorHelpAndExitWithCode255() throws Exception {

    try {
      SystemLogHandler.startCapture();
      Convert.main(new String[] {""});

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains("jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 255);
    }
  }

  @Test
  public void main_WithWrongFilenamesLength_PrintErrorHelpAndExitWithCode255() throws Exception {

    try {
      SystemLogHandler.startCapture();
      Convert.main(new String[] {"input1.txt", "output1.pdf", "input2.txt"});

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains("jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 255);
    }
  }

  @Test
  public void main_WithAllCustomizableOption_ExecuteAndExitWithCod0() throws Exception {

    try {
      SystemLogHandler.startCapture();
      Convert.main(
          new String[] {
            "-i", OfficeUtils.getDefaultOfficeHome().getPath(),
            "-m", OfficeUtils.findBestProcessManager().getClass().getName(),
            "-t", "30000",
            "-p", "2002",
            "input1.txt", "output1.pdf"
          });

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .contains("jodconverter-cli [options] infile outfile [infile outfile ...]");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 255);
    }
  }
}
