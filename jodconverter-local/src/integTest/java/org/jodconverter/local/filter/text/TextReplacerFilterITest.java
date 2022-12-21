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

package org.jodconverter.local.filter.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;

/** Contains tests for the {@link TextReplacerFilter} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
class TextReplacerFilterITest {

  private static final String SOURCE_FILENAME = "test_replace.doc";
  private static final File SOURCE_FILE = documentFile(SOURCE_FILENAME);

  @Test
  void shouldReplaceWords(final @TempDir File testFolder, final OfficeManager manager)
      throws IOException {

    final File targetFile = new File(testFolder, SOURCE_FILENAME + ".txt");

    // Create the TextReplacerFilter to test.
    final TextReplacerFilter filter =
        new TextReplacerFilter(
            new String[] {"SEARCH_WORD", "that", "have", "new common language will be more simple"},
            new String[] {
              "REPLACEMENT_STRING",
              "REPLACEMENT_THAT",
              "REPLACEMENT_HAVE",
              "most recent common language will be more basic"
            });

    // Convert to PDF
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(filter)
                    .build()
                    .convert(SOURCE_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    final String content = FileUtils.readFileToString(targetFile, StandardCharsets.UTF_8);
    assertThat(content)
        .as("Check content: %s", content)
        .contains("REPLACEMENT_STRING")
        .doesNotContain("SEARCH_WORD")
        .contains("REPLACEMENT_THAT")
        .doesNotContain("that")
        .contains("REPLACEMENT_HAVE")
        .doesNotContain("have")
        .contains("most recent common language will be more basic")
        .doesNotContain("new common language will be more simple");
  }
}
