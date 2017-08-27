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

package org.jodconverter.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.document.DefaultDocumentFormatRegistry;

public class TargetDocumentSpecsFromFileTest {

  private static final String SOURCE_FILE = "src/test/resources/documents/test.txt";
  private static final String TARGET_FILENAME = "test.pdf";

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void onFailure_ShouldDeleteTargetFile() throws IOException {

    final File targetFile = new File(testFolder.getRoot(), TARGET_FILENAME);
    FileUtils.copyFile(new File(SOURCE_FILE), targetFile);
    assertThat(targetFile).exists();

    final TargetDocumentSpecsFromFile specs = new TargetDocumentSpecsFromFile(targetFile);

    specs.onFailure(targetFile, new IOException());

    // Check that the temp file is deleted
    assertThat(targetFile).doesNotExist();
  }

  @Test
  public void ctor_WithValidValues_SpecsCreatedWithExpectedValues() throws IOException {

    final File targetFile = new File(testFolder.getRoot(), TARGET_FILENAME);
    FileUtils.copyFile(new File(SOURCE_FILE), targetFile);
    assertThat(targetFile).exists();

    final Map<String, Object> storeProperties = new HashMap<>();
    storeProperties.put("Overwrite", true);

    final TargetDocumentSpecsFromFile specs = new TargetDocumentSpecsFromFile(targetFile);
    specs.setDocumentFormat(DefaultDocumentFormatRegistry.CSV);

    assertThat(specs)
        .extracting("file", "documentFormat")
        .containsExactly(targetFile, DefaultDocumentFormatRegistry.CSV);
  }
}
