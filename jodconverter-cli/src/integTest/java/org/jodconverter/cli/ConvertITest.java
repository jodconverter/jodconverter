/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.cli.util.ConsoleStreamsListenerExtension;
import org.jodconverter.cli.util.ExitException;
import org.jodconverter.cli.util.NoExitExtension;
import org.jodconverter.cli.util.ResetExitExceptionExtension;
import org.jodconverter.cli.util.SystemLogHandler;
import org.jodconverter.office.LocalOfficeUtils;

/**
 * This class tests the {@link Convert} class, which contains the main function of the cli module.
 */
@ExtendWith({
  ConsoleStreamsListenerExtension.class,
  NoExitExtension.class,
  ResetExitExceptionExtension.class
})
public class ConvertITest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConvertITest.class);
  private static final String CONFIG_DIR = "src/integTest/resources/config/";
  private static final String SOURCE_FILE = "src/integTest/resources/documents/test1.doc";
  private static final String SOURCE_MULTI_FILE =
      "src/integTest/resources/documents/test_multi_page.doc";

  @Test
  public void convert_WithCustomFormatRegistry_ShouldSupportOnlyTargetTxtOrPdf(
      @TempDir File testFolder) {

    final File registryFile = new File(CONFIG_DIR + "cli-document-formats.json");
    final File inputFile = new File(SOURCE_FILE);
    final File outputFile = new File(testFolder, "convert_WithMultipleFilters.doc");

    SystemLogHandler.startCapture();
    assertThatExceptionOfType(ExitException.class)
        .isThrownBy(
            () ->
                Convert.main(
                    new String[] {
                      "-k", "-r", registryFile.getPath(), inputFile.getPath(), outputFile.getPath()
                    }))
        .satisfies(
            e -> {
              final String capturedlog = SystemLogHandler.stopCapture();
              assertThat(e).hasFieldOrPropertyWithValue("status", 2);
              assertThat(capturedlog).contains("The target format is missing or not supported");
            });
  }

  @Test
  public void convert_WithFilenames_ShouldSucceed(@TempDir File testFolder) {

    final File inputFile = new File(SOURCE_FILE);
    final File outputFile = new File(testFolder, "convert_WithFilenames.pdf");

    assertThatExceptionOfType(ExitException.class)
        .isThrownBy(
            () -> Convert.main(new String[] {"-k", inputFile.getPath(), outputFile.getPath()}))
        .satisfies(
            e -> {
              assertThat(e)
                  .isExactlyInstanceOf(ExitException.class)
                  .hasFieldOrPropertyWithValue("status", 0);

              assertThat(outputFile).isFile();
              assertThat(outputFile.length()).isGreaterThan(0L);
            });
  }

  @Test
  public void convert_WithOutputFormat_ShouldSucceed(@TempDir File testFolder) throws Exception {

    final File inputFile = new File(SOURCE_FILE);
    FileUtils.copyFileToDirectory(inputFile, testFolder);
    final File inputFileTmp = new File(testFolder, FilenameUtils.getName(SOURCE_FILE));
    final File outputFile =
        new File(testFolder, FilenameUtils.getBaseName(inputFile.getName()) + ".pdf");

    assertThatExceptionOfType(ExitException.class)
        .isThrownBy(() -> Convert.main(new String[] {"-k", "-f", "pdf", inputFileTmp.getPath()}))
        .satisfies(
            e -> {
              assertThat(e).hasFieldOrPropertyWithValue("status", 0);
              assertThat(outputFile).isFile();
              assertThat(outputFile.length()).isGreaterThan(0L);
            });
  }

  @Test
  public void convert_WithMultipleFilters_ShouldSucceed(@TempDir File testFolder) {

    final File filterChainFile = new File(CONFIG_DIR + "applicationContext_multipleFilters.xml");
    final File inputFile = new File(SOURCE_FILE);
    final File outputFile = new File(testFolder, "convert_WithMultipleFilters.pdf");

    assertThatExceptionOfType(ExitException.class)
        .isThrownBy(
            () ->
                Convert.main(
                    new String[] {
                      "-k",
                      "-a",
                      filterChainFile.getPath(),
                      inputFile.getPath(),
                      outputFile.getPath()
                    }))
        .satisfies(
            e -> {
              assertThat(e).hasFieldOrPropertyWithValue("status", 0);

              assertThat(outputFile).isFile();
              assertThat(outputFile.length()).isGreaterThan(0L);
            });
  }

  @Test
  public void convert_WithSingleFilter_ShouldSucceed(@TempDir File testFolder) {

    final File filterChainFile = new File(CONFIG_DIR + "applicationContext_pageSelectorFilter.xml");
    final File inputFile = new File(SOURCE_MULTI_FILE);
    final File outputFile = new File(testFolder, "convert_WithSingleFilter.txt");

    assertThatExceptionOfType(ExitException.class)
        .isThrownBy(
            () ->
                Convert.main(
                    new String[] {
                      "-k",
                      "-a",
                      filterChainFile.getPath(),
                      inputFile.getPath(),
                      outputFile.getPath()
                    }))
        .satisfies(
            e -> {
              assertThat(e).hasFieldOrPropertyWithValue("status", 0);

              try {
                final String content =
                    FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
                assertThat(content)
                    .as("Check content: %s", content)
                    .contains("Test document Page 2")
                    .doesNotContain("Test document Page 1")
                    .doesNotContain("Test document Page 3");
              } catch (IOException ex) {
                assertThat(ex).isNull();
              }
            });
  }

  @Test
  public void convert_WithCustomStoreProperties_ShouldSucceed(@TempDir File testFolder) {

    final File inputFile = new File(SOURCE_MULTI_FILE);
    final File outputFile = new File(testFolder, "convert_WithCustomStoreProperties.pdf");

    assertThatExceptionOfType(ExitException.class)
        .isThrownBy(
            () ->
                Convert.main(
                    new String[] {
                      "-k", "-sFDPageRange=2-2", inputFile.getPath(), outputFile.getPath()
                    }))
        .satisfies(
            e -> {
              assertThat(e).hasFieldOrPropertyWithValue("status", 0);

              // If the document (with the image) is fully converted, it will
              // be much greater that 30K (over 70K). Only the second page
              // doesn't have an image.
              assertThat(outputFile.length()).isLessThan(30000);
            });
  }

  @Test
  public void main_WithAllCustomizableOption_ExecuteAndExitWithCod0() {

    assertThatExceptionOfType(ExitException.class)
        .isThrownBy(
            () ->
                Convert.main(
                    new String[] {
                      "-g",
                      "-k",
                      "-i",
                      LocalOfficeUtils.getDefaultOfficeHome().getPath(),
                      "-m",
                      LocalOfficeUtils.findBestProcessManager().getClass().getName(),
                      "-t",
                      "30000",
                      "-p",
                      "2002",
                      "input1.txt",
                      "output1.pdf"
                    }))
        .satisfies(e -> assertThat(e.getStatus()).isEqualTo(0));
  }
}
