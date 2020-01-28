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

package org.jodconverter.boot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Objects;

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
import org.jodconverter.core.office.OfficeException;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@TestPropertySource(locations = "classpath:config/application-local.properties")
public class LocalConverterITest {

  @TempDir File testFolder;
  private File inputFileTxt;

  @Autowired private DocumentConverter converter;

  @BeforeEach
  public void setUp() throws IOException {

    inputFileTxt = new File(testFolder, "inputFile.txt");
    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(inputFileTxt.toPath()))) {
      writer.println("This is the first line of the input file.");
      writer.println("This is the second line of the input file.");
    }
  }

  @Test
  public void testTxtToRtf() throws OfficeException {

    final File outputFile = new File(testFolder, "outputFile.rtf");
    converter.convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  public void testTxtToDoc() throws OfficeException {

    final File outputFile = new File(testFolder, "outputFile.doc");
    converter.convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  public void testTxtToPdf() throws OfficeException {

    final File outputFile = new File(testFolder, "outputFile.pdf");
    converter.convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  public void testDocToHtml() throws OfficeException {

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

  @Test
  public void testDocToXhtml() throws OfficeException {
    final File outputDir = new File(testFolder, "xhtml");
    outputDir.mkdirs();

    final File outputFile = new File(outputDir, "outputFile.xhtml");
    final File inputFile = new File("src/integTest/resources/documents/test1.doc");
    converter.convert(inputFile).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    // Check that the EmbedImages option has been applied
    assertThat(Objects.requireNonNull(outputDir.list()).length)
        .as("Check %s file EmbedImages", outputFile.getName())
        .isEqualTo(1);
  }

  /** Test custom properties. */
  @Test
  public void testCustomProperties() {

    assertThat(
            converter
                .getFormatRegistry()
                .getFormatByExtension("txt")
                .getLoadProperties()
                .get("FilterOptions"))
        .isEqualTo("utf16");

    assertThat(
            converter
                .getFormatRegistry()
                .getFormatByExtension("txt")
                .getStoreProperties()
                .get(DocumentFamily.TEXT)
                .get("FilterOptions"))
        .isEqualTo("utf16");
  }
}
