/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2019 Simon Braconnier and contributors
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

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.LocalConverter;
import org.jodconverter.filter.text.GraphicInserterFilter;
import org.jodconverter.filter.text.TextReplacerFilter;
import org.jodconverter.office.OfficeException;

public class MultipleFiltersITest extends AbstractOfficeITest {

  private static final String SOURCE_FILENAME = "test_replace.doc";
  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR, SOURCE_FILENAME);
  private static final File IMAGE_FILE = new File(RESOURCES_DIR, "images/sample-1.jpg");

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

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
    LocalConverter.builder()
        .filterChain(replacerFilter, graphicfilter)
        .build()
        .convert(SOURCE_FILE)
        .to(new File(testFolder.getRoot(), SOURCE_FILENAME + ".pdf"))
        .execute();
  }
}
