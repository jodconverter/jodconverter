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

package org.jodconverter.boot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.core.pdf.PdfVersion;

/**
 * Contains tests for the {@code jodconverter.pdf} properties applied by the auto-configured local
 * converter, with a real office installation.
 */
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@TestPropertySource(
    properties = {
      "jodconverter.local.enabled=true",
      "jodconverter.local.port-numbers=2012",
      "jodconverter.local.existing-process-action=kill",
      "jodconverter.pdf.version=pdf-1-5",
      "jodconverter.pdf.tagged=true"
    })
class PdfOptionsLocalConverterITest {

  private static final File SOURCE_FILE = new File("src/integTest/resources/documents/test1.doc");

  @Autowired private DocumentConverter converter;

  private static String read(final File file) throws IOException {
    return Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
  }

  @Test
  void convertToPdf_ShouldApplyThePdfProperties(final @TempDir File testFolder)
      throws IOException, OfficeException {

    final File outputFile = new File(testFolder, "out.pdf");

    converter.convert(SOURCE_FILE).to(outputFile).execute();

    assertThat(read(outputFile)).startsWith("%PDF-1.5").contains("/StructTreeRoot");
  }

  @Test
  void convertToPdfWithOptions_ShouldUseTheOptionsOfTheConversion(final @TempDir File testFolder)
      throws IOException, OfficeException {

    final File outputFile = new File(testFolder, "out.pdf");

    converter
        .convert(SOURCE_FILE)
        .to(outputFile)
        .with(PdfOptions.builder().version(PdfVersion.PDF_1_6).tagged(false).build())
        .execute();

    assertThat(read(outputFile)).startsWith("%PDF-1.6").doesNotContain("/StructTreeRoot");
  }

  @Test
  void convertToAnotherFormat_ShouldNotApplyThePdfProperties(final @TempDir File testFolder)
      throws OfficeException {

    final File outputFile = new File(testFolder, "out.odt");

    converter.convert(SOURCE_FILE).to(outputFile).execute();

    assertThat(outputFile).isFile();
    assertThat(outputFile.length()).isGreaterThan(0L);
  }
}
