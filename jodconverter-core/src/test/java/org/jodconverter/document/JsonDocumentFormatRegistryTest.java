/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.Test;

@SuppressWarnings({"PMD.AtLeastOneConstructor", "PMD.LawOfDemeter"})
public class JsonDocumentFormatRegistryTest {

  /**
   * Test Backward compatibility.
   *
   * @throws IOException If an IO error occurs.
   */
  @Test
  public void create_UsingOldJsonFormat_AllOutputFormatsLoadedSuccessfully() throws IOException {

    try (final InputStream input =
        JsonDocumentFormatRegistry.class.getResourceAsStream("/former-document-formats.json")) {
      final JsonDocumentFormatRegistry registry = JsonDocumentFormatRegistry.create(input);
      Set<DocumentFormat> outputFormats = registry.getOutputFormats(DocumentFamily.TEXT);
      assertThat(outputFormats).hasSize(9);
      outputFormats = registry.getOutputFormats(DocumentFamily.SPREADSHEET);
      assertThat(outputFormats).hasSize(9);
      outputFormats = registry.getOutputFormats(DocumentFamily.PRESENTATION);
      assertThat(outputFormats).hasSize(8);
      outputFormats = registry.getOutputFormats(DocumentFamily.DRAWING);
      assertThat(outputFormats).hasSize(5);
    }
  }
}
