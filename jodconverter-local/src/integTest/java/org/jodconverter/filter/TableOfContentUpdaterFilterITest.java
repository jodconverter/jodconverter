/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

package org.jodconverter.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.LocalConverter;
import org.jodconverter.filter.text.TableOfContentUpdaterFilter;

public class TableOfContentUpdaterFilterITest extends AbstractOfficeITest {

  private static final String SOURCE_FILENAME = "test_toc.odt";
  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR, SOURCE_FILENAME);

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  /**
   * Test that updating the table of content only will just update the TOC.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_UpdateOnly_TableOfContentUpdatedSuccessfully() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), SOURCE_FILENAME + ".updateonly.txt");

    // Test the filter
    LocalConverter.builder()
        .filterChain(new TableOfContentUpdaterFilter())
        .build()
        .convert(SOURCE_FILE)
        .to(targetFile)
        .execute();

    final String content = FileUtils.readFileToString(targetFile, Charset.forName("UTF-8"));
    assertThat(content)
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
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_UpdateChangingLevel_TableOfContentUpdatedSuccessfully() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), SOURCE_FILENAME + ".updatelevel.txt");

    // Test the filter
    LocalConverter.builder()
        .filterChain(new TableOfContentUpdaterFilter(1))
        .build()
        .convert(SOURCE_FILE)
        .to(targetFile)
        .execute();

    final String content = FileUtils.readFileToString(targetFile, Charset.forName("UTF-8"));
    assertThat(content)
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
}
