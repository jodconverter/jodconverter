/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;

/**
 * Contains tests for the {@link HeaderFooterRemoverFilter} and {@link PageOrientationFilter}
 * classes, with a real office.
 */
@ExtendWith(LocalOfficeManagerExtension.class)
class PageStylesFiltersITest {

  // A single cell, "Test spreadsheet". The office names the sheet after the file. The order of
  // the texts in the PDF is not the order of the page, so the tests do not rely on it.
  private static final File CSV = documentFile("test.csv");
  private static final String CSV_CONTENT = "Test spreadsheet";
  private static final String CSV_HEADER = "test";
  private static final String CSV_FOOTER = "Page 1";

  private static final File DOC = documentFile("test.doc");
  private static final File XLS = documentFile("test.xls");
  private static final File ODP = documentFile("test.odp");

  private static File convert(
      final OfficeManager manager, final File source, final File target, final Filter... filters)
      throws OfficeException {

    final var builder = LocalConverter.builder().officeManager(manager);
    if (filters.length > 0) {
      builder.filterChain(filters);
    }
    builder.build().convert(source).to(target).execute();
    return target;
  }

  private static String text(final File pdf) throws IOException {
    try (var doc = Loader.loadPDF(pdf)) {
      return new PDFTextStripper().getText(doc).strip();
    }
  }

  private static boolean isLandscape(final File pdf) throws IOException {
    try (var doc = Loader.loadPDF(pdf)) {
      final var box = doc.getPage(0).getMediaBox();
      return box.getWidth() > box.getHeight();
    }
  }

  @Nested
  class HeaderFooterRemover {

    @Test
    void withoutFilter_ShouldPrintTheSheetNameAndThePageNumber(
        final @TempDir File testFolder, final OfficeManager manager) throws Exception {

      final var pdf = convert(manager, CSV, new File(testFolder, "default.pdf"));

      assertThat(text(pdf).lines()).containsExactlyInAnyOrder(CSV_HEADER, CSV_CONTENT, CSV_FOOTER);
    }

    @Test
    void withFilter_ShouldPrintTheCellsOnly(
        final @TempDir File testFolder, final OfficeManager manager) throws Exception {

      final var pdf =
          convert(manager, CSV, new File(testFolder, "bare.pdf"), new HeaderFooterRemoverFilter());

      assertThat(text(pdf)).isEqualTo(CSV_CONTENT);
    }

    @Test
    void withHeaderOnly_ShouldKeepThePageNumber(
        final @TempDir File testFolder, final OfficeManager manager) throws Exception {

      final var pdf =
          convert(
              manager,
              CSV,
              new File(testFolder, "footer.pdf"),
              new HeaderFooterRemoverFilter(true, false));

      assertThat(text(pdf).lines()).containsExactlyInAnyOrder(CSV_CONTENT, CSV_FOOTER);
    }

    @Test
    void withFooterOnly_ShouldKeepTheSheetName(
        final @TempDir File testFolder, final OfficeManager manager) throws Exception {

      final var pdf =
          convert(
              manager,
              CSV,
              new File(testFolder, "header.pdf"),
              new HeaderFooterRemoverFilter(false, true));

      assertThat(text(pdf).lines()).containsExactlyInAnyOrder(CSV_HEADER, CSV_CONTENT);
    }

    @Test
    void withTextDocument_ShouldConvert(final @TempDir File testFolder, final OfficeManager manager)
        throws Exception {

      final var pdf =
          convert(manager, DOC, new File(testFolder, "doc.pdf"), new HeaderFooterRemoverFilter());

      assertThat(text(pdf)).isNotEmpty();
    }
  }

  @Nested
  class PageOrientation {

    @Test
    void withoutFilter_ShouldBePortrait(final @TempDir File testFolder, final OfficeManager manager)
        throws Exception {

      assertThat(isLandscape(convert(manager, DOC, new File(testFolder, "doc.pdf")))).isFalse();
      assertThat(isLandscape(convert(manager, XLS, new File(testFolder, "xls.pdf")))).isFalse();
    }

    @Test
    void withTextDocument_ShouldBeLandscape(
        final @TempDir File testFolder, final OfficeManager manager) throws Exception {

      final var pdf =
          convert(manager, DOC, new File(testFolder, "doc.pdf"), new PageOrientationFilter());

      assertThat(isLandscape(pdf)).isTrue();
    }

    @Test
    void withSpreadsheet_ShouldBeLandscape(
        final @TempDir File testFolder, final OfficeManager manager) throws Exception {

      final var pdf =
          convert(manager, XLS, new File(testFolder, "xls.pdf"), new PageOrientationFilter());

      assertThat(isLandscape(pdf)).isTrue();
    }

    @Test
    void withLandscapeThenPortrait_ShouldBePortrait(
        final @TempDir File testFolder, final OfficeManager manager) throws Exception {

      final var pdf =
          convert(
              manager,
              DOC,
              new File(testFolder, "doc.pdf"),
              new PageOrientationFilter(true),
              new PageOrientationFilter(false));

      assertThat(isLandscape(pdf)).isFalse();
    }

    @Test
    void withPresentation_ShouldLeaveTheSlidesAsTheyAre(
        final @TempDir File testFolder, final OfficeManager manager) throws Exception {

      final var pdf =
          convert(manager, ODP, new File(testFolder, "odp.pdf"), new PageOrientationFilter(false));

      assertThat(isLandscape(pdf)).isTrue();
    }
  }
}
