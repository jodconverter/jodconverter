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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringControllerITest {

  @Configuration("SpringControllerTestConfiguration")
  static class ContextConfiguration {

    // this bean will be injected into the SpringControllerTest class
    @Bean
    public JodConverterBean springJoDConverter() {
      // We could set properties here.
      return new JodConverterBean();
    }
  }

  private File inputFileTxt;
  private File outputFileRtf;
  private File outputFileDoc;
  private File outputFilePdf;
  private File outputFileDocx;

  @Autowired private JodConverterBean bean;

  /** Method called before each test method annotated with the @Test annotation. */
  @Before
  public void setUp() throws Exception {

    inputFileTxt = File.createTempFile("JodConverterTest", ".txt");
    PrintWriter pw = null;
    try {
      pw = new PrintWriter(inputFileTxt);
      pw.println("This is the first line of the input file.");
      pw.println("This is the second line of the input file.");
    } catch (Exception e) {
      IOUtils.closeQuietly(pw);
    }

    final File parent = inputFileTxt.getParentFile();
    final String basename = FilenameUtils.getBaseName(inputFileTxt.getName());
    outputFileRtf = new File(parent, basename + ".rtf");
    outputFileDoc = new File(parent, basename + ".doc");
    outputFilePdf = new File(parent, basename + ".pdf");
    outputFileDocx = new File(parent, basename + ".docx");
  }

  /** Method called after each test method annotated with the @Test annotation. */
  @After
  public void tearDown() throws Exception {

    FileUtils.deleteQuietly(inputFileTxt);
    FileUtils.deleteQuietly(outputFileRtf);
    FileUtils.deleteQuietly(outputFileDoc);
    FileUtils.deleteQuietly(outputFilePdf);
    FileUtils.deleteQuietly(outputFileDocx);
  }

  @Test
  public void testTxtToRtf() throws Exception {

    bean.getConverter().convert(inputFileTxt).to(outputFileRtf).execute();
    assertTrue("RTF File not created.", outputFileRtf.exists());
  }

  @Test
  public void testTxtToDoc() throws Exception {

    bean.getConverter().convert(inputFileTxt).to(outputFileDoc).execute();
    assertTrue("DOC File not created.", outputFileDoc.exists());
  }

  @Test
  public void testTxtToDocx() throws Exception {

    bean.getConverter().convert(inputFileTxt).to(outputFileDocx).execute();
    assertTrue("DOCX File not created.", outputFileDocx.exists());
  }

  @Test
  public void testTxtToPdf() throws Exception {

    bean.getConverter().convert(inputFileTxt).to(outputFilePdf).execute();
    assertTrue("PDF File not created.", outputFilePdf.exists());
  }
}
