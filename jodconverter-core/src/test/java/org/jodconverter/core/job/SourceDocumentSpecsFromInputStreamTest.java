/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.TemporaryFileMaker;

/** Contains tests for the {@link SourceDocumentSpecsFromInputStream} class. */
@SuppressWarnings({"PMD.AvoidFileStream", "PMD.CloseResource"})
class SourceDocumentSpecsFromInputStreamTest {

  @Nested
  class GetFile {

    @Test
    void withFormat_ShouldCreateTempFileWithExtension(@TempDir final File testFolder)
        throws IOException {

      final File tempFile = new File(testFolder, "temp.txt");
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile("txt")).willReturn(tempFile);

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (InputStream inputStream = Files.newInputStream(sourceFile.toPath())) {
        final SourceDocumentSpecsFromInputStream specs =
            new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);
        specs.setDocumentFormat(DefaultDocumentFormatRegistry.TXT);
        assertThat(specs.getFile()).isEqualTo(tempFile);
      }
    }

    @Test
    void withoutFormat_ShouldCreateTempFileWithoutExtension(@TempDir final File testFolder)
        throws IOException {

      final File tempFile = new File(testFolder, "temp");
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(tempFile);

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (InputStream inputStream = Files.newInputStream(sourceFile.toPath())) {
        final SourceDocumentSpecsFromInputStream specs =
            new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);
        assertThat(specs.getFile()).isEqualTo(tempFile);
      }
    }

    @Test
    void whenIoExceptionOccurs_ShouldThrowDocumentSpecsIoException(@TempDir final File testFolder)
        throws IOException {

      // FileOutputStream will fail with an IOException
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(testFolder);

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (InputStream inputStream = Files.newInputStream(sourceFile.toPath())) {
        final SourceDocumentSpecsFromInputStream specs =
            new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);

        assertThatExceptionOfType(DocumentSpecsIOException.class)
            .isThrownBy(specs::getFile)
            .withMessageStartingWith("Could not write stream to file")
            .withCauseInstanceOf(IOException.class);
      }
    }
  }

  @Nested
  class OnConsume {

    @Test
    void whenIoExceptionOccurs_ShouldThrowDocumentSpecsIoException(@TempDir final File testFolder)
        throws IOException {

      final File tempFile = new File(testFolder, "temp");
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(tempFile);

      final FileInputStream inputStream = mock(FileInputStream.class);
      doThrow(IOException.class).when(inputStream).close();

      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, true);

      assertThatExceptionOfType(DocumentSpecsIOException.class)
          .isThrownBy(() -> specs.onConsumed(tempFile))
          .withMessage("Could not close input stream")
          .withCauseInstanceOf(IOException.class);
    }

    @Test
    void whenCloseStreamIsTrue_ShouldDeleteTempFileAndCloseInputStream(
        @TempDir final File testFolder) throws IOException {

      final File tempFile = new File(testFolder, "temp");
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(tempFile);

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (FileInputStream inputStream = new FileInputStream(sourceFile)) {
        final SourceDocumentSpecsFromInputStream specs =
            new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, true);

        specs.onConsumed(tempFile);

        // Check that the temp file is deleted
        assertThat(tempFile).doesNotExist();

        // Check that the InputStream is closed.
        assertThat((Object) inputStream).hasFieldOrPropertyWithValue("closed", true);
      }
    }

    @Test
    void whenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseInputStream(
        @TempDir final File testFolder) throws IOException {

      final File tempFile = new File(testFolder, "temp");
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(tempFile);

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (FileInputStream inputStream = new FileInputStream(sourceFile)) {
        final SourceDocumentSpecsFromInputStream specs =
            new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);

        specs.onConsumed(tempFile);

        // Check that the temp file is deleted
        assertThat(tempFile).doesNotExist();

        // Check that the InputStream is closed.
        assertThat((Object) inputStream).hasFieldOrPropertyWithValue("closed", false);
      }
    }
  }
}
