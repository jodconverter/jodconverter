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

package org.jodconverter.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.local.filter.text.DocumentInserterFilter;
import org.jodconverter.local.filter.text.TextReplacerFilter;

/** Contains tests for {@link LocalConverter#merge(File, File...)}, with a real office. */
@ExtendWith(LocalOfficeManagerExtension.class)
class MergeITest {

  // One page each.
  private static final File DOC = documentFile("test.doc");
  private static final File DOCX = documentFile("test.docx");
  private static final File ODT = documentFile("test.odt");
  // Three pages.
  private static final File MULTI_PAGE = documentFile("test_multi_page.doc");

  private static int pageCount(final File pdf) throws IOException {
    try (var doc = Loader.loadPDF(pdf)) {
      return doc.getNumberOfPages();
    }
  }

  private static String text(final File pdf) throws IOException {
    try (var doc = Loader.loadPDF(pdf)) {
      return new PDFTextStripper().getText(doc);
    }
  }

  @Test
  void merge_ShouldAppendEachDocumentOnANewPage(
      final @TempDir File testFolder, final OfficeManager manager)
      throws OfficeException, IOException {

    final var pdf = new File(testFolder, "merged.pdf");

    LocalConverter.make(manager).merge(DOC, MULTI_PAGE, ODT).to(pdf).execute();

    // 1 + 3 + 1 pages: each document starts on a new page.
    assertThat(pageCount(pdf)).isEqualTo(5);
    assertThat(text(pdf))
        .contains("Test document Page 1")
        .contains("Test document Page 2")
        .contains("Test document Page 3");
  }

  @Test
  void mergeWithList_ShouldAcceptTheTargetFormatAndOptions(
      final @TempDir File testFolder, final OfficeManager manager)
      throws OfficeException, IOException {

    final var pdf = new File(testFolder, "merged.pdf");
    final var docx = new File(testFolder, "merged.docx");

    final var converter = LocalConverter.make(manager);
    converter.merge(List.of(DOCX, ODT)).to(pdf).with(PdfOptions.archive()).execute();
    converter.merge(List.of(DOCX, ODT)).to(docx).execute();

    assertThat(pageCount(pdf)).isEqualTo(2);
    assertThat(Files.readString(pdf.toPath(), StandardCharsets.ISO_8859_1)).contains("pdfaid");
    assertThat(docx).isFile();
    assertThat(docx.length()).isGreaterThan(0L);
  }

  @Test
  void mergeWithOneDocument_ShouldConvertIt(
      final @TempDir File testFolder, final OfficeManager manager)
      throws OfficeException, IOException {

    final var pdf = new File(testFolder, "single.pdf");

    LocalConverter.make(manager).merge(List.of(DOC)).to(pdf).execute();

    assertThat(pageCount(pdf)).isEqualTo(1);
  }

  @Test
  void mergeWithFilters_ShouldApplyThemToTheMergedDocument(
      final @TempDir File testFolder, final OfficeManager manager)
      throws OfficeException, IOException {

    final var txt = new File(testFolder, "merged.txt");

    // The replacement applies to the inserted document too: the filters of the converter run
    // after the insertions.
    LocalConverter.builder()
        .officeManager(manager)
        .filterChain(new TextReplacerFilter(new String[] {"Page"}, new String[] {"Sheet"}))
        .build()
        .merge(DOC, MULTI_PAGE)
        .to(txt)
        .execute();

    final var content = Files.readString(txt.toPath(), StandardCharsets.UTF_8);
    assertThat(content).contains("Test document Sheet 1").doesNotContain("Page 1");
  }

  @Test
  void inserterWithoutPageBreak_ShouldAppendRightAfterTheText(
      final @TempDir File testFolder, final OfficeManager manager)
      throws OfficeException, IOException {

    final var pdf = new File(testFolder, "appended.pdf");

    LocalConverter.builder()
        .officeManager(manager)
        .filterChain(new DocumentInserterFilter(ODT))
        .build()
        .convert(DOC)
        .to(pdf)
        .execute();

    // Two short documents on the same page.
    assertThat(pageCount(pdf)).isEqualTo(1);
  }

  @Test
  void withMissingDocument_ShouldThrowIllegalArgumentException(final OfficeManager manager) {

    final var missing = new File("does-not-exist.docx");

    assertThatIllegalArgumentException()
        .isThrownBy(() -> LocalConverter.make(manager).merge(DOC, missing))
        .withMessage("File not found: " + missing);
    assertThatIllegalArgumentException()
        .isThrownBy(() -> LocalConverter.make(manager).merge(List.of()));
  }
}
