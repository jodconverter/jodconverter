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

import java.io.*;
import java.nio.charset.StandardCharsets;
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

      final var tempFile = new File(testFolder, "temp.txt");
      final var fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile("txt")).willReturn(tempFile);

      final var sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (var inputStream = Files.newInputStream(sourceFile.toPath())) {
        final var specs = new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);
        specs.setDocumentFormat(DefaultDocumentFormatRegistry.TXT);
        assertThat(specs.getFile()).isEqualTo(tempFile);
      }
    }

    @Test
    void whenCalledTwice_ShouldReturnTheSameFile(@TempDir final File testFolder)
        throws IOException {

      final var fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile())
          .willReturn(new File(testFolder, "temp1"), new File(testFolder, "temp2"));
      try (var inputStream = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8))) {
        final var specs = new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);

        final var file = specs.getFile();

        // The stream can only be written once: the second call gives the same file.
        assertThat(specs.getFile()).isEqualTo(file);
        assertThat(file).hasContent("content");

        // Once consumed, the next conversion gets a file of its own.
        specs.onConsumed(file);
        assertThat(specs.getFile()).isEqualTo(new File(testFolder, "temp2"));
      }
    }

    @Test
    void withoutFormat_ShouldCreateTempFileWithoutExtension(@TempDir final File testFolder)
        throws IOException {

      final var tempFile = new File(testFolder, "temp");
      final var fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(tempFile);

      final var sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (var inputStream = Files.newInputStream(sourceFile.toPath())) {
        final var specs = new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);
        assertThat(specs.getFile()).isEqualTo(tempFile);
      }
    }

    @Test
    void whenIoExceptionOccurs_ShouldThrowDocumentSpecsIoException(@TempDir final File testFolder)
        throws IOException {

      // FileOutputStream will fail with an IOException
      final var fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(testFolder);

      final var sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (var inputStream = Files.newInputStream(sourceFile.toPath())) {
        final var specs = new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);

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

      final var tempFile = new File(testFolder, "temp");
      final var fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(tempFile);

      final var inputStream = mock(FileInputStream.class);
      doThrow(IOException.class).when(inputStream).close();

      final var specs = new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, true);

      assertThatExceptionOfType(DocumentSpecsIOException.class)
          .isThrownBy(() -> specs.onConsumed(tempFile))
          .withMessage("Could not close input stream")
          .withCauseInstanceOf(IOException.class);
    }

    @Test
    void whenCloseStreamIsTrue_ShouldDeleteTempFileAndCloseInputStream(
        @TempDir final File testFolder) throws IOException {

      final var tempFile = new File(testFolder, "temp");
      final var fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(tempFile);

      final var sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (var inputStream = new CloseTrackingInputStream(new FileInputStream(sourceFile))) {
        final var specs = new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, true);

        specs.onConsumed(tempFile);

        // Check that the temp file is deleted
        assertThat(tempFile).doesNotExist();

        // Check that the InputStream is closed.
        assertThat(inputStream.closed).isTrue();
      }
    }

    @Test
    void whenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseInputStream(
        @TempDir final File testFolder) throws IOException {

      final var tempFile = new File(testFolder, "temp");
      final var fileMaker = mock(TemporaryFileMaker.class);
      given(fileMaker.makeTemporaryFile()).willReturn(tempFile);

      final var sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (var inputStream = new CloseTrackingInputStream(new FileInputStream(sourceFile))) {
        final var specs = new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);

        specs.onConsumed(tempFile);

        // Check that the temp file is deleted
        assertThat(tempFile).doesNotExist();

        // Check that the InputStream is not closed.
        assertThat(inputStream.closed).isFalse();
      }
    }
  }

  // Records whether the stream was closed. Reading the private "closed" field of
  // FileInputStream is not allowed since Java 17 (strong encapsulation of the JDK internals).
  private static final class CloseTrackingInputStream extends FilterInputStream {

    private boolean closed;

    /* default */ CloseTrackingInputStream(final InputStream in) {
      super(in);
    }

    @Override
    public void close() throws IOException {
      closed = true;
      super.close();
    }
  }
}
