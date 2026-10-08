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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** Contains tests for the {@link JsonDocumentFormatRegistry} class. */
class JsonDocumentFormatRegistryTest {

  @Test
  void create_WithNumericProperties_ShouldKeepWholeNumbersAsIntegers() {

    final var registry =
        JsonDocumentFormatRegistry.create(
            """
                                [
                                  {
                                    "name": "Portable Document Format",
                                    "extensions": ["pdf"],
                                    "mediaType": "application/pdf",
                                    "loadProperties": { "Version": 2 },
                                    "storeProperties": {
                                      "TEXT": {
                                        "FilterName": "writer_pdf_Export",
                                        "FilterData": {
                                          "SelectPdfVersion": 15,
                                          "Negative": -3,
                                          "Big": 4294967296,
                                          "Ratio": 1.5,
                                          "Exponent": 1e3
                                        }
                                      }
                                    }
                                  }
                                ]""");

    final var format = registry.getFormatByExtension("pdf");
    assertThat(format).isNotNull();
    assertThat(format.getLoadProperties()).containsEntry("Version", 2);
    assertThat(format.getStoreProperties(DocumentFamily.TEXT))
        .extractingByKey("FilterData")
        .isInstanceOfSatisfying(
            Map.class,
            filterData ->
                assertThat(filterData)
                    .containsEntry("SelectPdfVersion", 15)
                    .containsEntry("Negative", -3)
                    .containsEntry("Big", 4_294_967_296L)
                    .containsEntry("Ratio", 1.5)
                    .containsEntry("Exponent", 1000.0));
  }

  /** Test custom properties. */
  @Test
  void create_WithCustomLoadProperties_CustomPropertiesAppliedSuccessfully() throws IOException {

    try (var input =
        JsonDocumentFormatRegistry.class.getResourceAsStream("/document-formats.json")) {

      // Custom html props.
      final var customFromTextToHtmlStoreProps = new HashMap<String, Object>();
      customFromTextToHtmlStoreProps.put("FilterOptions", "EmbedImages");
      final var customHtmlProps = new DocumentFormatProperties();
      customHtmlProps.getStore().put(DocumentFamily.TEXT, customFromTextToHtmlStoreProps);

      // Custom text props.
      final var customFromTextToTextStoreProps = new HashMap<String, Object>();
      customFromTextToTextStoreProps.put("FilterOptions", "utf16");
      final var customTextProps = new DocumentFormatProperties();
      customTextProps.getLoad().put("FilterOptions", "utf16");
      customTextProps.getStore().put(DocumentFamily.TEXT, customFromTextToTextStoreProps);

      final var customPropsPerExtension = new HashMap<String, DocumentFormatProperties>();
      customPropsPerExtension.put("html", customHtmlProps);
      customPropsPerExtension.put("txt", customTextProps);

      final var registry = JsonDocumentFormatRegistry.create(input, customPropsPerExtension);

      var format = registry.getFormatByExtension("html");
      assertThat(format).isNotNull();
      var storeProps = format.getStoreProperties();
      assertThat(storeProps).isNotNull();
      assertThat(storeProps.get(DocumentFamily.TEXT).get("FilterOptions")).isEqualTo("EmbedImages");

      format = registry.getFormatByExtension("txt");
      assertThat(format).isNotNull();
      final var loadProps = format.getLoadProperties();
      assertThat(loadProps).isNotNull();
      assertThat(loadProps.get("FilterOptions")).isEqualTo("utf16");

      storeProps = format.getStoreProperties();
      assertThat(storeProps).isNotNull();
      assertThat(storeProps.get(DocumentFamily.TEXT).get("FilterOptions")).isEqualTo("utf16");
    }
  }
}
