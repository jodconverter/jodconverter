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

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.filter.text.GraphicInserterFilter;
import org.jodconverter.filter.text.TextReplacerFilter;
import org.jodconverter.office.OfficeException;

public class MultipleFiltersITest extends AbstractOfficeITest {

  private static final String SOURCE_FILENAME = "test_replace.doc";
  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR, SOURCE_FILENAME);
  private static final File IMAGE_FILE = new File(RESOURCES_DIR, "images/sample-1.jpg");

  private static File outputDir;

  /** Creates an output test directory just once. */
  @BeforeClass
  public static void setUpClass() {

    outputDir = new File(TEST_OUTPUT_DIR, MultipleFiltersITest.class.getSimpleName());
    outputDir.mkdirs();
  }

  /** Deletes the output test directory once the tests are all done. */
  @AfterClass
  public static void tearDownClass() {

    FileUtils.deleteQuietly(outputDir);
  }

  /**
   * Test the conversion of a document replacing text along the way.
   *
   * @throws OfficeException If an office error occurs.
   */
  @Test
  public void doFilter_WithDefaultProperties() throws OfficeException {

    // Create the TextReplacerFilter to test.
    final TextReplacerFilter replacerFilter =
        new TextReplacerFilter(
            new String[] {"SEARCH_WORD", "that", "have", "new common language will be more simple"},
            new String[] {
              "REPLACEMENT_STRING",
              "REPLACEMENT_THAT",
              "REPLACEMENT_HAVE",
              "most recent common language will be more basic"
            });

    // Create the GraphicInserterFilter to test.
    final GraphicInserterFilter graphicfilter =
        new GraphicInserterFilter(
            IMAGE_FILE.getPath(),
            74, // Image Width // 7.4 CM (half the original size)
            56, // Image Height // 5.6 CM (half the original size)
            60, // Horizontal Position // 6 CM
            100); // Vertical Position // 10 CM

    // Convert to PDF
    converter
        .convert(SOURCE_FILE)
        .filterWith(replacerFilter, graphicfilter)
        .to(new File(outputDir, SOURCE_FILENAME + ".pdf"))
        .execute();
  }
}
