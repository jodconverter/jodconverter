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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.TemporaryFileMaker;

/** Contains tests for the {@link TargetDocumentSpecsFromOutputStream} class. */
public class TargetDocumentSpecsFromOutputStreamTest {

  private static final String SOURCE_FILE = "src/test/resources/documents/test.txt";
  private static final String TARGET_FILENAME = "test.pdf";

  /* default */ @TempDir File testFolder;
  private TemporaryFileMaker fileMaker;

  /** Setup the file maker before each test. */
  @BeforeEach
  public void setUp() {

    fileMaker = mock(TemporaryFileMaker.class);
    given(fileMaker.makeTemporaryFile()).willReturn(new File(testFolder, "temp"));
  }

  @Test
  public void ctor_WithNullOutputStream_ThrowsNullPointerException() {

    assertThatNullPointerException()
        .isThrownBy(() -> new TargetDocumentSpecsFromOutputStream(null, fileMaker, true));
  }

  @Test
  public void onComplete_WhenIoExceptionCatch_ThrowsDocumentSpecsIoException() throws IOException {

    final File tempFile = new File(testFolder, "onComplete_WhenIoExceptionCatch.txt");
    assertThat(tempFile.createNewFile()).isTrue();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    final FileOutputStream outputStream = mock(FileOutputStream.class);
    doThrow(IOException.class)
        .when(outputStream)
        .write(isA(byte[].class), isA(int.class), isA(int.class));

    final TargetDocumentSpecsFromOutputStream specs =
        new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

    assertThatExceptionOfType(DocumentSpecsIOException.class)
        .isThrownBy(() -> specs.onComplete(tempFile))
        .withCauseInstanceOf(IOException.class);
  }

  @Test
  public void onComplete_WhenCloseStreamIsTrue_ShouldDeleteTempFileAndCloseOutputStream()
      throws IOException {

    final File tempFile = new File(testFolder, "onComplete_WhenCloseStreamIsTrue.txt");
    assertThat(tempFile.createNewFile()).isTrue();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileOutputStream outputStream =
        new FileOutputStream(new File(testFolder, TARGET_FILENAME))) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, true);

      specs.onComplete(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", true);
    }
  }

  @Test
  public void onConsumed_WhenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseOutputStream()
      throws IOException {

    final File tempFile = new File(testFolder, "onConsumed_WhenCloseStreamIsFalse.txt");
    assertThat(tempFile.createNewFile()).isTrue();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileOutputStream outputStream =
        new FileOutputStream(new File(testFolder, TARGET_FILENAME))) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

      specs.onComplete(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is not closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }

  @Test
  public void onFailure_WhenCloseStreamIsTrue_ShouldDeleteTempFileAndNotCloseOutputStream()
      throws IOException {

    final File tempFile = new File(testFolder, "onConsumed_WhenIoExceptionCatch.txt");
    assertThat(tempFile.createNewFile()).isTrue();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileOutputStream outputStream =
        new FileOutputStream(new File(testFolder, TARGET_FILENAME))) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, true);

      specs.onFailure(tempFile, new IOException());

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is not closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }

  @Test
  public void onFailure_WhenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseOutputStream()
      throws IOException {

    final File tempFile = new File(testFolder, "onFailure_WhenCloseStreamIsFalse.txt");
    assertThat(tempFile.createNewFile()).isTrue();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileOutputStream outputStream =
        new FileOutputStream(new File(testFolder, TARGET_FILENAME))) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

      specs.onFailure(tempFile, new IOException());

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is not closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }

  @Test
  public void ctor_WithValidValues_SpecsCreatedWithExpectedValues() throws IOException {

    final File tempFile = new File(testFolder, "ctor_WithValidValues.txt");
    assertThat(tempFile.createNewFile()).isTrue();
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (OutputStream outputStream =
        Files.newOutputStream(new File(testFolder, TARGET_FILENAME).toPath())) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.CSV);

      assertThat(specs)
          .extracting("outputStream", "documentFormat")
          .containsExactly(outputStream, DefaultDocumentFormatRegistry.CSV);
    }
  }
}
