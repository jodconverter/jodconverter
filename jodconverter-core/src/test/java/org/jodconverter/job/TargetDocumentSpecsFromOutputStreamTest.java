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
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class TargetDocumentSpecsFromOutputStreamTest {

  private static final String SOURCE_FILE = "src/integTest/resources/documents/test.doc";
  private static final String OUTPUT_DIR =
      "test-output/" + SourceDocumentSpecsFromInputStreamTest.class.getSimpleName() + "/";
  private static final String TARGET_FILE = OUTPUT_DIR + "test.doc";

  @Test(expected = NullPointerException.class)
  public void ctor_WithNullOutputStream_ThrowsNullPointerException() throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "pdf");
    tempFile.deleteOnExit();
    new TargetDocumentSpecsFromOutputStream(null, tempFile, true);
  }

  @Test
  public void onComplete_IOExceptionCatch_ThrowsDocumentSpecsIOException() throws IOException {

    final FileOutputStream outputStream = new FileOutputStream(TARGET_FILE);
    outputStream.close();

    final File tempFile = File.createTempFile(getClass().getName(), "doc");
    tempFile.deleteOnExit();

    final TargetDocumentSpecsFromOutputStream specs =
        new TargetDocumentSpecsFromOutputStream(outputStream, tempFile, false);

    try {
      specs.onComplete(tempFile);
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DocumentSpecsIOException.class);
      assertThat(e).hasCauseInstanceOf(IOException.class);
    }
  }

  @Test
  public void onComplete_CloseStreamTrue_ShouldDeleteTempFileAndCloseOutputStream()
      throws IOException {

    final File tempFile = File.createTempFile(getClass().getName(), "doc");
    tempFile.deleteOnExit();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();

    try (final FileOutputStream outputStream = new FileOutputStream(TARGET_FILE)) {
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

    final File tempFile = File.createTempFile(getClass().getName(), "doc");
    tempFile.deleteOnExit();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();

    try (final FileOutputStream outputStream = new FileOutputStream(TARGET_FILE)) {
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

    final File tempFile = File.createTempFile(getClass().getName(), "doc");
    tempFile.deleteOnExit();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();

    try (final FileOutputStream outputStream = new FileOutputStream(TARGET_FILE)) {
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

    final File tempFile = File.createTempFile(getClass().getName(), "doc");
    tempFile.deleteOnExit();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();

    try (final FileOutputStream outputStream = new FileOutputStream(TARGET_FILE)) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, tempFile, false);

      specs.onFailure(tempFile, new IOException());

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is not closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }
}
