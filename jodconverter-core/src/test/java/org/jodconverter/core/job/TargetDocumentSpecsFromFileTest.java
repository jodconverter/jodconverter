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

package org.jodconverter.core.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.util.FileUtils;

/** Contains tests for the {@link TargetDocumentSpecsFromFile} class. */
public class TargetDocumentSpecsFromFileTest {

  private static final String SOURCE_FILE = "src/test/resources/documents/test.txt";
  private static final String TARGET_FILENAME = "test.pdf";

  @Test
  public void onFailure_ShouldDeleteTargetFile(final @TempDir File testFolder) throws IOException {

    final File targetFile = new File(testFolder, TARGET_FILENAME);
    FileUtils.copyFile(new File(SOURCE_FILE), targetFile);
    assertThat(targetFile).exists();

    final TargetDocumentSpecsFromFile specs = new TargetDocumentSpecsFromFile(targetFile);

    specs.onFailure(targetFile, new IOException());

    // Check that the temp file is deleted
    assertThat(targetFile).doesNotExist();
  }

  @Test
  public void new_WithValidValues_SpecsCreatedWithExpectedValues(final @TempDir File testFolder)
      throws IOException {

    final File targetFile = new File(testFolder, TARGET_FILENAME);
    FileUtils.copyFile(new File(SOURCE_FILE), targetFile);
    assertThat(targetFile).exists();

    final TargetDocumentSpecsFromFile specs = new TargetDocumentSpecsFromFile(targetFile);
    specs.setDocumentFormat(DefaultDocumentFormatRegistry.CSV);

    assertThat(specs)
        .extracting("file", "documentFormat")
        .containsExactly(targetFile, DefaultDocumentFormatRegistry.CSV);
  }
}
