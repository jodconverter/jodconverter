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

package org.artofsolving.jodconverter.cli;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class ConvertTest {

  private static final String CONFIG_DIR = "src/integTest/resources/config/";
  private static final String SOURCE_FILE = "src/test/resources/documents/test.doc";
  private static final String OUTPUT_DIR = "test-output/" + ConvertTest.class.getSimpleName();

  @Test
  public void convert() throws Exception {

    final File inputFile = new File(SOURCE_FILE);
    final File outputFile = new File(OUTPUT_DIR, "convert.pdf");

    Convert.main(new String[] {inputFile.getPath(), outputFile.getPath()});

    assertTrue(outputFile.isFile() && outputFile.length() > 0);
  }

  @Test
  public void convertWithFilterChain() throws Exception {

    final File filterChainFile = new File(CONFIG_DIR + "filterchain.xml");
    final File inputFile = new File(SOURCE_FILE);
    final File outputFile = new File(OUTPUT_DIR, "convertWithFilterChain.pdf");

    Convert.main(
        new String[] {"-f", filterChainFile.getPath(), inputFile.getPath(), outputFile.getPath()});

    assertTrue(outputFile.isFile() && outputFile.length() > 0);
  }
}
