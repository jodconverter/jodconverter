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

import java.util.Set;

import org.junit.Test;

@SuppressWarnings({"PMD.AtLeastOneConstructor", "PMD.LawOfDemeter"})
public class DocumentFormatTest {

  private void toString(final Set<DocumentFormat> formats) {

    for (final DocumentFormat format : formats) {
      format.toString();
    }
  }

  /** Since toString is overridden, ensure that none of the default formats throws an exception. */
  @Test
  public void toString_ShouldNotThrowException() {

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
}
