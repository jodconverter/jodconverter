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

package org.jodconverter.local.filter.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;

/** Contains tests for the {@link TableOfContentUpdaterFilter} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
public class TableOfContentUpdaterFilterITest {

  private static final String SOURCE_FILENAME = "test_toc.odt";
  private static final File SOURCE_FILE = documentFile(SOURCE_FILENAME);

  /** Test that updating the table of content only will just update the TOC. */
  @Test
  public void doFilter_UpdateOnly_TableOfContentUpdatedSuccessfully(
      final @TempDir File testFolder, final OfficeManager manager) throws IOException {

    final File targetFile = new File(testFolder, SOURCE_FILENAME + ".updateonly.txt");

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(new TableOfContentUpdaterFilter())
                    .build()
                    .convert(SOURCE_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    final String content = FileUtils.readFileToString(targetFile, StandardCharsets.UTF_8);
    assertThat(content)
        .as("Check content: %s", content)
        .containsPattern(
            "(?s)Contents.*"
                + "Copyright\\s+2.{1,2}"
                + "Contributors\\s+2.{1,2}"
                + "Feedback\\s+2.{1,2}"
                + "Acknowledgments\\s+2.{1,2}"
                + "Publication date and software version\\s+2.{1,2}"
                + "Note for Mac users\\s+2.{1,2}"
                + "Heading 1\\s+4.{1,2}"
                + "Heading 2\\s+4.{1,2}"
                + "Heading 3\\s+4.{1,2}"
                + "Figures\\s+5.{1,2}"
                + "Tables\\s+6.{1,2}"
                + "Notes, Tips and Cautions\\s+7.{1,2}"
                + "Lists\\s+8.{1,2}"
                + "Numbered lists\\s+8.{1,2}"
                + "Bullet lists\\s+8.{1,2}"
                + "Mixed lists\\s+8.{1,2}"
                + "Simple lists\\s+8.{1,2}"
                + "Definition lists\\s+9.{1,2}"
                + "Other paragraph styles\\s+10.{1,2}"
                + "Character styles\\s+11.{1,2}"
                + "Computer code styles\\s+12.{1,2}"
                + "Paragraph style Code\\s+12.{1,2}"
                + "Character style Code\\s+12.{1,2}"
                + "Cover pages\\s+13.{1,2}"
                + "Cross referencing\\s+14");
  }

  /**
   * Test that updating the table of content and removing level only will update the TOC at the
   * disired level.
   */
  @Test
  public void doFilter_UpdateChangingLevel_TableOfContentUpdatedSuccessfully(
      final @TempDir File testFolder, final OfficeManager manager) throws IOException {

    final File targetFile = new File(testFolder, SOURCE_FILENAME + ".updatelevel.txt");

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(new TableOfContentUpdaterFilter(1))
                    .build()
                    .convert(SOURCE_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    final String content = FileUtils.readFileToString(targetFile, StandardCharsets.UTF_8);
    assertThat(content)
        .as("Check content: %s", content)
        .containsPattern(
            "(?s)Contents.*"
                + "Copyright\\s+2.{1,2}"
                + "Heading 1\\s+4.{1,2}"
                + "Figures\\s+5.{1,2}"
                + "Tables\\s+6.{1,2}"
                + "Notes, Tips and Cautions\\s+7.{1,2}"
                + "Lists\\s+8.{1,2}"
                + "Other paragraph styles\\s+10.{1,2}"
                + "Character styles\\s+11.{1,2}"
                + "Computer code styles\\s+12.{1,2}"
                + "Cover pages\\s+13.{1,2}"
                + "Cross referencing\\s+14");
  }

  /**
   * Test the conversion of a document which is not a TEXT document. We can't really test the
   * result, but at least we will test the the conversion doesn't fail (filter does nothing).
   */
  @Test
  public void doFilter_WithBadDocumentType_DoNothing(
      final @TempDir File testFolder, final OfficeManager manager) {

    final File targetFile = new File(testFolder, SOURCE_FILENAME + ".badtype.pdf");

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(new TableOfContentUpdaterFilter(1))
                    .build()
                    .convert(documentFile("test.xls"))
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();
  }
}
