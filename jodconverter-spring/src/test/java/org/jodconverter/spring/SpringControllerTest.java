/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
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
public class SpringControllerTest {

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

    bean.convert(inputFileTxt, outputFileRtf);
    assertTrue("RTF File not created.", outputFileRtf.exists());
  }

  @Test
  public void testTxtToDoc() throws Exception {

    bean.convert(inputFileTxt, outputFileDoc);
    assertTrue("DOC File not created.", outputFileDoc.exists());
  }

  @Test
  public void testTxtToDocx() throws Exception {

    bean.convert(inputFileTxt, outputFileDocx);
    assertTrue("DOCX File not created.", outputFileDocx.exists());
  }

  @Test
  public void testTxtToPdf() throws Exception {

    bean.convert(inputFileTxt, outputFilePdf);
    assertTrue("PDF File not created.", outputFilePdf.exists());
  }
}
