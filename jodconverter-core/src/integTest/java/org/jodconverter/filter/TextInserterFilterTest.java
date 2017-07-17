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
import org.junit.BeforeClass;
import org.junit.Test;

import org.jodconverter.filter.text.GraphicInserterFilter;
import org.jodconverter.filter.text.TextInserterFilter;
import org.jodconverter.office.OfficeException;

public class TextInserterFilterTest extends FilterTest {

  private static final String SOURCE_FILE = DOCUMENTS_DIR + "test.doc";
  private static final String SOURCE_MULTI_PAGE_FILE = DOCUMENTS_DIR + "test_multi_page.doc";
  private static final String OUTPUT_DIR =
      TEST_OUTPUT_DIR + TextInserterFilterTest.class.getSimpleName();

  /** Clears the output directory before the execution of the first test in this class. */
  @BeforeClass
  public static void deleteOutputDirectory() {

    final File testOutputDir = new File(OUTPUT_DIR);
    FileUtils.deleteQuietly(testOutputDir);
  }

  /**
   * Test the conversion of a document inserting text along the way.
   *
   * @throws OfficeException if an error occurs.
   */
  @Test
  public void doFilter_WithCustomizedProperties() throws OfficeException {

    final File sourceFile = new File(SOURCE_MULTI_PAGE_FILE);
    final File testOutputDir = new File(OUTPUT_DIR);

    // Create the properties of the filter
    final Map<String, Object> props =
        GraphicInserterFilter.createDefaultShapeProperties(
            50, // Horizontal Position, 5 CM
            100 // Vertical Position, 10 CM
            );

    // Add a special property to add the image on the second page
    props.put("AnchorPageNo", (short) 2);

    // Create the TextInserterFilter to test.
    final TextInserterFilter filter =
        new TextInserterFilter("This is a test of text insertion", 2, 10, props);

    // Test the filter
    testFilters(sourceFile, testOutputDir, "test.onsecondpage", filter, RefreshFilter.INSTANCE);
  }

  /**
   * Test the conversion of a document inserting text along the way.
   *
   * @throws OfficeException if an error occurs.
   */
  @Test
  public void doFilter_WithDefaultProperties() throws OfficeException {

    final File sourceFile = new File(SOURCE_FILE);
    final File testOutputDir = new File(OUTPUT_DIR);

    // Create the TextInserterFilter to test.
    final TextInserterFilter filter =
        new TextInserterFilter(
            "This is a test of text insertion",
            100, // Width, 10 CM
            20, // Height, 2 CM
            50, // Horizontal Position, 5 CM
            100); // Vertical Position , 10 CM

    // Test the filter
    testFilters(sourceFile, testOutputDir, "test.default", filter, RefreshFilter.INSTANCE);
  }
}
