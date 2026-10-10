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

package org.jodconverter.local.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.SimpleDocumentFormatRegistry;

/** Contains tests for the {@link OfficeFormatRegistry} class. */
class OfficeFormatRegistryTest {

  private static final String TEXT = "com.sun.star.text.TextDocument";
  private static final String CALC = "com.sun.star.sheet.SpreadsheetDocument";
  private static final String DRAW = "com.sun.star.drawing.DrawingDocument";
  private static final String FILTER_NAME = "FilterName";

  // The filters as OfficeInfoTask gives them, sorted by name.
  private static final OfficeInfo INFO =
      new OfficeInfo(
          "LibreOffice",
          "25.2",
          List.of(
              filter("EPUB", TEXT, "application/epub+zip", false, true, "epub"),
              filter("MS Word 2007 XML VBA", TEXT, null, true, true, "docm"),
              filter("Office Open XML Text", TEXT, "application/x-docm", true, true, "DOCM"),
              filter("calc_webp_Export", CALC, "image/webp", false, true, "webp"),
              filter("draw_webp_Export", DRAW, "image/webp", false, true, "webp"),
              filter("fb2_import", TEXT, null, true, false, "fb2"),
              filter("math8", "com.sun.star.formula.FormulaProperties", null, true, true, "odf"),
              filter("no_service", null, null, true, true, "zzz"),
              filter("writer_pdf_Export", TEXT, "application/pdf", false, true, "pdf")));

  private final OfficeFormatRegistry registry = OfficeFormatRegistry.create(INFO);

  private static OfficeFilterInfo filter(
      final String name,
      final String service,
      final String mediaType,
      final boolean canImport,
      final boolean canExport,
      final String... extensions) {
    return new OfficeFilterInfo(
        name, name + " UI", "type", service, mediaType, List.of(extensions), canImport, canExport);
  }

  @Nested
  class Create {

    @Test
    void withNullArguments_ShouldThrowNullPointerException() {

      assertThatNullPointerException().isThrownBy(() -> OfficeFormatRegistry.create(null, INFO));
      assertThatNullPointerException()
          .isThrownBy(
              () -> OfficeFormatRegistry.create(DefaultDocumentFormatRegistry.getInstance(), null));
    }
  }

  @Nested
  class GetFormatByExtension {

    @Test
    void withAnExtensionOfTheOtherRegistry_ShouldReturnItsFormat() {

      assertThat(registry.getFormatByExtension("pdf")).isSameAs(DefaultDocumentFormatRegistry.PDF);
    }

    @Test
    void withAnExportOnlyExtensionOfTheOffice_ShouldBuildAnOutputFormat() {

      final var format = registry.getFormatByExtension("EPUB");

      assertThat(format).isNotNull();
      assertThat(format.getName()).isEqualTo("EPUB UI");
      assertThat(format.getExtension()).isEqualTo("epub");
      assertThat(format.getMediaType()).isEqualTo("application/epub+zip");
      assertThat(format.getInputFamily()).isNull();
      assertThat(format.getStoreProperties())
          .containsExactly(Map.entry(DocumentFamily.TEXT, Map.of(FILTER_NAME, "EPUB")));
    }

    @Test
    void withSeveralFiltersOfAFamily_ShouldTakeTheFirstByName() {

      final var format = registry.getFormatByExtension("docm");

      assertThat(format).isNotNull();
      assertThat(format.getInputFamily()).isEqualTo(DocumentFamily.TEXT);
      // The media type of the first filter that has one.
      assertThat(format.getMediaType()).isEqualTo("application/x-docm");
      assertThat(format.getStoreProperties(DocumentFamily.TEXT))
          .containsExactly(Map.entry(FILTER_NAME, "MS Word 2007 XML VBA"));
    }

    @Test
    void withFiltersOfSeveralFamilies_ShouldHaveOneFilterPerFamily() {

      final var format = registry.getFormatByExtension("webp");

      assertThat(format).isNotNull();
      assertThat(format.getStoreProperties(DocumentFamily.SPREADSHEET))
          .containsExactly(Map.entry(FILTER_NAME, "calc_webp_Export"));
      assertThat(format.getStoreProperties(DocumentFamily.DRAWING))
          .containsExactly(Map.entry(FILTER_NAME, "draw_webp_Export"));
      assertThat(format.getStoreProperties(DocumentFamily.TEXT)).isNull();
    }

    @Test
    void withAnImportOnlyExtension_ShouldBuildAnInputFormat() {

      final var format = registry.getFormatByExtension("fb2");

      assertThat(format).isNotNull();
      assertThat(format.getName()).isEqualTo("fb2_import UI");
      assertThat(format.getInputFamily()).isEqualTo(DocumentFamily.TEXT);
      assertThat(format.getMediaType()).isEqualTo("application/octet-stream");
      assertThat(format.getStoreProperties()).isEmpty();
    }

    @Test
    void withAFilterOfNoDocumentFamily_ShouldReturnNull() {

      assertThat(registry.getFormatByExtension("odf")).isNull();
      assertThat(registry.getFormatByExtension("zzz")).isNull();
      assertThat(registry.getFormatByExtension("unknown")).isNull();
    }
  }

  @Nested
  class GetFormatByMediaType {

    @Test
    void shouldAskTheOtherRegistryFirst() {

      assertThat(registry.getFormatByMediaType("application/pdf"))
          .isSameAs(DefaultDocumentFormatRegistry.PDF);
      assertThat(registry.getFormatByMediaType("APPLICATION/EPUB+ZIP"))
          .isSameAs(registry.getFormatByExtension("epub"));
      assertThat(registry.getFormatByMediaType("application/unknown")).isNull();
    }
  }

  @Nested
  class GetOutputFormats {

    @Test
    void shouldAddTheFormatsOfTheOffice() {

      final var defaults =
          DefaultDocumentFormatRegistry.getInstance().getOutputFormats(DocumentFamily.TEXT);

      assertThat(registry.getOutputFormats(DocumentFamily.TEXT))
          .containsAll(defaults)
          .contains(registry.getFormatByExtension("epub"), registry.getFormatByExtension("docm"))
          .doesNotContain(registry.getFormatByExtension("webp"))
          .hasSize(defaults.size() + 2);
      assertThat(registry.getOutputFormats(DocumentFamily.DRAWING))
          .contains(registry.getFormatByExtension("webp"));
    }
  }

  @Nested
  class WithAnotherRegistry {

    @Test
    void shouldCompleteThatRegistry() {

      final var own = new SimpleDocumentFormatRegistry();
      final var epub =
          DocumentFormat.builder()
              .name("My EPUB")
              .extension("epub")
              .mediaType("application/epub+zip")
              .storeFilterName(DocumentFamily.TEXT, "EPUB")
              .build();
      own.addFormat(epub);

      final var completed = OfficeFormatRegistry.create(own, INFO);

      assertThat(completed.getFormatByExtension("epub")).isSameAs(epub);
      // Unknown to that registry, so built from the office.
      assertThat(completed.getFormatByExtension("pdf"))
          .isNotNull()
          .extracting(format -> format.getStoreProperties(DocumentFamily.TEXT))
          .isEqualTo(Map.of(FILTER_NAME, "writer_pdf_Export"));
    }
  }
}
