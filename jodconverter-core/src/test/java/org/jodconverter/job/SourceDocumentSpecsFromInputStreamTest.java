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
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SourceDocumentSpecsFromInputStreamTest {

  private static final String SOURCE_FILE = "src/integTest/resources/documents/test.doc";
  private static final String OUTPUT_DIR =
      "test-output/" + SourceDocumentSpecsFromInputStreamTest.class.getSimpleName() + "/";

  /** Ensures we start with a fresh output directory. */
  @BeforeClass
  public static void createOutputDir() {

    // Ensure we start with a fresh output directory
    final File outputDir = new File(OUTPUT_DIR);
    FileUtils.deleteQuietly(outputDir);
    outputDir.mkdirs();
  }

  /**  Deletes the output directory. */
  @AfterClass
  public static void deleteOutputDir() {

    // Delete the output directory
    FileUtils.deleteQuietly(new File(OUTPUT_DIR));
  }

  @Test(expected = NullPointerException.class)
  public void ctor_WithNullInputStream_ThrowsNullPointerException() throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "doc");
    tempFile.deleteOnExit();
    new SourceDocumentSpecsFromInputStream(null, tempFile, true);
  }

  @Test
  public void getFile_IOExceptionCatch_ThrowsDocumentSpecsIOException() throws IOException {

    // Create a temp directory and use it as temp file to force an IOException.
    final File tempFile = new File("test-output/" + getClass().getName() + "/out");
    tempFile.mkdirs();

    try (final FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, tempFile, false);

      try {
        specs.getFile();
      } catch (Exception e) {
        assertThat(e).isInstanceOf(DocumentSpecsIOException.class);
        assertThat(e).hasCauseInstanceOf(IOException.class);
      }
    }
  }

  @Test
  public void onConsumed_CloseStreamTrue_ShouldDeleteTempFileAndCloseInputStream()
      throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "doc");
    tempFile.deleteOnExit();
    assertThat(tempFile).exists();

    try (final FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, tempFile, true);

      specs.onConsumed(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the InputStream is closed.
      assertThat((Object) inputStream).hasFieldOrPropertyWithValue("closed", true);
    }
  }

  @Test
  public void onConsumed_CloseStreamFalse_ShouldDeleteTempFileAndNotCloseInputStream()
      throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "doc");
    tempFile.deleteOnExit();
    assertThat(tempFile).exists();

    try (final FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, tempFile, false);

      specs.onConsumed(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the InputStream is closed.
      assertThat((Object) inputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }
}
