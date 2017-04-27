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

package org.jodconverter.filter;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import org.jodconverter.filter.text.GraphicInserterFilter;
import org.jodconverter.filter.text.TextReplacerFilter;
import org.jodconverter.office.OfficeException;

public class MultipleFiltersTest extends FilterTest {

  private static final String SOURCE_FILE = DOCUMENTS_DIR + "test_replace.doc";
  private static final String IMAGE_FILE = RESOURCES_DIR + "images/sample-1.jpg";
  private static final String OUTPUT_DIR =
      TEST_OUTPUT_DIR + MultipleFiltersTest.class.getSimpleName();

  /** Clears the output directory before the execution of the first test in this class. */
  @BeforeClass
  public static void deleteOutputDirectory() {

    final File testOutputDir = new File(OUTPUT_DIR);
    FileUtils.deleteQuietly(testOutputDir);
  }

  /**
   * Test the conversion of a document replacing text along the way.
   *
   * @throws OfficeException if an error occurs.
   */
  @Test
  public void doFilter_WithDefaultProperties() throws OfficeException {

    final File sourceFile = new File(SOURCE_FILE);
    final File sourceImage = new File(IMAGE_FILE);
    final File testOutputDir = new File(OUTPUT_DIR);

    // Create the GraphicInserterFilter to test.
    final TextReplacerFilter testReplacerFilter =
        new TextReplacerFilter(
            new String[] {"SEARCH_WORD", "that", "have", "new common language will be more simple"},
            new String[] {
              "REPLACEMENT_STRING",
              "REPLACEMENT_THAT",
              "REPLACEMENT_HAVE",
              "most recent common language will be more basic"
            });

    // Create the GraphicInserterFilter to test.
    final GraphicInserterFilter graphicInserterfilter =
        new GraphicInserterFilter(
            sourceImage.getPath(),
            74, // Image Width // 7.4 CM (half the original size)
            56, // Image Height // 5.6 CM (half the original size)
            60, // Horizontal Position // 6 CM
            100); // Vertical Position // 10 CM

    // Test the filter
    testFilters(
        sourceFile,
        testOutputDir,
        "test.replaceTextThenAddGraphic",
        testReplacerFilter,
        graphicInserterfilter,
        RefreshFilter.INSTANCE);
  }
}
