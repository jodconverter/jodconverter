/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.cli.util.ConsoleStreamsListenerExtension;
import org.jodconverter.cli.util.SystemLogHandler;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeUtils;

/**
 * This class tests the {@link Convert} class, which contains the main function of the cli module.
 */
@ExtendWith(ConsoleStreamsListenerExtension.class)
class ConvertITest {

  private static final String CONFIG_DIR = "src/integTest/resources/config/";
  private static final String SOURCE_FILE = "src/integTest/resources/documents/test1.doc";
  private static final String SOURCE_MULTI_FILE =
      "src/integTest/resources/documents/test_multi_page.doc";

  @Nested
  class ConvertTests {

    @Test
    void withCustomFormatRegistry_ShouldSupportOnlyTargetTxtOrPdf(final @TempDir File testFolder) {

      final var registryFile = new File(CONFIG_DIR + "cli-document-formats.json");
      final var inputFile = new File(SOURCE_FILE);
      final var outputFile = new File(testFolder, "convert_WithMultipleFilters.doc");

      SystemLogHandler.startCapture();
      final var status =
          Convert.run(
              "-r",
              registryFile.getPath(),
              "-x",
              ExistingProcessAction.KILL.toString(),
              inputFile.getPath(),
              outputFile.getPath());
      final var capturedlog = SystemLogHandler.stopCapture();
      assertThat(status).isEqualTo(2);
      assertThat(capturedlog).contains("The target format is missing or not supported");
    }

    @Test
    void withFilenames_ShouldSucceed(final @TempDir File testFolder) {

      final var inputFile = new File(SOURCE_FILE);
      final var outputFile = new File(testFolder, "convert_WithFilenames.pdf");

      final var status =
          Convert.run(
              "-x",
              ExistingProcessAction.KILL.toString(),
              inputFile.getPath(),
              outputFile.getPath());
      assertThat(status).isEqualTo(0);
      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withPdfOptions_ShouldApplyThemToThePdfOutputsOnly(final @TempDir File testFolder)
        throws Exception {

      final var inputFile = new File(SOURCE_MULTI_FILE);
      final var pdfFile = new File(testFolder, "convert_WithPdfOptions.pdf");
      final var odtFile = new File(testFolder, "convert_WithPdfOptions.odt");

      final var status =
          Convert.run(
              "-x",
              ExistingProcessAction.KILL.toString(),
              "--pdf-preset",
              "compact",
              "--pdf-option",
              "version=1.5",
              "--pdf-option",
              "pages.range=1",
              inputFile.getPath(),
              pdfFile.getPath(),
              inputFile.getPath(),
              odtFile.getPath());

      assertThat(status).isEqualTo(0);
      assertThat(odtFile).isFile();
      final var pdf = FileUtils.readFileToString(pdfFile, StandardCharsets.ISO_8859_1);
      assertThat(pdf).startsWith("%PDF-1.5");
      // One page, and tagged by the preset.
      assertThat(pdf.split("/Type\\s*/Page\\b(?!s)", -1)).hasSize(2);
      assertThat(pdf).contains("/StructTreeRoot");
    }

    @Test
    void withOutputFormat_ShouldSucceed(final @TempDir File testFolder) throws Exception {

      final var inputFile = new File(SOURCE_FILE);
      FileUtils.copyFileToDirectory(inputFile, testFolder);
      final var inputFileTmp =
          new File(testFolder, Objects.requireNonNull(FileUtils.getName(SOURCE_FILE)));
      final var outputFile =
          new File(testFolder, FileUtils.getBaseName(inputFile.getName()) + ".pdf");

      final var status =
          Convert.run(
              "-f", "pdf", "-x", ExistingProcessAction.KILL.toString(), inputFileTmp.getPath());
      assertThat(status).isEqualTo(0);
      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withMultipleFilters_ShouldSucceed(final @TempDir File testFolder) {

      final var filterChainFile = new File(CONFIG_DIR + "applicationContext_multipleFilters.xml");
      final var inputFile = new File(SOURCE_FILE);
      final var outputFile = new File(testFolder, "convert_WithMultipleFilters.pdf");

      final var status =
          Convert.run(
              "-a",
              filterChainFile.getPath(),
              "-x",
              ExistingProcessAction.KILL.toString(),
              inputFile.getPath(),
              outputFile.getPath());
      assertThat(status).isEqualTo(0);
      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withContextWithoutFilterChain_ShouldSucceed(final @TempDir File testFolder)
        throws Exception {

      // A context that only defines an SslConfig, of no use for a local conversion.
      final var contextFile = new File(CONFIG_DIR + "applicationContext_sslConfig.xml");
      final var inputFile = new File(SOURCE_MULTI_FILE);
      final var outputFile = new File(testFolder, "convert_WithContextWithoutFilterChain.txt");

      final var status =
          Convert.run(
              "-a",
              contextFile.getPath(),
              "-x",
              ExistingProcessAction.KILL.toString(),
              inputFile.getPath(),
              outputFile.getPath());

      assertThat(status).isEqualTo(0);
      assertThat(outputFile).isFile();
    }

    @Test
    void withSingleFilter_ShouldSucceed(final @TempDir File testFolder) throws Exception {

      final var filterChainFile =
          new File(CONFIG_DIR + "applicationContext_pagesSelectorFilter.xml");
      final var inputFile = new File(SOURCE_MULTI_FILE);
      final var outputFile = new File(testFolder, "convert_WithSingleFilter.txt");

      final var status =
          Convert.run(
              "-a",
              filterChainFile.getPath(),
              "-x",
              ExistingProcessAction.KILL.toString(),
              inputFile.getPath(),
              outputFile.getPath());
      assertThat(status).isEqualTo(0);
      final var content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
      assertThat(content)
          .as("Check content: %s", content)
          .contains("Test document Page 2")
          .doesNotContain("Test document Page 1")
          .doesNotContain("Test document Page 3");
    }

    @Test
    void withCustomStoreProperties_ShouldSucceed(final @TempDir File testFolder) {

      final var inputFile = new File(SOURCE_MULTI_FILE);
      final var outputFile = new File(testFolder, "convert_WithCustomStoreProperties.pdf");

      final var status =
          Convert.run(
              "-sFDPageRange=2-2",
              "-x",
              ExistingProcessAction.KILL.toString(),
              inputFile.getPath(),
              outputFile.getPath());
      assertThat(status).isEqualTo(0);

      // If the document (with the image) is fully converted, it will
      // be much greater that 30K (over 70K). Only the second page
      // doesn't have an image.
      assertThat(outputFile.length()).isLessThan(30_000L);
    }
  }

  @Nested
  class MainTests {

    @Test
    void withAllCustomizableOption_ShouldExecuteAndExitWithCode0() {

      final var status =
          Convert.run(
              "-i",
              LocalOfficeUtils.getDefaultOfficeHome().getPath(),
              "-m",
              LocalOfficeUtils.findBestProcessManager().getClass().getName(),
              "-t",
              "30000",
              "-p",
              "2002",
              "-u",
              new File("src/integTest/resources/templateProfileDir").getPath(),
              "-x",
              ExistingProcessAction.KILL.toString(),
              "input1.txt",
              "output1.pdf");
      assertThat(status).isEqualTo(0);
    }
  }
}
