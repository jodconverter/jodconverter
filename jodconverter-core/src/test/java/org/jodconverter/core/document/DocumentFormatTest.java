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

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Contains tests for the {@link DocumentFormat} class. */
class DocumentFormatTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentFormatTest.class);

  private void toString(final Set<DocumentFormat> formats) {

    formats.forEach(fmt -> LOGGER.info(fmt.toString()));
  }

  /** Since toString is overridden, ensure that none of the default formats throws an exception. */
  @Test
  void toString_ShouldNotThrowException() {

    // If an exception is thrown, the test will automatically fail.

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();
    // TEXT output format
    toString(registry.getOutputFormats(DocumentFamily.TEXT));
    // SPREADSHEET output format
    toString(registry.getOutputFormats(DocumentFamily.SPREADSHEET));
    // PRESENTATION output format
    toString(registry.getOutputFormats(DocumentFamily.PRESENTATION));
    // DRAWING output format
    toString(registry.getOutputFormats(DocumentFamily.DRAWING));
  }

  @Test
  void copy_ShouldCreateModifiableCopy() {

    // If an exception is thrown, the test will automatically fail.
    final DocumentFormat copy = DocumentFormat.copy(DefaultDocumentFormatRegistry.CSV);
    assertThat(copy).isNotEqualTo(DefaultDocumentFormatRegistry.CSV);
    assertThat(copy.getName()).isEqualTo(DefaultDocumentFormatRegistry.CSV.getName());

    // Ensure it is modifiable
    final Map<String, Object> map = copy.getLoadProperties();
    assertThat(map).isNotNull();
    map.put("PropertyX", "ValueX");
  }
}
