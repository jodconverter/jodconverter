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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;

/** Contains tests for the {@link DocumentInserterFilter} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
public class DocumentInserterFilterITest {

  @Test
  public void doFilter_With2Filter_TargetShouldContainAllDocuments(
      final @TempDir File testFolder, final OfficeManager manager) throws IOException {

    final File sourceFile = documentFile("test.doc");
    final File targetFile = new File(testFolder, "target.txt");

    // Create the DocumentInserterFilter to test.
    final DocumentInserterFilter filter1 =
        new DocumentInserterFilter(documentFile("test_multi_page.doc"));
    final DocumentInserterFilter filter2 =
        new DocumentInserterFilter(documentFile("test_replace.doc"));

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(filter1, filter2)
                    .build()
                    .convert(sourceFile)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    final String content = FileUtils.readFileToString(targetFile, StandardCharsets.UTF_8);
    assertThat(content)
        .as("Check content: %s", content)
        .contains("Test document")
        .contains("Test document Page 1")
        .contains("Test document Page 2")
        .contains("Test document Page 3")
        .contains("Getting up early all the time");
  }
}
