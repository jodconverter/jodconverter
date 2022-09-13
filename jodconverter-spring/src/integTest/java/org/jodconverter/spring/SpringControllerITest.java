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

package org.jodconverter.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.LocalConverter;

/** Contains tests for the {@link JodConverterBean} class. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@DirtiesContext(classMode = AFTER_CLASS)
class SpringControllerITest {

  /* default */ @TempDir File testFolder;
  private File inputFileTxt;

  @Autowired private JodConverterBean bean;

  @Configuration("SpringControllerTestConfiguration")
  /* default */ static class ContextConfiguration {

    // this bean will be injected into the SpringControllerTest class
    @Bean
    /* default */ JodConverterBean springJodConverter() {

      final JodConverterBean bean = new JodConverterBean();
      bean.setPortNumbers("2005");

      return bean;
    }
  }

  @BeforeEach
  void setUp() throws IOException {

    inputFileTxt = new File(testFolder, "inputFile.txt");
    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(inputFileTxt.toPath()))) {
      writer.println("This is the first line of the input file.");
      writer.println("This is the second line of the input file.");
    }
  }

  @Test
  void testOfficeManager() throws OfficeException {

    final File outputFile = new File(testFolder, "outputFile.txt");
    LocalConverter.builder()
        .officeManager(bean.getManager())
        .build()
        .convert(inputFileTxt)
        .to(outputFile)
        .execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  void testTxtToRtf() throws OfficeException {

    final File outputFile = new File(testFolder, "outputFile.rtf");
    bean.getConverter().convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  void testTxtToDoc() throws OfficeException {

    final File outputFile = new File(testFolder, "outputFile.doc");
    bean.getConverter().convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  void testTxtToPdf() throws OfficeException {

    final File outputFile = new File(testFolder, "outputFile.pdf");
    bean.getConverter().convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  void testLogAvailableFormats() {

    bean.logAvailableFormats();
  }
}
