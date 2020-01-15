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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.jodconverter.ResourceUtil.documentFile;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.LocalConverter;
import org.jodconverter.LocalOfficeManagerExtension;
import org.jodconverter.office.OfficeManager;

@ExtendWith(LocalOfficeManagerExtension.class)
public class PageCounterFilterITest {

  private static final String CALC_FILENAME = "test_multi_page.xls";
  private static final File CALC_FILE = documentFile(CALC_FILENAME);

  private static final String DRAW_FILENAME = "test_multi_page.odg";
  private static final File DRAW_FILE = documentFile(DRAW_FILENAME);

  private static final String IMPRESS_FILENAME = "test_multi_page.ppt";
  private static final File IMPRESS_FILE = documentFile(IMPRESS_FILENAME);

  private static final String TEXT_FILENAME = "test_multi_page.doc";
  private static final File TEXT_FILE = documentFile(TEXT_FILENAME);

  @Test
  public void Calc_doFilter_SelectPage2_ShouldCount3Then1(
      @TempDir File testFolder, OfficeManager manager) {

    final File targetFile = new File(testFolder, CALC_FILENAME + ".sheet2.xls");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(count1, new PagesSelectorFilter(2), count2)
                    .build()
                    .convert(CALC_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(1);
  }

  @Test
  public void Draw_doFilter_SelectPage2_ShouldCount3Then1(
      @TempDir File testFolder, OfficeManager manager) {

    final File targetFile = new File(testFolder, DRAW_FILENAME + ".page2.pdf");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(count1, new PagesSelectorFilter(2), count2)
                    .build()
                    .convert(DRAW_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(1);
  }

  @Test
  public void Impress_doFilter_SelectPage2_ShouldCount4Then1(
      @TempDir File testFolder, OfficeManager manager) {

    final File targetFile = new File(testFolder, IMPRESS_FILENAME + ".page2.pdf");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(count1, new PagesSelectorFilter(2), count2)
                    .build()
                    .convert(IMPRESS_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    assertThat(count1.getPageCount()).isEqualTo(4);
    assertThat(count2.getPageCount()).isEqualTo(1);
  }

  @Test
  public void Text_doFilter_SelectPage2_ShouldCount3Then1(
      @TempDir File testFolder, OfficeManager manager) {

    final File targetFile = new File(testFolder, TEXT_FILENAME + ".page2.pdf");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(count1, new PagesSelectorFilter(2), count2)
                    .build()
                    .convert(TEXT_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(1);
  }
}
