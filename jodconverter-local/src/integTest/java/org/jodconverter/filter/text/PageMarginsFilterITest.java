/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

package org.jodconverter.filter.text;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.LocalConverter;
import org.jodconverter.office.OfficeException;

public class PageMarginsFilterITest extends AbstractOfficeITest {

  private static final String SOURCE_FILENAME = "test.doc";
  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR, SOURCE_FILENAME);

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  /**
   * Test the conversion of a document, settings specifics margins. We can't really test the result,
   * but at least we will test the the conversion doesn't fail.
   */
  @Test
  public void doFilter_WithMargins_ShouldConvertDocument() {

    final File targetFile = new File(testFolder.getRoot(), SOURCE_FILENAME + ".margins.pdf");

    // Test the filter
    try {
      LocalConverter.builder()
          .filterChain(new PageMarginsFilter(50, 50, 50, 50))
          .build()
          .convert(SOURCE_FILE)
          .to(targetFile)
          .execute();
    } catch (OfficeException e) {

      // The test should fail if an Exception occurs.
      fail();
    }
  }

  /**
   * Test the conversion of a document, settings margins to null (no change). We can't really test
   * the result, but at least we will test the the conversion doesn't fail.
   */
  @Test
  public void doFilter_WithNullMargins_ShouldConvertDocument() {

    final File targetFile = new File(testFolder.getRoot(), SOURCE_FILENAME + ".nullmargins.pdf");

    // Test the filter
    try {
      LocalConverter.builder()
          .filterChain(new PageMarginsFilter(null, null, null, null))
          .build()
          .convert(SOURCE_FILE)
          .to(targetFile)
          .execute();
    } catch (OfficeException e) {

      // The test should fail if an Exception occurs.
      fail();
    }
  }
}
