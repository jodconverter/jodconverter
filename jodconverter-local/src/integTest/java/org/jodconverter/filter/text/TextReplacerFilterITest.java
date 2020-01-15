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

package org.jodconverter.filter.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.jodconverter.ResourceUtil.documentFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.LocalConverter;
import org.jodconverter.LocalOfficeManagerExtension;
import org.jodconverter.office.OfficeManager;

@ExtendWith(LocalOfficeManagerExtension.class)
public class TextReplacerFilterITest {

  private static final String SOURCE_FILENAME = "test_replace.doc";
  private static final File SOURCE_FILE = documentFile(SOURCE_FILENAME);

  /**
   * Test that the creation of a TextReplacerFilter with a search list and replacement list of
   * different size throws a IllegalArgumentException.
   */
  @Test
  public void create_WithArgumentsSizeNotEqual_ThrowsIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new TextReplacerFilter(
                    new String[] {"SEARCH_STRING", "ANOTHER_SEARCH_STRING"},
                    new String[] {"REPLACEMENT_STRING"}));
  }

  /**
   * Test that the creation of a TextReplacerFilter with an empty replacement list throws a
   * IllegalArgumentException.
   */
  @Test
  public void create_WithEmptyReplacementList_ThrowsIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new TextReplacerFilter(new String[] {"SEARCH_STRING"}, new String[0]));
  }

  /**
   * Test that the creation of a TextReplacerFilter with an empty search list throws a
   * IllegalArgumentException.
   */
  @Test
  public void create_WithEmptySearchList_ThrowsIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> new TextReplacerFilter(new String[0], new String[] {"REPLACEMENT_STRING"}));
  }

  /**
   * Test that the creation of a TextReplacerFilter with a null replacement list throws a
   * NullPointerException.
   */
  @Test
  public void create_WithNullReplacementList_ThrowsNullPointerException() {

    assertThatNullPointerException()
        .isThrownBy(() -> new TextReplacerFilter(new String[] {"SEARCH_STRING"}, null));
  }

  /**
   * Test that the creation of a TextReplacerFilter with a null search list throws a
   * NullPointerException.
   */
  @Test
  public void create_WithNullSearchList_ThrowsNullPointerException() {

    assertThatNullPointerException()
        .isThrownBy(() -> new TextReplacerFilter(null, new String[] {"REPLACEMENT_STRING"}));
  }

  /** Test the conversion of a document replacing text along the way. */
  @Test
  public void doFilter_WithDefaultProperties(@TempDir File testFolder, OfficeManager manager)
      throws IOException {

    final File targetFile = new File(testFolder, SOURCE_FILENAME + ".txt");

    // Create the GraphicInserterFilter to test.
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
