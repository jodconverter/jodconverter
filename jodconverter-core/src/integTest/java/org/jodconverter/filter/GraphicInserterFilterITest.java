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

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.filter.text.GraphicInserterFilter;
import org.jodconverter.office.OfficeException;

public class GraphicInserterFilterITest extends AbstractOfficeITest {

  private static final String SOURCE_FILENAME = "test.doc";
  private static final String SOURCE_MULTI_PAGE_FILENAME = "test_multi_page.doc";
  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR, SOURCE_FILENAME);
  private static final File SOURCE_MULTI_PAGE_FILE =
      new File(DOCUMENTS_DIR, SOURCE_MULTI_PAGE_FILENAME);
  private static final File IMAGE_FILE = new File(RESOURCES_DIR, "images/sample-1.jpg");

  private static File outputDir;

  /** Creates an output test directory just once. */
  @BeforeClass
  public static void setUpClass() {

    outputDir = new File(TEST_OUTPUT_DIR, GraphicInserterFilterITest.class.getSimpleName());
    outputDir.mkdirs();
  }

  /** Deletes the output test directory once the tests are all done. */
  @AfterClass
  public static void tearDownClass() {

    FileUtils.deleteQuietly(outputDir);
  }

  /**
   * Test the conversion of a document inserting a graphic along the way on the second page.
   *
   * @throws OfficeException If an error occurs.
   */
  @Test
  public void doFilter_WithCustomizedProperties() throws OfficeException {

    // Create the properties of the filter
    final Map<String, Object> props =
        GraphicInserterFilter.createDefaultShapeProperties(
            50, // Horizontal Position, 5 CM
            100 // Vertical Position, 10 CM
            );

    // Add a special property to add the image on the second page
    props.put("AnchorPageNo", (short) 2);

    // Create the GraphicInserterFilter to test.
    final GraphicInserterFilter filter = new GraphicInserterFilter(IMAGE_FILE.getPath(), props);

    // Convert to PDF
    converter
        .convert(SOURCE_MULTI_PAGE_FILE)
        .filterWith(filter)
        .to(new File(outputDir, SOURCE_MULTI_PAGE_FILENAME + ".pdf"))
        .execute();
  }

  /**
   * Test the conversion of a document inserting a graphic along the way.
   *
   * @throws OfficeException If an error occurs.
   */
  @Test
  public void doFilter_WithDefaultProperties() throws OfficeException {

    // Create the GraphicInserterFilter to test.
    final GraphicInserterFilter filter =
        new GraphicInserterFilter(
            IMAGE_FILE.getPath(),
            50, // Horizontal Position // 5 CM
            100); // Vertical Position // 10 CM

    // Convert to PDF
    converter
        .convert(SOURCE_FILE)
        .filterWith(filter)
        .to(new File(outputDir, SOURCE_FILENAME + ".originalsize.pdf"))
        .execute();
  }

  /**
   * Test the conversion of a document inserting a graphic along the way. The image will be resize
   * (smaller).
   *
   * @throws OfficeException If an error occurs.
   */
  @Test
  public void doFilter_WithDefaultPropertiesAndSmallerSize() throws OfficeException {

    // Create the GraphicInserterFilter to test.
    final GraphicInserterFilter filter =
        new GraphicInserterFilter(
            IMAGE_FILE.getPath(),
            74, // Image Width // 7.4 CM (half the original size)
            56, // Image Height // 5.6 CM (half the original size)
            30, // Horizontal Position // 3 CM
            50); // Vertical Position // 5 CM

    // Convert to PDF
    converter
        .convert(SOURCE_FILE)
        .filterWith(filter)
        .to(new File(outputDir, SOURCE_FILENAME + ".smallersize.pdf"))
        .execute();
  }

  /**
   * Test the conversion of a document inserting a graphic along the way on the second page. The
   * image will be resize (smaller).
   *
   * @throws OfficeException If an error occurs.
   */
  @Test
  public void doFilter_WithCustomizedPropertiesAndSmallerSize() throws OfficeException {

    // Create the properties of the filter
    final Map<String, Object> props =
        GraphicInserterFilter.createDefaultShapeProperties(
            50, // Horizontal Position, 5 CM
            100 // Vertical Position, 10 CM
            );

    // Add a special property to add the image on the second page
    props.put("AnchorPageNo", (short) 2);

    // Create the GraphicInserterFilter to test.
    final GraphicInserterFilter filter =
        new GraphicInserterFilter(
            IMAGE_FILE.getPath(),
            74, // Image Width // 7.4 CM (half the original size)
            56, // Image Height // 5.6 CM (half the original size)
            props);

    // Convert to PDF
    converter
        .convert(SOURCE_MULTI_PAGE_FILE)
        .filterWith(filter)
        .to(new File(outputDir, SOURCE_MULTI_PAGE_FILENAME + ".smallersize.pdf"))
        .execute();
  }
}
