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
import org.junit.BeforeClass;
import org.junit.Test;

import org.jodconverter.filter.text.TextReplacerFilter;
import org.jodconverter.office.OfficeException;

public class TextReplacerFilterTest extends FilterTest {

  private static final String SOURCE_FILE = DOCUMENTS_DIR + "test_replace.doc";
  private static final String OUTPUT_DIR =
      TEST_OUTPUT_DIR + TextReplacerFilterTest.class.getSimpleName();

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
    final File testOutputDir = new File(OUTPUT_DIR);

    // Create the GraphicInserterFilter to test.
    final TextReplacerFilter filter =
        new TextReplacerFilter(
            new String[] {"SEARCH_WORD", "that", "have", "new common language will be more simple"},
            new String[] {
              "REPLACEMENT_STRING",
              "REPLACEMENT_THAT",
              "REPLACEMENT_HAVE",
              "most recent common language will be more basic"
            });

    // Test the filter
    testFilters(sourceFile, testOutputDir, "test", filter, RefreshFilter.INSTANCE);
  }
}
