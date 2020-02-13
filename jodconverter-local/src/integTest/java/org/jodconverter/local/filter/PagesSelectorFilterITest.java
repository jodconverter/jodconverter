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

package org.jodconverter.local.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.sun.star.lang.XComponent;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.job.SourceDocumentSpecsFromFile;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.task.AbstractLocalOfficeTask;

/** Contains tests for the {@link PagesSelectorFilter} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
public class PagesSelectorFilterITest {

  private static final String CALC_FILENAME = "test_multi_page.xls";
  private static final File CALC_FILE = documentFile(CALC_FILENAME);

  private static final String DRAW_FILENAME = "test_multi_page.odg";
  private static final File DRAW_FILE = documentFile(DRAW_FILENAME);

  private static final String IMPRESS_FILENAME = "test_multi_page.ppt";
  private static final File IMPRESS_FILE = documentFile(IMPRESS_FILENAME);

  private static final String TEXT_FILENAME = "test_multi_page.doc";
  private static final File TEXT_FILE = documentFile(TEXT_FILENAME);

  @Test
  public void Calc_doFilter_SelectPage2_ShouldOnlyHaveTextFromPage2(
      final @TempDir File testFolder, final OfficeManager manager) {

    final File targetFile = new File(testFolder, CALC_FILENAME + ".sheet2.pdf");

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

    // TODO: Validate text from resulting PDF if possible.
    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(1);
  }

  @Test
  public void Calc_doFilter_SelectPage1And3_ShouldOnlyHaveTextFromPage1And3(
      final @TempDir File testFolder, final OfficeManager manager) {

    final File targetFile = new File(testFolder, CALC_FILENAME + ".sheet1And3.xls");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(count1, new PagesSelectorFilter(1, 3), count2)
                    .build()
                    .convert(CALC_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    // TODO: Validate text from resulting PDF if possible.
    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(2);
  }

  @Test
  public void Draw_doFilter_SelectPage2_ShouldOnlyHaveTextFromPage2(
      final @TempDir File testFolder, final OfficeManager manager) {

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

    // TODO: Validate text from resulting PDF if possible.
    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(1);
  }

  @Test
  public void Draw_doFilter_SelectPage1And3_ShouldOnlyHaveTextFromPage1And3(
      final @TempDir File testFolder, final OfficeManager manager) {

    final File targetFile = new File(testFolder, DRAW_FILENAME + ".page1And3.pdf");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(count1, new PagesSelectorFilter(1, 3), count2)
                    .build()
                    .convert(DRAW_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    // TODO: Validate text from resulting PDF if possible.
    assertThat(count1.getPageCount()).isEqualTo(3);
    assertThat(count2.getPageCount()).isEqualTo(2);
  }

  @Test
  public void Impress_doFilter_SelectPage2_ShouldOnlyHaveTextFromPage2(
      final @TempDir File testFolder, final OfficeManager manager) {

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

    // TODO: Validate text from resulting PDF if possible.
    assertThat(count1.getPageCount()).isEqualTo(4);
    assertThat(count2.getPageCount()).isEqualTo(1);
  }

  @Test
  public void Impress_doFilter_SelectPage1And3_ShouldOnlyHaveTextFromPage1And3(
      final @TempDir File testFolder, final OfficeManager manager) {

    final File targetFile = new File(testFolder, IMPRESS_FILENAME + ".page1And3.pdf");

    final PageCounterFilter count1 = new PageCounterFilter();
    final PageCounterFilter count2 = new PageCounterFilter();

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(count1, new PagesSelectorFilter(1, 3), count2)
                    .build()
                    .convert(IMPRESS_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    // TODO: Validate text from resulting PDF if possible.
    assertThat(count1.getPageCount()).isEqualTo(4);
    assertThat(count2.getPageCount()).isEqualTo(2);
  }

  @Test
  public void Text_doFilter_SelectPage2_ShouldOnlyHaveTextFromPage2(
      final @TempDir File testFolder, final OfficeManager manager) throws IOException {

    final File targetFile = new File(testFolder, TEXT_FILENAME + ".page2.txt");

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(new PagesSelectorFilter(2))
                    .build()
                    .convert(TEXT_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    final String content = FileUtils.readFileToString(targetFile, StandardCharsets.UTF_8);
    assertThat(content)
        .as("Check content: %s", content)
        .doesNotContain("Test document Page 1")
        .contains("Test document Page 2")
        .doesNotContain("Test document Page 3");
  }

  @Test
  public void Text_doFilter_SelectPage1And3_ShouldOnlyHaveTextFromPage1And3(
      final @TempDir File testFolder, final OfficeManager manager) throws IOException {

    final File targetFile = new File(testFolder, TEXT_FILENAME + ".page1And3.txt");

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(new PagesSelectorFilter(1, 3))
                    .build()
                    .convert(TEXT_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    final String content = FileUtils.readFileToString(targetFile, StandardCharsets.UTF_8);
    assertThat(content)
        .as("Check content: %s", content)
        .contains("Test document Page 1")
        .doesNotContain("Test document Page 2")
        .contains("Test document Page 3");
  }

  @Test
  public void doFilter_WithPageOutOfRange_InexistingPagesIgnored(
      final @TempDir File testFolder, final OfficeManager manager) throws IOException {

    final File targetFile = new File(testFolder, TEXT_FILENAME + ".page2Only.txt");

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(new PagesSelectorFilter(0, 2, 4))
                    .build()
                    .convert(TEXT_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    final String content = FileUtils.readFileToString(targetFile, StandardCharsets.UTF_8);
    assertThat(content)
        .as("Check content: %s", content)
        .doesNotContain("Test document Page 1")
        .contains("Test document Page 2")
        .doesNotContain("Test document Page 3");
  }

  @Test
  public void doFilter_WithUnsupportedDocument_DoNothing(final LocalOfficeManager manager) {

    final AbstractLocalOfficeTask task =
        new AbstractLocalOfficeTask(new SourceDocumentSpecsFromFile(documentFile("db.odb")) {}) {
          @Override
          @SuppressWarnings("NullableProblems")
          public void execute(final OfficeContext context) throws OfficeException {

            final LocalOfficeContext localContext = (LocalOfficeContext) context;
            final XComponent document = loadDocument(localContext, source.getFile());
            final FilterChain chain = new DefaultFilterChain(new PagesSelectorFilter(2));
            chain.doFilter(context, document);
            closeDocument(document);
          }
        };

    assertThatCode(() -> manager.execute(task)).doesNotThrowAnyException();
  }
}
