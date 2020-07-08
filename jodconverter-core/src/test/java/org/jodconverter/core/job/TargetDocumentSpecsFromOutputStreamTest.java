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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.TemporaryFileMaker;

/** Contains tests for the {@link TargetDocumentSpecsFromOutputStream} class. */
@SuppressWarnings({"PMD.AvoidFileStream", "PMD.CloseResource"})
class TargetDocumentSpecsFromOutputStreamTest {

  @Nested
  class GetFile {

    @Test
    void withFormat_ShouldCreateTempFileWithExtension(@TempDir final File testFolder)
        throws IOException {

      final File tempFile = new File(testFolder, "temp.txt");
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile("txt")).willReturn(tempFile);

      final FileOutputStream outputStream = mock(FileOutputStream.class);
      doThrow(IOException.class)
          .when(outputStream)
          .write(isA(byte[].class), isA(int.class), isA(int.class));

      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.TXT);

      assertThat(specs.getFile()).isEqualTo(tempFile);
    }

    @Test
    void withoutFormat_ShouldCreateTempFileWithoutExtension(@TempDir final File testFolder)
        throws IOException {

      final File tempFile = new File(testFolder, "temp");
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(tempFile);

      final FileOutputStream outputStream = mock(FileOutputStream.class);
      doThrow(IOException.class)
          .when(outputStream)
          .write(isA(byte[].class), isA(int.class), isA(int.class));

      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

      assertThat(specs.getFile()).isEqualTo(tempFile);
    }
  }

  @Nested
  class OnComplete {

    @Test
    void whenIOExceptionOccurs_ShouldThrowDocumentSpecsIoException(@TempDir final File testFolder)
        throws IOException {

      final File tempFile = new File(testFolder, "temp.txt");
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);

      final FileOutputStream outputStream = mock(FileOutputStream.class);
      doThrow(IOException.class)
          .when(outputStream)
          .write(isA(byte[].class), isA(int.class), isA(int.class));

      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);
      assertThatExceptionOfType(DocumentSpecsIOException.class)
          .isThrownBy(() -> specs.onComplete(tempFile))
          .withMessageStartingWith("Could not write file")
          .withCauseInstanceOf(IOException.class);
    }

    @Test
    void whenCloseStreamIsTrue_ShouldDeleteTempFileAndCloseOutputStream(
        @TempDir final File testFolder) throws IOException {

      final File tempFile = new File(testFolder, "temp.txt");
      assertThat(tempFile.createNewFile()).isTrue();
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);

      try (FileOutputStream outputStream =
          new FileOutputStream(new File(testFolder, "target.txt"))) {
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
    void whenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseOutputStream(
        @TempDir final File testFolder) throws IOException {

      final File tempFile = new File(testFolder, "temp.txt");
      assertThat(tempFile.createNewFile()).isTrue();
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);

      try (FileOutputStream outputStream =
          new FileOutputStream(new File(testFolder, "target.txt"))) {
        final TargetDocumentSpecsFromOutputStream specs =
            new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

        specs.onComplete(tempFile);

        // Check that the temp file is deleted
        assertThat(tempFile).doesNotExist();

        // Check that the OutputStream is not closed.
        assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", false);
      }
    }
  }

  @Nested
  class OnFailure {

    @Test
    void whenCloseStreamIsTrue_ShouldDeleteTempFileAndNotCloseOutputStream(
        @TempDir final File testFolder) throws IOException {

      final File tempFile = new File(testFolder, "temp.txt");
      assertThat(tempFile.createNewFile()).isTrue();
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);

      try (FileOutputStream outputStream =
          new FileOutputStream(new File(testFolder, "target.txt"))) {
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
    void whenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseOutputStream(
        @TempDir final File testFolder) throws IOException {

      final File tempFile = new File(testFolder, "temp.txt");
      assertThat(tempFile.createNewFile()).isTrue();
      final TemporaryFileMaker fileMaker = mock(TemporaryFileMaker.class);

      try (FileOutputStream outputStream =
          new FileOutputStream(new File(testFolder, "target.txt"))) {
        final TargetDocumentSpecsFromOutputStream specs =
            new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

        specs.onFailure(tempFile, new IOException());

        // Check that the temp file is deleted
        assertThat(tempFile).doesNotExist();

        // Check that the OutputStream is not closed.
        assertThat(outputStream).hasFieldOrPropertyWithValue("closed", false);
      }
    }
  }
}
