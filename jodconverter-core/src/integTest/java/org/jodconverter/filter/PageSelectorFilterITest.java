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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.jodconverter.BaseOfficeITest;
import org.jodconverter.filter.text.PageSelectorFilter;

public class PageSelectorFilterITest extends BaseOfficeITest {

  private static final String SOURCE_FILENAME = "test_multi_page.doc";
  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR, SOURCE_FILENAME);

  private static File outputDir;

  /** Creates an output test directory just once. */
  @BeforeClass
  public static void setUpClass() {

    outputDir = new File(TEST_OUTPUT_DIR, PageSelectorFilterITest.class.getSimpleName());
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
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_SelectPage2_ShouldConvertOnlyPage2() throws Exception {

    final File targetFile = new File(outputDir, SOURCE_FILENAME + ".page2.txt");

    // Create the PageSelectorFilter to test.
    final PageSelectorFilter pageSelectorFilter = new PageSelectorFilter((short) 2);

    // Test the filter
    converter
        .convert(SOURCE_FILE)
        .filterWith(pageSelectorFilter, RefreshFilter.REFRESH)
        .to(targetFile)
        .execute();

    final String content = FileUtils.readFileToString(targetFile, Charset.forName("UTF-8"));
    assertThat(content)
        .contains("Test document Page 2")
        .doesNotContain("Test document Page 1")
        .doesNotContain("Test document Page 3");
  }
}
