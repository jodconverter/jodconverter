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

import java.util.Set;

import org.junit.Test;

public class DefaultDocumentFormatRegistryTest {

  private void assertExpectedExtensions(Set<DocumentFormat> formats, String... extensions) {

    assertThat(formats).hasSize(extensions.length);
    for (final DocumentFormat format : formats) {
      assertThat(format.getExtension()).isIn((Object[]) extensions);
    }
    //formats
    //.stream()
    //.forEach(
    //fmt ->
    //assertThat(fmt.getExtension())
    //.isIn(extensions));
  }

  /** Tests all the default output formats are load successfully. */
  @Test
  public void getInstance_AllOutputFormatsLoadedSuccessfully() {

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();

    // TEXT output format
    Set<DocumentFormat> outputFormats = registry.getOutputFormats(DocumentFamily.TEXT);
    assertExpectedExtensions(
        outputFormats, "doc", "docx", "html", "odt", "pdf", "png", "rtf", "sxw", "txt");
    // SPREADSHEET output format
    outputFormats = registry.getOutputFormats(DocumentFamily.SPREADSHEET);
    assertExpectedExtensions(
        outputFormats, "csv", "html", "ods", "pdf", "png", "sxc", "tsv", "xls", "xlsx");
    // PRESENTATION output format
    outputFormats = registry.getOutputFormats(DocumentFamily.PRESENTATION);
    assertExpectedExtensions(
        outputFormats, "html", "odp", "pdf", "png", "ppt", "pptx", "swf", "sxi");
    // DRAWING output format
    outputFormats = registry.getOutputFormats(DocumentFamily.DRAWING);
    assertExpectedExtensions(outputFormats, "odg", "pdf", "png", "svg", "swf");
  }
}
