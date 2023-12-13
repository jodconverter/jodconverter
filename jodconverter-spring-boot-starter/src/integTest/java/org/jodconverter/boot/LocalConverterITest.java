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
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;

/** Contains tests for the {@link org.jodconverter.local.LocalConverter} class. */
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@TestPropertySource(locations = "classpath:config/application-local.properties")
class LocalConverterITest {

  /* default */ @TempDir File testFolder;
  private File inputFileTxt;

  @Autowired private DocumentConverter converter;

  @BeforeEach
  void setUp() throws IOException {

    inputFileTxt = new File(testFolder, "inputFile.txt");
    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(inputFileTxt.toPath()))) {
      writer.println("This is the first line of the input file.");
      writer.println("This is the second line of the input file.");
    }
  }

  @Test
  void testTxtToRtf() throws OfficeException {

    final File outputFile = new File(testFolder, "outputFile.rtf");
    converter.convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  void testTxtToDoc() throws OfficeException {

    final File outputFile = new File(testFolder, "outputFile.doc");
    converter.convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  void testTxtToPdf() throws OfficeException {

    final File outputFile = new File(testFolder, "outputFile.pdf");
    converter.convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void testDocToHtml() throws OfficeException {

    final File outputDir = new File(testFolder, "html");
    outputDir.mkdirs();
    final File outputFile = new File(outputDir, "outputFile.html");
    final File inputFile = new File("src/integTest/resources/documents/test1.doc");

    converter.convert(inputFile).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    // Check that the EmbedImages option has been applied
    assertThat(Objects.requireNonNull(outputDir.list()).length)
        .as("Check %s file EmbedImages", outputFile.getName())
        .isEqualTo(1);
  }

  // The following test fails on Apache Open Office.
  //  @Test
  //  @SuppressWarnings("ResultOfMethodCallIgnored")
  //  void testDocToXhtml() throws OfficeException {
  //    final File outputDir = new File(testFolder, "xhtml");
  //    outputDir.mkdirs();
  //
  //    final File outputFile = new File(outputDir, "outputFile.xhtml");
  //    final File inputFile = new File("src/integTest/resources/documents/test1.doc");
  //    converter.convert(inputFile).to(outputFile).execute();
  //
  //    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
  //    // Check that the EmbedImages option has been applied
  //    assertThat(Objects.requireNonNull(outputDir.list()).length)
  //        .as("Check %s file EmbedImages", outputFile.getName())
  //        .isEqualTo(1);
  //  }

  /** Test custom properties. */
  @Test
  void testCustomProperties() {

    final DocumentFormat format = converter.getFormatRegistry().getFormatByExtension("txt");
    assertThat(format).isNotNull();

    final Map<String, Object> loadProperties = format.getLoadProperties();
    assertThat(loadProperties).isNotNull();
    assertThat(loadProperties).containsEntry("FilterOptions", "utf16");

    final Map<String, Object> storeProperties = format.getStoreProperties(DocumentFamily.TEXT);
    assertThat(storeProperties).isNotNull();
    assertThat(storeProperties).containsEntry("FilterOptions", "utf16");
  }

  /** Test custom registry. */
  @Test
  void testCustomRegistry() {

    final DocumentFormat format = converter.getFormatRegistry().getFormatByExtension("html");
    assertThat(format).isNotNull();

    final Map<String, Object> properties = format.getStoreProperties(DocumentFamily.PRESENTATION);
    assertThat(properties).isNotNull();

    final Object filterData = properties.get("FilterData");
    assertThat(filterData).isNotNull();
    assertThat(filterData).asInstanceOf(InstanceOfAssertFactories.MAP).containsKey("PublishMode");
  }
}
