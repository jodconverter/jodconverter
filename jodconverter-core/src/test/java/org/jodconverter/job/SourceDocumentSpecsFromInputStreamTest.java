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
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.star.document.UpdateDocMode;

import org.jodconverter.document.DefaultDocumentFormatRegistry;

@SuppressWarnings({
  "PMD.AvoidCatchingGenericException",
  "PMD.LawOfDemeter",
  "PMD.UseConcurrentHashMap"
})
public class SourceDocumentSpecsFromInputStreamTest {

  private static final String TEST_OUTPUT_DIR = "build/test-results/";
  private static final String SOURCE_FILE = "src/test/resources/documents/test.txt";

  private static File outputDir;

  /** Creates an output test directory just once. */
  @BeforeClass
  public static void setUpClass() {

    outputDir =
        new File(TEST_OUTPUT_DIR, SourceDocumentSpecsFromInputStreamTest.class.getSimpleName());
    outputDir.mkdirs();
  }

  /** Deletes the output test directory once the tests are all done. */
  @AfterClass
  public static void tearDownClass() {

    FileUtils.deleteQuietly(outputDir);
  }

  @Test(expected = NullPointerException.class)
  public void ctor_WithNullInputStream_ThrowsNullPointerException() throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "doc");
    tempFile.deleteOnExit();
    new SourceDocumentSpecsFromInputStream(null, tempFile, true);
  }

  @Test
  public void getFile_IoExceptionCatch_ThrowsDocumentSpecsIoException() throws IOException {

    try (final FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, outputDir, false);

      try {
        specs.getFile();
        fail("getFile should throw DocumentSpecsIOException");
      } catch (Exception e) {
        assertThat(e).isInstanceOf(DocumentSpecsIOException.class);
        assertThat(e).hasCauseInstanceOf(IOException.class);
      }
    }
  }

  @Test
  public void onConsumed_IoExceptionCatch_ThrowsDocumentSpecsIoException() throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "doc");
    tempFile.deleteOnExit();
    assertThat(tempFile).exists();

    final FileInputStream inputStream = mock(FileInputStream.class);
    doThrow(IOException.class).when(inputStream).close();

    final SourceDocumentSpecsFromInputStream specs =
        new SourceDocumentSpecsFromInputStream(inputStream, tempFile, true);

    try {
      specs.onConsumed(tempFile);
      fail("onConsumed should throw DocumentSpecsIOException");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DocumentSpecsIOException.class);
      assertThat(e).hasCauseInstanceOf(IOException.class);
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

  @Test
  public void ctor_WithValidValues_SpecsCreatedWithExpectedValues() throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "doc");
    tempFile.deleteOnExit();
    assertThat(tempFile).exists();

    try (final FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {

      final Map<String, Object> loadProperties = new HashMap<>();
      loadProperties.put("UpdateDocMode", UpdateDocMode.ACCORDING_TO_CONFIG);

      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, tempFile, false);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.ODS);
      specs.setCustomLoadProperties(loadProperties);

      assertThat(specs)
          .extracting("inputStream", "documentFormat", "customLoadProperties")
          .containsExactly(inputStream, DefaultDocumentFormatRegistry.ODS, loadProperties);
    }
  }
}
