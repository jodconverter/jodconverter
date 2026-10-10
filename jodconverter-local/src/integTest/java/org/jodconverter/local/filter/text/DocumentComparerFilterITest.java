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

package org.jodconverter.local.filter.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import com.sun.star.document.XRedlinesSupplier;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Props;

/** Contains tests for the {@link DocumentComparerFilter} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
class DocumentComparerFilterITest {

  // The document being converted, and what its earlier version had instead of SEARCH_WORD: the
  // two have no character in common, since the office compares the words character by character.
  private static final File SOURCE_FILE = documentFile("test_replace.doc");
  private static final String NEW_WORD = "SEARCH_WORD";
  private static final String OLD_WORD = "0123456789";

  private static @TempDir File originalFolder;
  private static File original;

  @BeforeAll
  static void createOriginal(final OfficeManager manager) throws OfficeException {

    original = new File(originalFolder, "original.odt");
    LocalConverter.builder()
        .officeManager(manager)
        .filterChain(new TextReplacerFilter(new String[] {NEW_WORD}, new String[] {OLD_WORD}))
        .build()
        .convert(SOURCE_FILE)
        .to(original)
        .execute();
  }

  // Counts the tracked changes of the document by type, when it reaches the filter.
  private static Filter redlineCounter(final Map<String, Integer> counts) {
    return (context, document, chain) -> {
      final var redlines =
          Lo.qi(XRedlinesSupplier.class, document).getRedlines().createEnumeration();
      while (redlines.hasMoreElements()) {
        final var type = String.valueOf(Props.getProperty(redlines.nextElement(), "RedlineType"));
        counts.merge(type, 1, Integer::sum);
      }
      chain.doFilter(context, document);
    };
  }

  @Test
  void withEditableDocument_ShouldTrackTheDifferences(
      final @TempDir File testFolder, final OfficeManager manager) throws Exception {

    final var before = new TreeMap<String, Integer>();
    final var after = new TreeMap<String, Integer>();
    final var pdf = new File(testFolder, "changes.pdf");

    LocalConverter.builder()
        .officeManager(manager)
        .loadProperty("ReadOnly", false)
        .filterChain(
            redlineCounter(before), new DocumentComparerFilter(original), redlineCounter(after))
        .build()
        .convert(SOURCE_FILE)
        .to(pdf)
        .execute();

    // The word of the document is an insertion, the word of the original a deletion.
    assertThat(before).isEmpty();
    assertThat(after).containsOnlyKeys("Delete", "Insert");
    assertThat(after.get("Insert")).isEqualTo(after.get("Delete")).isPositive();

    // The PDF shows both.
    try (var doc = Loader.loadPDF(pdf)) {
      assertThat(new PDFTextStripper().getText(doc)).contains(NEW_WORD, OLD_WORD);
    }
  }

  @Test
  void withTheSameDocument_ShouldTrackNothing(
      final @TempDir File testFolder, final OfficeManager manager) throws Exception {

    final var after = new TreeMap<String, Integer>();

    LocalConverter.builder()
        .officeManager(manager)
        .loadProperty("ReadOnly", false)
        .filterChain(new DocumentComparerFilter(SOURCE_FILE), redlineCounter(after))
        .build()
        .convert(SOURCE_FILE)
        .to(new File(testFolder, "same.pdf"))
        .execute();

    assertThat(after).isEmpty();
  }

  @Test
  void withReadOnlyDocument_ShouldThrowOfficeException(
      final @TempDir File testFolder, final OfficeManager manager) {

    // Read-only is the default of a converter.
    final var converter =
        LocalConverter.builder()
            .officeManager(manager)
            .filterChain(new DocumentComparerFilter(original))
            .build();

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(
            () -> converter.convert(SOURCE_FILE).to(new File(testFolder, "ro.pdf")).execute())
        .withMessageContaining("loadProperty(\"ReadOnly\", false)");
  }

  @Test
  void withSpreadsheet_ShouldLeaveItAsItIs(
      final @TempDir File testFolder, final OfficeManager manager) throws Exception {

    final var pdf = new File(testFolder, "sheet.pdf");

    LocalConverter.builder()
        .officeManager(manager)
        .filterChain(new DocumentComparerFilter(original))
        .build()
        .convert(documentFile("test.xls"))
        .to(pdf)
        .execute();

    assertThat(pdf).isNotEmpty();
  }
}
