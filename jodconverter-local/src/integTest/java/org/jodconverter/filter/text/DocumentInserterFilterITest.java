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

package org.jodconverter.filter.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.LocalConverter;

public class DocumentInserterFilterITest extends AbstractOfficeITest {

  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR, "test.doc");
  private static final File MERGED_FILE_1 = new File(DOCUMENTS_DIR, "test_multi_page.doc");
  private static final File MERGED_FILE_2 = new File(DOCUMENTS_DIR, "test_replace.doc");

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  /**
   * Test the conversion of a document inserting documents along the way.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_With2Filter_TargetShouldContainAllDocuments() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), "target.txt");

    // Create the DocumentInserterFilter to test.
    final DocumentInserterFilter filter1 = new DocumentInserterFilter(MERGED_FILE_1);
    final DocumentInserterFilter filter2 = new DocumentInserterFilter(MERGED_FILE_2);

    // Test the filter

    LocalConverter.builder()
        .filterChain(filter1, filter2)
        .build()
        .convert(SOURCE_FILE)
        .to(targetFile)
        .execute();

    final String content = FileUtils.readFileToString(targetFile, Charset.forName("UTF-8"));
    assertThat(content)
        .contains("Test document")
        .contains("Test document Page 1")
        .contains("Test document Page 2")
        .contains("Test document Page 3")
        .contains("Getting up early all the time");
  }
}
