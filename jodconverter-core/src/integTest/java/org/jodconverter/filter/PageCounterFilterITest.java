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
import org.jodconverter.filter.text.PageCounterFilter;
import org.jodconverter.filter.text.PageSelectorFilter;

public class PageCounterFilterITest extends BaseOfficeITest {

  private static final String SOURCE_FILE = DOCUMENTS_DIR + "test_multi_page.doc";
  private static final String OUTPUT_DIR =
      TEST_OUTPUT_DIR + PageCounterFilterITest.class.getSimpleName() + "/";

  /** Ensures we start with a fresh output directory. */
  @BeforeClass
  public static void createOutputDir() {

    // Ensure we start with a fresh output directory
    final File outputDir = new File(OUTPUT_DIR);
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
   * Test the conversion of a document replacing text along the way.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_SelectPage2BetweenCounter_ShouldCount3Then1() throws Exception {

    final File sourceFile = new File(SOURCE_FILE);
    final File outputFile = new File(OUTPUT_DIR, "page2.txt");

    final PageCounterFilter pageCounterFilter1 = new PageCounterFilter();
    final PageSelectorFilter pageSelectorFilter = new PageSelectorFilter((short) 2);
    final PageCounterFilter pageCounterFilter2 = new PageCounterFilter();

    // Test the filter
    converter
        .convert(sourceFile)
        .filterWith(
            pageCounterFilter1, pageSelectorFilter, pageCounterFilter2, RefreshFilter.REFRESH)
        .to(outputFile)
        .execute();

    String content = FileUtils.readFileToString(outputFile, Charset.forName("UTF-8"));
    assertThat(content)
        .contains("Test document Page 2")
        .doesNotContain("Test document Page 1")
        .doesNotContain("Test document Page 3");
    assertThat(pageCounterFilter1.getPageCount()).isEqualTo((short) 3);
    assertThat(pageCounterFilter2.getPageCount()).isEqualTo((short) 1);
  }
}
