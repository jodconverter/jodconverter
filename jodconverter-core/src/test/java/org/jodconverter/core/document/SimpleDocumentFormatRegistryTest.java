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

import org.junit.jupiter.api.Test;

/** Contains tests for the {@link SimpleDocumentFormatRegistry} class. */
class SimpleDocumentFormatRegistryTest {

  /** Tests that calling addRegistry with a null registry does nothing. */
  @Test
  void addRegistry_WithNullRegistry_DoNothing() {

    final SimpleDocumentFormatRegistry sourceRegistry = new SimpleDocumentFormatRegistry();
    sourceRegistry.addFormat(
        DefaultDocumentFormatRegistry.getInstance().getFormatByExtension("pdf"));
    sourceRegistry.addRegistry(null);

    final DocumentFormat testFormat = sourceRegistry.getFormatByExtension("test");
    assertThat(testFormat).isNull();
    final DocumentFormat pdfFormat = sourceRegistry.getFormatByExtension("pdf");
    assertThat(pdfFormat).isNotNull().hasFieldOrPropertyWithValue("extension", "pdf");
    assertThat(pdfFormat.getStoreProperties()).isNotEmpty();
  }

  /** Tests that calling addRegistry with a null registry does nothing. */
  @Test
  void addRegistry_WithCustomRegitry_DocumentFormatsFromCustomRegistryAdded() {

    final SimpleDocumentFormatRegistry sourceRegistry = new SimpleDocumentFormatRegistry();
    sourceRegistry.addFormat(
        DefaultDocumentFormatRegistry.getInstance().getFormatByExtension("pdf"));
    final SimpleDocumentFormatRegistry toAdd = new SimpleDocumentFormatRegistry();
    toAdd.addFormat(
        DocumentFormat.builder()
            .name("TestName")
            .extension("test")
            .mediaType("application/test")
            .build());
    toAdd.addFormat(
        DocumentFormat.builder().name("Pdf").extension("pdf").mediaType("application/pdf").build());
    sourceRegistry.addRegistry(toAdd);

    final DocumentFormat testFormat = sourceRegistry.getFormatByExtension("pdf");
    assertThat(testFormat).isNotNull().hasFieldOrPropertyWithValue("extension", "pdf");
    final DocumentFormat pdfFormat = sourceRegistry.getFormatByExtension("pdf");
    assertThat(pdfFormat).isNotNull().hasFieldOrPropertyWithValue("extension", "pdf");
    assertThat(pdfFormat.getStoreProperties()).isNullOrEmpty();
  }

  /**
   * Tests that calling getFormatByExtension with a valid extension will return the expected
   * document format.
   */
  @Test
  void getFormatByExtension_WithPdfExtension_ReturnPdfDocumentFormat() {

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();
    final DocumentFormat format = registry.getFormatByExtension("pdf");
    assertThat(format).isNotNull().hasFieldOrPropertyWithValue("extension", "pdf");
  }

  /**
   * Tests that calling getFormatByMediaType with a valid type will return the expected document
   * format.
   */
  @Test
  void getFormatByMediaType_WithPdfMediaType_ReturnPdfDocumentFormat() {

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();
    final DocumentFormat format = registry.getFormatByMediaType("application/pdf");
    assertThat(format).isNotNull().hasFieldOrPropertyWithValue("extension", "pdf");
  }
}
