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

package org.jodconverter.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.jodconverter.office.OfficeUtils;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringControllerITest {

  @Configuration("SpringControllerTestConfiguration")
  static class ContextConfiguration {

    // this bean will be injected into the SpringControllerTest class
    @Bean
    public JodConverterBean springJODConverter() {

      JodConverterBean bean = new JodConverterBean();
      bean.setPortNumbers("2005");
      bean.setOfficeHome(OfficeUtils.getDefaultOfficeHome().getPath());
      bean.setWorkingDir(null);
      bean.setTemplateProfileDir(null);
      bean.setKillExistingProcess(true);
      bean.setProcessRetryInterval(1000L);
      bean.setProcessTimeout(60000L);
      bean.setMaxTasksPerProcess(20);
      bean.setTaskExecutionTimeout(60000L);
      bean.setTaskQueueTimeout(60000L);

      return bean;
    }
  }

  private static File inputFileTxt;
  private static File outputDir;

  /**
   * Creates an input file to convert and an output test directory just once.
   *
   * @throws IOException if an IO error occurs.
   */
  @BeforeClass
  public static void setUpClass() throws IOException {

    final File tempDir = new File(System.getProperty("java.io.tmpdir"));
    outputDir =
        new File(
            tempDir,
            "jodconverter_"
                + SpringControllerITest.class.getSimpleName()
                + "_"
                + UUID.randomUUID().toString());
    outputDir.mkdirs();

    inputFileTxt = new File(outputDir, "inputFile.txt");
    try (final PrintWriter pw = new PrintWriter(new FileWriter(inputFileTxt))) {
      pw.println("This is the first line of the input file.");
      pw.println("This is the second line of the input file.");
    }
  }

  /** Deletes the output test directory once the tests are all done. */
  @AfterClass
  public static void tearDownClass() {

    FileUtils.deleteQuietly(outputDir);
  }

  @Autowired private JodConverterBean bean;

  @Test
  public void testTxtToRtf() throws Exception {

    final File outputFile = new File(outputDir, "outputFile.rtf");
    bean.getConverter().convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  public void testTxtToDoc() throws Exception {

    final File outputFile = new File(outputDir, "outputFile.doc");
    bean.getConverter().convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  public void testTxtToDocx() throws Exception {

    final File outputFile = new File(outputDir, "outputFile.docx");
    bean.getConverter().convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  public void testTxtToPdf() throws Exception {

    final File outputFile = new File(outputDir, "outputFile.pdf");
    bean.getConverter().convert(inputFileTxt).to(outputFile).execute();

    assertThat(outputFile).as("Check %s file creation", outputFile.getName()).isFile();
    assertThat(outputFile.length())
        .as("Check %s file length", outputFile.getName())
        .isGreaterThan(0L);
  }

  @Test
  public void testLogAvailableFormats() throws Exception {

    bean.logAvailableFormats();
  }
}
