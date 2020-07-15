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

package org.jodconverter.local.filter;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.jodconverter.local.ResourceUtil.imageFile;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;
import org.jodconverter.local.filter.text.GraphicInserterFilter;
import org.jodconverter.local.filter.text.TextReplacerFilter;

/** Contains tests that use multiple filters. */
@ExtendWith(LocalOfficeManagerExtension.class)
class MultipleFiltersITest {

  private static final String SOURCE_FILENAME = "test_replace.doc";
  private static final File SOURCE_FILE = documentFile(SOURCE_FILENAME);
  private static final File IMAGE_FILE = imageFile("sample-1.jpg");

  @Test
  void shouldApplyMultipleFilters(final @TempDir File testFolder, final OfficeManager manager) {

    // Create the TextReplacerFilter to test.
    final TextReplacerFilter replacerFilter =
        new TextReplacerFilter(
            new String[] {"SEARCH_WORD", "that", "have", "new common language will be more simple"},
            new String[] {
              "REPLACEMENT_STRING",
              "REPLACEMENT_THAT",
              "REPLACEMENT_HAVE",
              "most recent common language will be more basic"
            });

    final File targetFile = new File(testFolder, SOURCE_FILENAME + ".pdf");
    assertThatCode(
            () -> {
              // Create the GraphicInserterFilter to test.
              final GraphicInserterFilter graphicfilter =
                  new GraphicInserterFilter(
                      IMAGE_FILE.getPath(),
                      74, // Image Width // 7.4 CM (half the original size)
                      56, // Image Height // 5.6 CM (half the original size)
                      60, // Horizontal Position // 6 CM
                      100); // Vertical Position // 10 CM

              // Convert to PDF
              LocalConverter.builder()
                  .officeManager(manager)
                  .filterChain(replacerFilter, graphicfilter)
                  .build()
                  .convert(SOURCE_FILE)
                  .to(targetFile)
                  .execute();
            })
        .doesNotThrowAnyException();
  }
}
