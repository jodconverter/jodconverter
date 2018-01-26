/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

package org.jodconverter.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import org.jodconverter.document.DocumentFormat.Builder;

public class DocumentFormatBuilderTest {

  @Test
  public void build_WithAllProperties_ShouldCreateExpectedDocumentFormat() {

    final Builder builder =
        DocumentFormat.builder()
            .name(DefaultDocumentFormatRegistry.CSV.getName())
            .extension(DefaultDocumentFormatRegistry.CSV.getExtension())
            .mediaType(DefaultDocumentFormatRegistry.CSV.getMediaType())
            .inputFamily(DefaultDocumentFormatRegistry.CSV.getInputFamily());
    DefaultDocumentFormatRegistry.CSV.getLoadProperties().forEach(builder::loadProperty);
    DefaultDocumentFormatRegistry.CSV
        .getStoreProperties()
        .forEach(
            (family, map) -> {
              map.forEach((name, value) -> builder.storeProperty(family, name, value));
            });

    assertThat(builder.build())
        .isEqualToComparingFieldByFieldRecursively(DefaultDocumentFormatRegistry.CSV);
  }

  @Test
  public void loadProperty_WithNullBValue_ShouldRemoveProperty() {

    final Builder builder = DocumentFormat.builder().from(DefaultDocumentFormatRegistry.CSV);
    DocumentFormat csv = builder.build();
    assertThat(csv.getLoadProperties()).containsKey("FilterOptions");
    csv = builder.loadProperty("FilterOptions", null).build();
    assertThat(csv.getLoadProperties()).doesNotContainKey("FilterOptions");
  }

  @Test
  public void storeProperty_WithNullBValue_ShouldRemoveProperty() {

    final Builder builder = DocumentFormat.builder().from(DefaultDocumentFormatRegistry.CSV);
    DocumentFormat csv = builder.build();
    assertThat(csv.getStoreProperties().get(DocumentFamily.SPREADSHEET))
        .containsKey("FilterOptions");
    csv = builder.storeProperty(DocumentFamily.SPREADSHEET, "FilterOptions", null).build();
    assertThat(csv.getStoreProperties().get(DocumentFamily.SPREADSHEET))
        .doesNotContainKey("FilterOptions");
  }
}
