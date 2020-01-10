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

package org.jodconverter.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.LocalConverter;

public class PagesSelectorFilterITest extends AbstractOfficeITest {

  private static final String TEXT_FILENAME = "test_multi_page.doc";
  private static final File TEXT_FILE = new File(DOCUMENTS_DIR, TEXT_FILENAME);
  private static final String DRAW_FILENAME = "test_multi_page.odg";
  private static final File DRAW_FILE = new File(DOCUMENTS_DIR, DRAW_FILENAME);

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  /**
   * Test the conversion of a text document, choosing a specific page.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_TextSelectPage2_ShouldCount3Then1() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), TEXT_FILENAME + ".page2.pdf");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PagesSelectorFilter selector = new PagesSelectorFilter(2);
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    LocalConverter.builder()
        .filterChain(count1, selector, count2)
        .build()
        .convert(TEXT_FILE)
        .to(targetFile)
        .execute();

    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(1);
  }

  /**
   * Test the conversion of a text document, choosing the first and last page.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_TextSelectPage1And3_ShouldCount3Then2() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), TEXT_FILENAME + ".page1And3.pdf");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PagesSelectorFilter selector = new PagesSelectorFilter(1, 3);
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    LocalConverter.builder()
        .filterChain(count1, selector, count2)
        .build()
        .convert(TEXT_FILE)
        .to(targetFile)
        .execute();

    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(2);
  }

  /**
   * Test the conversion of a text document, choosing the last two pages.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_TextSelectPage2And3_ShouldCount3Then2() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), TEXT_FILENAME + ".page2And3.docx");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PagesSelectorFilter selector = new PagesSelectorFilter(2, 3);
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    LocalConverter.builder()
        .filterChain(count1, selector, count2)
        .build()
        .convert(TEXT_FILE)
        .to(targetFile)
        .execute();

    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(2);
  }

  /**
   * Test the conversion of a draw document, choosing a specific page.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_DrawSelectPage2_ShouldCount3Then1() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), DRAW_FILENAME + ".page2.pdf");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PagesSelectorFilter selector = new PagesSelectorFilter(2);
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    LocalConverter.builder()
        .filterChain(count1, selector, count2)
        .build()
        .convert(DRAW_FILE)
        .to(targetFile)
        .execute();

    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(1);
  }

  /**
   * Test the conversion of a draw document, choosing the first and last page.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_DrawSelectPage1And3_ShouldCount3Then2() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), DRAW_FILENAME + ".page1And3.pdf");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PagesSelectorFilter selector = new PagesSelectorFilter(1, 3);
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    LocalConverter.builder()
        .filterChain(count1, selector, count2)
        .build()
        .convert(DRAW_FILE)
        .to(targetFile)
        .execute();

    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(2);
  }

  /**
   * Test the conversion of a draw document, choosing the last 2 pages.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_DrawSelectPage2And3_ShouldCount3Then2() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), DRAW_FILENAME + ".page2And3.pdf");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PagesSelectorFilter selector = new PagesSelectorFilter(1, 3);
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    LocalConverter.builder()
        .filterChain(count1, selector, count2)
        .build()
        .convert(DRAW_FILE)
        .to(targetFile)
        .execute();

    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(2);
  }
}
