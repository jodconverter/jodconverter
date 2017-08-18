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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.jodconverter.document.DefaultDocumentFormatRegistry;

@SuppressWarnings({
  "PMD.AvoidCatchingGenericException",
  "PMD.LawOfDemeter",
  "PMD.UseConcurrentHashMap"
})
public class TargetDocumentSpecsFromOutputStreamTest {

  private static final String TEST_OUTPUT_DIR = "build/test-results/";
  private static final String SOURCE_FILE = "src/test/resources/documents/test.txt";
  private static final String TARGET_FILENAME = "test.pdf";

  private static File outputDir;

  /** Creates an output test directory just once. */
  @BeforeClass
  public static void setUpClass() {

    outputDir =
        new File(TEST_OUTPUT_DIR, TargetDocumentSpecsFromOutputStreamTest.class.getSimpleName());
    outputDir.mkdirs();
  }

  /** Deletes the output test directory once the tests are all done. */
  @AfterClass
  public static void tearDownClass() {

    FileUtils.deleteQuietly(outputDir);
  }

  @Test(expected = NullPointerException.class)
  public void ctor_WithNullOutputStream_ThrowsNullPointerException() throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "pdf");
    tempFile.deleteOnExit();
    new TargetDocumentSpecsFromOutputStream(null, tempFile, true);
  }

  @Test
  public void onComplete_IoExceptionCatch_ThrowsDocumentSpecsIoException() throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "txt");
    tempFile.deleteOnExit();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();

    final FileOutputStream outputStream = mock(FileOutputStream.class);
    doThrow(IOException.class)
        .when(outputStream)
        .write(isA(byte[].class), isA(int.class), isA(int.class));

    final TargetDocumentSpecsFromOutputStream specs =
        new TargetDocumentSpecsFromOutputStream(outputStream, tempFile, false);

    try {
      specs.onComplete(tempFile);
      fail("onComplete should throw DocumentSpecsIOException");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DocumentSpecsIOException.class);
      assertThat(e).hasCauseInstanceOf(IOException.class);
    }
  }

  @Test
  public void onComplete_CloseStreamTrue_ShouldDeleteTempFileAndCloseOutputStream()
      throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "txt");
    tempFile.deleteOnExit();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();

    final File targetFile = new File(outputDir, TARGET_FILENAME);
    try (final FileOutputStream outputStream = new FileOutputStream(targetFile)) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, tempFile, true);

      specs.onComplete(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", true);
    }
  }

  @Test
  public void onConsumed_CloseStreamFalse_ShouldDeleteTempFileAndNotCloseOutputStream()
      throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "txt");
    tempFile.deleteOnExit();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();

    final File targetFile = new File(outputDir, TARGET_FILENAME);
    try (final FileOutputStream outputStream = new FileOutputStream(targetFile)) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, tempFile, false);

      specs.onComplete(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is not closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }

  @Test
  public void onFailure_CloseStreamTrue_ShouldDeleteTempFileAndNotCloseOutputStream()
      throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "txt");
    tempFile.deleteOnExit();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();

    final File targetFile = new File(outputDir, TARGET_FILENAME);
    try (final FileOutputStream outputStream = new FileOutputStream(targetFile)) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, tempFile, true);

      specs.onFailure(tempFile, new IOException());

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is not closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }

  @Test
  public void onFailure_CloseStreamFalse_ShouldDeleteTempFileAndNotCloseOutputStream()
      throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "txt");
    tempFile.deleteOnExit();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();

    final File targetFile = new File(outputDir, TARGET_FILENAME);
    try (final FileOutputStream outputStream = new FileOutputStream(targetFile)) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, tempFile, false);

      specs.onFailure(tempFile, new IOException());

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is not closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }

  @Test
  public void ctor_WithValidValues_SpecsCreatedWithExpectedValues() throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "txt");
    tempFile.deleteOnExit();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();

    final File targetFile = new File(outputDir, TARGET_FILENAME);
    try (final FileOutputStream outputStream = new FileOutputStream(targetFile)) {

      final Map<String, Object> storeProperties = new HashMap<>();
      storeProperties.put("Overwrite", true);
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, tempFile, false);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.CSV);
      specs.setCustomStoreProperties(storeProperties);

      assertThat(specs)
          .extracting("outputStream", "documentFormat", "customStoreProperties")
          .containsExactly(outputStream, DefaultDocumentFormatRegistry.CSV, storeProperties);
    }
  }
}
