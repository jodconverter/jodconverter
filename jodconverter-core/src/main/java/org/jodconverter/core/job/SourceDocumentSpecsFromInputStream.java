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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.office.TemporaryFileMaker;
import org.jodconverter.core.util.FileUtils;

/** Source document specifications for from an input stream. */
public class SourceDocumentSpecsFromInputStream extends AbstractSourceDocumentSpecs {

  private final InputStream inputStream;
  private final TemporaryFileMaker fileMaker;
  private final boolean closeStream;

  // The file the stream was written to, for the duration of a conversion.
  private File tempFile;

  /**
   * Creates specs from the specified stream.
   *
   * @param inputStream The source stream.
   * @param fileMaker Temporary file maker.
   * @param closeStream If we close the stream on completion.
   */
  public SourceDocumentSpecsFromInputStream(
      final @NonNull InputStream inputStream,
      final @NonNull TemporaryFileMaker fileMaker,
      final boolean closeStream) {
    super();

    Objects.requireNonNull(inputStream, "inputStream must not be null");
    Objects.requireNonNull(fileMaker, "fileMaker must not be null");
    this.inputStream = inputStream;
    this.fileMaker = fileMaker;
    this.closeStream = closeStream;
  }

  @Override
  public @NonNull File getFile() {

    // The stream can only be read once: the first call writes it to the temp file.
    if (tempFile == null) {
      final var file =
          Optional.ofNullable(getFormat())
              .map(format -> fileMaker.makeTemporaryFile(format.getExtension()))
              .orElseGet(fileMaker::makeTemporaryFile);
      try (var outputStream = Files.newOutputStream(file.toPath())) {
        inputStream.transferTo(outputStream);
      } catch (IOException ex) {
        throw new DocumentSpecsIOException(
            String.format("Could not write stream to file '%s'", file), ex);
      }
      tempFile = file;
    }
    return tempFile;
  }

  @Override
  public void onConsumed(final @NonNull File tempFile) {

    // The temporary file must be deleted
    FileUtils.deleteQuietly(tempFile);
    this.tempFile = null;

    if (closeStream) {
      try {
        inputStream.close();
      } catch (IOException ex) {
        throw new DocumentSpecsIOException("Could not close input stream", ex);
      }
    }
  }
}
