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

package org.jodconverter.boot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.jodconverter.DocumentConverter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JodConverterITest {

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  private static File inputFileTxt;

  @Autowired private DocumentConverter converter;

  /**
   * Creates an input file to convert and an output test directory just once.
   *
   * @throws IOException if an IO error occurs.
   */
  @BeforeClass
  public static void setUpClass() throws IOException {

    inputFileTxt = testFolder.newFile("inputFile.txt");
    try (final PrintWriter writer = new PrintWriter(new FileWriter(inputFileTxt))) {
      writer.println("This is the first line of the input file.");
      writer.println("This is the second line of the input file.");
    }
  }

  /** Deletes the output test directory once the tests are all done. */
  @AfterClass
  public static void tearDownClass() {

    FileUtils.deleteQuietly(testFolder.getRoot());
  }

  @Test
  public void testTxtToRtf() throws Exception {

    final File outputFile = new File(testFolder.getRoot(), "outputFile.rtf");
    converter.convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  public void testTxtToDoc() throws Exception {

    final File outputFile = new File(testFolder.getRoot(), "outputFile.doc");
    converter.convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  public void testTxtToDocx() throws Exception {

    final File outputFile = new File(testFolder.getRoot(), "outputFile.docx");
    converter.convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  public void testTxtToPdf() throws Exception {

    final File outputFile = new File(testFolder.getRoot(), "outputFile.pdf");
    converter.convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }
}
