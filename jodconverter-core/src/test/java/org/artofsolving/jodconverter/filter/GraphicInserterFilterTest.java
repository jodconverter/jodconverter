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

package org.artofsolving.jodconverter.filter;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import org.artofsolving.jodconverter.office.OfficeException;

public class GraphicInserterFilterTest extends FilterTest {

  private static final String SOURCE_FILE = "src/test/resources/documents/test.doc";
  private static final String SOURCE_MULTI_PAGE_FILE =
      "src/test/resources/documents/test_multi_page.doc";
  private static final String IMAGE_FILE = "src/test/resources/images/sample-1.jpg";
  private static final String OUTPUT_DIR =
      "test-output/" + GraphicInserterFilterTest.class.getSimpleName();

  /** Clears the output directory before the execution of the first test in this class. */
  @BeforeClass
  public static void deleteOutputDirectory() {

    final File testOutputDir = new File(OUTPUT_DIR);
    FileUtils.deleteQuietly(testOutputDir);
  }

  /**
   * Test the conversion of a document inserting a graphic along the way.
   *
   * @throws OfficeException if an error occurs.
   */
  @Test
  public void doFilter_WithCustomizedProperties() throws OfficeException {

    final File sourceFile = new File(SOURCE_MULTI_PAGE_FILE);
    final File sourceImage = new File(IMAGE_FILE);
    final File testOutputDir = new File(OUTPUT_DIR);

    // Create the properties of the filter
    final Map<String, Object> props =
        GraphicInserterFilter.createDefaultShapeProperties(
            50, // Horizontal Position, 5 CM
            100 // Vertical Position, 10 CM
            );

    // Add a special property to add the image on the second page
    props.put("AnchorPageNo", (short) 2);

    // Create the GraphicInserterFilter to test.
    final GraphicInserterFilter filter = new GraphicInserterFilter(sourceImage.getPath(), props);

    // Test the filter
    testFilters(sourceFile, testOutputDir, "test.onsecondpage", filter, RefreshFilter.INSTANCE);
  }

  /**
   * Test the conversion of a document inserting a graphic along the way.
   *
   * @throws OfficeException if an error occurs.
   */
  @Test
  public void doFilter_WithDefaultProperties() throws OfficeException {

    final File sourceFile = new File(SOURCE_FILE);
    final File sourceImage = new File(IMAGE_FILE);
    final File testOutputDir = new File(OUTPUT_DIR);

    // Create the GraphicInserterFilter to test.
    final GraphicInserterFilter filter =
        new GraphicInserterFilter(
            sourceImage.getPath(),
            50, // Horizontal Position // 5 CM
            100); // Vertical Position // 10 CM

    // Test the filter
    testFilters(sourceFile, testOutputDir, "test.originalsize", filter, RefreshFilter.INSTANCE);
  }

  /**
   * Test the conversion of a document inserting a graphic along the way. The image will be resize
   * (smaller).
   *
   * @throws OfficeException if an error occurs.
   */
  @Test
  public void doFilter_WithDefaultPropertiesAndSmallerSize() throws OfficeException {

    final File sourceFile = new File(SOURCE_FILE);
    final File sourceImage = new File(IMAGE_FILE);
    final File testOutputDir = new File(OUTPUT_DIR);

    // Create the GraphicInserterFilter to test.
    final GraphicInserterFilter filter =
        new GraphicInserterFilter(
            sourceImage.getPath(),
            74, // Image Width // 7.4 CM (half the original size)
            56, // Image Height // 5.6 CM (half the original size)
            30, // Horizontal Position // 3 CM
            50); // Vertical Position // 5 CM

    // Test the filter
    testFilters(sourceFile, testOutputDir, "test.smallersize", filter, RefreshFilter.INSTANCE);
  }
}
