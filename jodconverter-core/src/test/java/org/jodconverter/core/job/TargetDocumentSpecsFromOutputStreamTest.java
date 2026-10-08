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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.TemporaryFileMaker;

/** Contains tests for the {@link TargetDocumentSpecsFromOutputStream} class. */
@SuppressWarnings("PMD.AvoidFileStream")
class TargetDocumentSpecsFromOutputStreamTest {

  @Nested
  class GetFile {

    @Test
    void withFormat_ShouldCreateTempFileWithExtension(@TempDir final File testFolder)
        throws IOException {

      final var tempFile = new File(testFolder, "temp.txt");
      final var fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile("txt")).willReturn(tempFile);

      final var outputStream = mock(FileOutputStream.class);
      doThrow(IOException.class)
          .when(outputStream)
          .write(isA(byte[].class), isA(int.class), isA(int.class));

      final var specs = new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.TXT);

      assertThat(specs.getFile()).isEqualTo(tempFile);
    }

    @Test
    void whenCalledTwice_ShouldReturnTheSameFile(@TempDir final File testFolder)
        throws IOException {

      final var fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile())
          .willReturn(new File(testFolder, "temp1"), new File(testFolder, "temp2"));
      final var specs =
          new TargetDocumentSpecsFromOutputStream(new ByteArrayOutputStream(), fileMaker, false);

      final var file = specs.getFile();

      assertThat(specs.getFile()).isEqualTo(file);

      // Once completed, or failed, the next conversion gets a file of its own.
      assertThat(file.createNewFile()).isTrue();
      specs.onComplete(file);
      final var next = specs.getFile();
      assertThat(next).isEqualTo(new File(testFolder, "temp2"));
      given(fileMaker.makeTemporaryFile()).willReturn(new File(testFolder, "temp3"));
      specs.onFailure(next, new IOException("failed"));
      assertThat(specs.getFile()).isEqualTo(new File(testFolder, "temp3"));
    }

    @Test
    void withoutFormat_ShouldCreateTempFileWithoutExtension(@TempDir final File testFolder)
        throws IOException {

      final var tempFile = new File(testFolder, "temp");
      final var fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(tempFile);

      final var outputStream = mock(FileOutputStream.class);
      doThrow(IOException.class)
          .when(outputStream)
          .write(isA(byte[].class), isA(int.class), isA(int.class));

      final var specs = new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

      assertThat(specs.getFile()).isEqualTo(tempFile);
    }
  }

  @Nested
  class OnComplete {

    @Test
    void whenIOExceptionOccurs_ShouldThrowDocumentSpecsIoException(@TempDir final File testFolder)
        throws IOException {

      final var tempFile = new File(testFolder, "temp.txt");
      final var fileMaker = mock(TemporaryFileMaker.class);

      final var outputStream = mock(FileOutputStream.class);
      doThrow(IOException.class)
          .when(outputStream)
          .write(isA(byte[].class), isA(int.class), isA(int.class));

      final var specs = new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);
      assertThatExceptionOfType(DocumentSpecsIOException.class)
          .isThrownBy(() -> specs.onComplete(tempFile))
          .withMessageStartingWith("Could not write file")
          .withCauseInstanceOf(IOException.class);
    }

    @Test
    void whenCloseStreamIsTrue_ShouldDeleteTempFileAndCloseOutputStream(
        @TempDir final File testFolder) throws IOException {

      final var tempFile = new File(testFolder, "temp.txt");
      assertThat(tempFile.createNewFile()).isTrue();
      final var fileMaker = mock(TemporaryFileMaker.class);

      try (var outputStream =
          new CloseTrackingOutputStream(new FileOutputStream(new File(testFolder, "target.txt")))) {
        final var specs = new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, true);

        specs.onComplete(tempFile);

        // Check that the temp file is deleted
        assertThat(tempFile).doesNotExist();

        // Check that the OutputStream is closed.
        assertThat(outputStream.closed).isTrue();
      }
    }

    @Test
    void whenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseOutputStream(
        @TempDir final File testFolder) throws IOException {

      final var tempFile = new File(testFolder, "temp.txt");
      assertThat(tempFile.createNewFile()).isTrue();
      final var fileMaker = mock(TemporaryFileMaker.class);

      try (var outputStream =
          new CloseTrackingOutputStream(new FileOutputStream(new File(testFolder, "target.txt")))) {
        final var specs = new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

        specs.onComplete(tempFile);

        // Check that the temp file is deleted
        assertThat(tempFile).doesNotExist();

        // Check that the OutputStream is not closed.
        assertThat(outputStream.closed).isFalse();
      }
    }
  }

  @Nested
  class OnFailure {

    @Test
    void whenCloseStreamIsTrue_ShouldDeleteTempFileAndNotCloseOutputStream(
        @TempDir final File testFolder) throws IOException {

      final var tempFile = new File(testFolder, "temp.txt");
      assertThat(tempFile.createNewFile()).isTrue();
      final var fileMaker = mock(TemporaryFileMaker.class);

      try (var outputStream =
          new CloseTrackingOutputStream(new FileOutputStream(new File(testFolder, "target.txt")))) {
        final var specs = new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, true);

        specs.onFailure(tempFile, new IOException());

        // Check that the temp file is deleted
        assertThat(tempFile).doesNotExist();

        // Check that the OutputStream is not closed.
        assertThat(outputStream.closed).isFalse();
      }
    }

    @Test
    void whenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseOutputStream(
        @TempDir final File testFolder) throws IOException {

      final var tempFile = new File(testFolder, "temp.txt");
      assertThat(tempFile.createNewFile()).isTrue();
      final var fileMaker = mock(TemporaryFileMaker.class);

      try (var outputStream =
          new CloseTrackingOutputStream(new FileOutputStream(new File(testFolder, "target.txt")))) {
        final var specs = new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

        specs.onFailure(tempFile, new IOException());

        // Check that the temp file is deleted
        assertThat(tempFile).doesNotExist();

        // Check that the OutputStream is not closed.
        assertThat(outputStream.closed).isFalse();
      }
    }
  }

  // Records whether the stream was closed. Reading the private "closed" field of
  // FileOutputStream is not allowed since Java 17 (strong encapsulation of the JDK internals).
  private static final class CloseTrackingOutputStream extends FilterOutputStream {

    private boolean closed;

    /* default */ CloseTrackingOutputStream(final OutputStream out) {
      super(out);
    }

    @Override
    public void close() throws IOException {
      closed = true;
      super.close();
    }
  }
}
