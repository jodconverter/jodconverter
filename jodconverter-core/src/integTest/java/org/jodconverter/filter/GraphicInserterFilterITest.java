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

package org.jodconverter.filter;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.jodconverter.BaseOfficeITest;
import org.jodconverter.filter.text.GraphicInserterFilter;

public class GraphicInserterFilterITest extends BaseOfficeITest {

  private static final String SOURCE_FILE = DOCUMENTS_DIR + "test.doc";
  private static final String SOURCE_MULTI_PAGE_FILE = DOCUMENTS_DIR + "test_multi_page.doc";
  private static final String IMAGE_FILE = RESOURCES_DIR + "images/sample-1.jpg";
  private static final String OUTPUT_DIR =
      TEST_OUTPUT_DIR + GraphicInserterFilterITest.class.getSimpleName() + "/";

  /** Ensures we start with a fresh output directory. */
  @BeforeClass
  public static void createOutputDir() {

    // Ensure we start with a fresh output directory
    File outputDir = new File(OUTPUT_DIR);
    FileUtils.deleteQuietly(outputDir);
    outputDir.mkdirs();
  }

  /**  Deletes the output directory. */
  @AfterClass
  public static void deleteOutputDir() {

    // Delete the output directory
    FileUtils.deleteQuietly(new File(OUTPUT_DIR));
  }

  /**
   * Test the conversion of a document inserting a graphic along the way.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_WithCustomizedProperties() throws Exception {

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
    convertFileToAllSupportedFormats(
        sourceFile, testOutputDir, "test.onsecondpage", filter, RefreshFilter.INSTANCE);
  }

  /**
   * Test the conversion of a document inserting a graphic along the way.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_WithDefaultProperties() throws Exception {

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
    convertFileToAllSupportedFormats(
        sourceFile, testOutputDir, "test.originalsize", filter, RefreshFilter.INSTANCE);
  }

  /**
   * Test the conversion of a document inserting a graphic along the way. The image will be resize
   * (smaller).
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_WithDefaultPropertiesAndSmallerSize() throws Exception {

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
    convertFileToAllSupportedFormats(
        sourceFile, testOutputDir, "test.smallersize", filter, RefreshFilter.INSTANCE);
  }
}
