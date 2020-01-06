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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.LocalConverter;

public class TextReplacerFilterITest extends AbstractOfficeITest {

  private static final String SOURCE_FILENAME = "test_replace.doc";
  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR, SOURCE_FILENAME);

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  /**
   * Test that the creation of a TextReplacerFilter with a search list and replacement list of
   * different size throws a IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void create_WithArgumentsSizeNotEqual_ThrowsIllegalArgumentException() {

    new TextReplacerFilter(
        new String[] {"SEARCH_STRING", "ANOTHER_SEARCH_STRING"},
        new String[] {"REPLACEMENT_STRING"});
  }

  /**
   * Test that the creation of a TextReplacerFilter with an empty replacement list throws a
   * IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void create_WithEmptyReplacementList_ThrowsIllegalArgumentException() {

    new TextReplacerFilter(new String[] {"SEARCH_STRING"}, new String[0]);
  }

  /**
   * Test that the creation of a TextReplacerFilter with an empty search list throws a
   * IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void create_WithEmptySearchList_ThrowsIllegalArgumentException() {

    new TextReplacerFilter(new String[0], new String[] {"REPLACEMENT_STRING"});
  }

  /**
   * Test that the creation of a TextReplacerFilter with a null replacement list throws a
   * NullPointerException.
   */
  @Test(expected = NullPointerException.class)
  public void create_WithNullReplacementList_ThrowsNullPointerException() {

    new TextReplacerFilter(new String[] {"SEARCH_STRING"}, null);
  }

  /**
   * Test that the creation of a TextReplacerFilter with a null search list throws a
   * NullPointerException.
   */
  @Test(expected = NullPointerException.class)
  public void create_WithNullSearchList_ThrowsNullPointerException() {

    new TextReplacerFilter(null, new String[] {"REPLACEMENT_STRING"});
  }

  /**
   * Test the conversion of a document replacing text along the way.
   *
   * @throws IOException If an IO error occurs.
   * @throws Exception If an error occurs.
   */
  @Test
  public void doFilter_WithDefaultProperties() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), SOURCE_FILENAME + ".txt");

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

    LocalConverter.builder()
        .filterChain(filter)
        .build()
        .convert(SOURCE_FILE)
        .to(targetFile)
        .execute();

    final String content = FileUtils.readFileToString(targetFile, Charset.forName("UTF-8"));
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
