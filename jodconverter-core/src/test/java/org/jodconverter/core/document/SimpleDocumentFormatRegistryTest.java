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

package org.jodconverter.core.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class SimpleDocumentFormatRegistryTest {

  /** Tests that calling getFormatByExtension with a null extension will return null. */
  @Test
  public void getFormatByExtension_WithNullExtension_ReturnNull() {

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();
    final DocumentFormat format = registry.getFormatByExtension(null);
    assertThat(format).isNull();
  }

  /**
   * Tests that calling getFormatByExtension with a valid extension will return the expected
   * document format.
   */
  @Test
  public void getFormatByExtension_WithPdfExtension_ReturnPdfDocumentFormat() {

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();
    final DocumentFormat format = registry.getFormatByExtension("pdf");
    assertThat(format).isNotNull().hasFieldOrPropertyWithValue("extension", "pdf");
  }

  /** Tests that calling getFormatByMediaType with a null type will return null. */
  @Test
  public void getFormatByMediaType_WithNullMediaType_ReturnNull() {

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();
    final DocumentFormat format = registry.getFormatByMediaType(null);
    assertThat(format).isNull();
  }

  /**
   * Tests that calling getFormatByMediaType with a valid type will return the expected document
   * format.
   */
  @Test
  public void getFormatByMediaType_WithPdfMediaType_ReturnPdfDocumentFormat() {

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();
    final DocumentFormat format = registry.getFormatByMediaType("application/pdf");
    assertThat(format).isNotNull().hasFieldOrPropertyWithValue("extension", "pdf");
  }

  /** Tests that calling getOutputFormats with a null family will return an empty set. */
  @Test
  public void getOutputFormats_WithNullFamily_ReturnEmptySet() {

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();
    final Set<DocumentFormat> formats = registry.getOutputFormats(null);
    assertThat(formats).isEmpty();
  }
}
