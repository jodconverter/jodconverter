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
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.cli.util.ConsoleStreamsListener;
import org.jodconverter.cli.util.ExitException;
import org.jodconverter.cli.util.NoExitResource;
import org.jodconverter.cli.util.ResetExitExceptionResource;
import org.jodconverter.cli.util.SystemLogHandler;
import org.jodconverter.office.OfficeUtils;

/**
 * This class tests the {@link Convert} class, which contains the main function of the cli module.
 */
public class ConvertITest {

  private static final String CONFIG_DIR = "src/integTest/resources/config/";
  private static final String SOURCE_FILE = "src/integTest/resources/documents/test1.doc";
  private static final String SOURCE_MULTI_PAGE_FILE =
      "src/integTest/resources/documents/test_multi_page.doc";

  @ClassRule public static NoExitResource noExit = new NoExitResource();
  @ClassRule public static ConsoleStreamsListener consoleListener = new ConsoleStreamsListener();
  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  @Rule public ResetExitExceptionResource resetExitEx = new ResetExitExceptionResource();

  @Test
  public void main_WithOneSourceFileAndOneTargetFile_ShouldConvertSuccessfully() throws Exception {

    final File inputFile = new File(SOURCE_FILE);
    final File outputFile =
        new File(testFolder.getRoot(), "main_WithOneSourceFileAndOneTargetFile.pdf");

    assertThat(outputFile).doesNotExist();

    try {
      Convert.main(new String[] {"-k", inputFile.getPath(), outputFile.getPath()});

      // Be sure the ExitException exception is thrown.
      fail();

    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 0);

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }
  }

  @Test
  public void main_WithOneSourceFileAndOutputFormat_ShouldConvertSuccessfully() throws Exception {

    final File inputFile = new File(SOURCE_FILE);
    FileUtils.copyFileToDirectory(inputFile, testFolder.getRoot());
    final File renamedInputFile =
        new File(
            testFolder.getRoot(),
            "main_WithOutputFormat." + FilenameUtils.getExtension(inputFile.getName()));
    FileUtils.moveFile(new File(testFolder.getRoot(), inputFile.getName()), renamedInputFile);
    final File outputFile = new File(testFolder.getRoot(), "main_WithOutputFormat.pdf");

    assertThat(outputFile).doesNotExist();

    try {
      Convert.main(new String[] {"-k", "-f", "pdf", renamedInputFile.getPath()});

      // Be sure the ExitException exception is thrown.
      fail();

    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 0);

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }
  }

  @Test
  public void main_WithCustomFilterChain_ShouldConvertSuccessfully() throws Exception {

    final File filterChainFile = new File(CONFIG_DIR, "applicationContext_pageSelectorFilter.xml");
    final File inputFile = new File(SOURCE_MULTI_PAGE_FILE);
    final File outputFile1 =
        new File(testFolder.getRoot(), "main_WithCustomFilterChain_WithoutFilter.txt");
    final File outputFile2 =
        new File(testFolder.getRoot(), "main_WithCustomFilterChain_WithFilter.txt");

    assertThat(outputFile1).doesNotExist();
    assertThat(outputFile2).doesNotExist();

    try {
      Convert.main(new String[] {"-k", inputFile.getPath(), outputFile1.getPath()});

      // Be sure the ExitException exception is thrown.
      fail();

    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 0);

      final String content = FileUtils.readFileToString(outputFile1, Charset.forName("UTF-8"));
      assertThat(content)
          .contains("Test document Page 1")
          .contains("Test document Page 2")
          .contains("Test document Page 3");
    }

    try {
      Convert.main(
          new String[] {
            "-k", "-a", filterChainFile.getPath(), inputFile.getPath(), outputFile2.getPath()
          });

      // Be sure the ExitException exception is thrown.
      fail();

    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 0);

      final String content = FileUtils.readFileToString(outputFile2, Charset.forName("UTF-8"));
      assertThat(content)
          .contains("Test document Page 2")
          .doesNotContain("Test document Page 1")
          .doesNotContain("Test document Page 3");
    }
  }

  @Test
  public void main_WithAllCustomizableOption_ExecuteAndExitWithCod0() throws Exception {

    try {
      Convert.main(
          new String[] {
            "-k",
            "-i",
            OfficeUtils.getDefaultOfficeHome().getPath(),
            "-m",
            OfficeUtils.findBestProcessManager().getClass().getName(),
            "-u",
            "src/integTest/resources/templateProfileDir",
            "-t",
            "30000",
            "-p",
            "2002",
            "input1.txt",
            "output1.pdf"
          });

      // Be sure the ExitException exception is thrown.
      fail();

    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 0);
    }
  }

  @Test
  public void main_WithCustomDocumentFormats_ShouldSupportOnlyCustomFormats() throws Exception {

    final File outputFile = new File(testFolder.getRoot(), "main_WithCustomDocumentFormats.pdf");
    try {
      SystemLogHandler.startCapture();
      Convert.main(
          new String[] {
            "-k",
            "-o",
            "-r",
            new File("src/integTest/resources/cli-document-formats.json").getPath(),
            SOURCE_FILE,
            outputFile.getPath()
          });

      // Be sure the ExitException exception is thrown.
      fail();

    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog).contains("The source format is missing or not supported");
      assertThat(ex)
          .isExactlyInstanceOf(ExitException.class)
          .hasFieldOrPropertyWithValue("status", 2);
    }
  }
}
