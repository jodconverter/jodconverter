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

package org.jodconverter.core.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.document.DocumentFormat.Builder;

/** Contains tests for the {@link org.jodconverter.core.document.DocumentFormat.Builder} class. */
class DocumentFormatBuilderTest {

  @Nested
  class Build {

    @Test
    void shouldCreateExpectedDocumentFormat() {

      final Builder builder =
          DocumentFormat.builder()
              .name("Foo Format")
              .extension("foo")
              .extension("fii")
              .mediaType("application/foo")
              .inputFamily(DocumentFamily.TEXT)
              .loadProperty("lprops1_name", "lprops1_value")
              .loadProperty("lprops2_name", 1)
              .loadProperty("lprops3_toremove", "bla")
              .loadProperty("lprops3_toremove", null)
              .loadFilterName("Text Filter")
              .loadFilterOptions("12345")
              .storeProperty(DocumentFamily.DRAWING, "sprops1_name", "sprops1_value")
              .storeProperty(DocumentFamily.DRAWING, "sprops2_name", 2)
              .storeProperty(DocumentFamily.DRAWING, "sprops3_toremove", "blo")
              .storeProperty(DocumentFamily.DRAWING, "sprops3_toremove", null)
              .storeFilterName(DocumentFamily.SPREADSHEET, "Text Filter 6")
              .storeFilterOptions(DocumentFamily.SPREADSHEET, "123456");

      final DocumentFormat format = builder.build();
      try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
        softly.assertThat(format.getName()).isEqualTo("Foo Format");
        softly.assertThat(format.getExtension()).isEqualTo("foo");
        softly.assertThat(format.getExtensions()).containsExactly("foo", "fii");
        softly.assertThat(format.getMediaType()).isEqualTo("application/foo");
        softly.assertThat(format.getInputFamily()).isEqualTo(DocumentFamily.TEXT);
        softly
            .assertThat(format.getLoadProperties())
            .hasSize(4)
            .contains(
                entry("lprops1_name", "lprops1_value"),
                entry("lprops2_name", 1),
                entry("FilterName", "Text Filter"),
                entry("FilterOptions", "12345"));
        softly
            .assertThat(format.getStoreProperties())
            .hasSize(2)
            .hasEntrySatisfying(
                DocumentFamily.DRAWING,
                props ->
                    assertThat(props)
                        .hasSize(2)
                        .contains(entry("sprops1_name", "sprops1_value"), entry("sprops2_name", 2)))
            .hasEntrySatisfying(
                DocumentFamily.SPREADSHEET,
                props ->
                    assertThat(props)
                        .hasSize(2)
                        .contains(
                            entry("FilterName", "Text Filter 6"),
                            entry("FilterOptions", "123456")));
      }
    }

    @Test
    void withoutLoadStoreProperties_ShouldCreateExpectedDocumentFormat() {

      final Builder builder =
          DocumentFormat.builder()
              .name("Foo Format")
              .extension("foo")
              .extension("fii")
              .mediaType("application/foo")
              .inputFamily(DocumentFamily.TEXT)
              .loadProperty("lprops1_name", null)
              .storeProperty(DocumentFamily.DRAWING, "sprops1_name", null);

      final DocumentFormat format = builder.build();
      try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
        softly.assertThat(format.getName()).isEqualTo("Foo Format");
        softly.assertThat(format.getExtension()).isEqualTo("foo");
        softly.assertThat(format.getExtensions()).containsExactly("foo", "fii");
        softly.assertThat(format.getMediaType()).isEqualTo("application/foo");
        softly.assertThat(format.getInputFamily()).isEqualTo(DocumentFamily.TEXT);
        softly.assertThat(format.getLoadProperties()).isNull();
        softly.assertThat(format.getStoreProperties()).isNull();
      }
    }

    @Test
    void removingLoadStoreProperties_ShouldCreateDocumentFormatWithNullLoadStoreProperties() {

      final Builder builder =
          DocumentFormat.builder()
              .name("Foo Format")
              .extension("foo")
              .extension("fii")
              .mediaType("application/foo")
              .inputFamily(DocumentFamily.TEXT)
              .loadProperty("lprops1_name", "lprops1_value")
              .loadProperty("lprops1_name", null)
              .storeProperty(DocumentFamily.DRAWING, "sprops1_name", "sprops1_value")
              .storeProperty(DocumentFamily.TEXT, "sprops1_name", null)
              .storeProperty(DocumentFamily.DRAWING, "sprops1_name", null);

      final DocumentFormat format = builder.build();
      try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
        softly.assertThat(format.getName()).isEqualTo("Foo Format");
        softly.assertThat(format.getExtension()).isEqualTo("foo");
        softly.assertThat(format.getExtensions()).containsExactly("foo", "fii");
        softly.assertThat(format.getMediaType()).isEqualTo("application/foo");
        softly.assertThat(format.getInputFamily()).isEqualTo(DocumentFamily.TEXT);
        softly.assertThat(format.getLoadProperties()).isNull();
        softly.assertThat(format.getStoreProperties()).isNull();
      }
    }
  }

  @Nested
  class LoadProperty {

    @Test
    void withNullValue_ShouldRemoveProperty() {

      final Builder builder = DocumentFormat.builder().from(DefaultDocumentFormatRegistry.CSV);
      DocumentFormat csv = builder.build();
      assertThat(csv.getLoadProperties()).containsKey("FilterOptions");
      csv = builder.loadProperty("FilterOptions", null).build();
      assertThat(csv.getLoadProperties()).doesNotContainKey("FilterOptions");
    }
  }

  @Nested
  class StoreProperty {

    @Test
    void withNullBValue_ShouldRemoveProperty() {

      final Builder builder = DocumentFormat.builder().from(DefaultDocumentFormatRegistry.CSV);
      DocumentFormat csv = builder.build();
      assertThat(csv.getStoreProperties()).isNotNull();
      assertThat(csv.getStoreProperties().get(DocumentFamily.SPREADSHEET))
          .containsKey("FilterOptions");
      csv = builder.storeProperty(DocumentFamily.SPREADSHEET, "FilterOptions", null).build();
      assertThat(csv.getStoreProperties()).isNotNull();
      assertThat(csv.getStoreProperties().get(DocumentFamily.SPREADSHEET))
          .doesNotContainKey("FilterOptions");
    }
  }
}
