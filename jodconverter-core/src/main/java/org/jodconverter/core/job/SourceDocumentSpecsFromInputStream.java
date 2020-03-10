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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.office.TemporaryFileMaker;
import org.jodconverter.core.util.AssertUtils;

/** Source document specifications for from an input stream. */
public class SourceDocumentSpecsFromInputStream extends AbstractSourceDocumentSpecs
    implements SourceDocumentSpecs {

  private final InputStream inputStream;
  private final TemporaryFileMaker fileMaker;
  private final boolean closeStream;

  /**
   * Creates specs from the specified stream.
   *
   * @param inputStream The source stream.
   * @param fileMaker Temporary file maker.
   * @param closeStream If we close the stream on completion.
   */
  public SourceDocumentSpecsFromInputStream(
      @NonNull final InputStream inputStream,
      @NonNull final TemporaryFileMaker fileMaker,
      final boolean closeStream) {
    super(fileMaker.makeTemporaryFile());

    AssertUtils.notNull(inputStream, "inputStream must not be null");
    AssertUtils.notNull(fileMaker, "fileMaker must not be null");
    this.inputStream = inputStream;
    this.fileMaker = fileMaker;
    this.closeStream = closeStream;
  }

  @NonNull
  @Override
  public File getFile() {

    // Write the InputStream to the temp file
    final File tempFile =
        Optional.ofNullable(getFormat())
            .map(format -> fileMaker.makeTemporaryFile(format.getExtension()))
            .orElse(super.getFile());
    try {
      final FileOutputStream outputStream = new FileOutputStream(tempFile);
      outputStream.getChannel().lock();
      try {
        IOUtils.copy(inputStream, outputStream);
        return tempFile;
      } finally {
        // Note: This will implicitly release the file lock.
        outputStream.close();
      }
    } catch (IOException ex) {
      throw new DocumentSpecsIOException("Could not write stream to file " + tempFile, ex);
    }
  }

  @Override
  public void onConsumed(@NonNull final File tempFile) {

    // The temporary file must be deleted
    FileUtils.deleteQuietly(tempFile);

    if (closeStream) {
      try {
        inputStream.close();
      } catch (IOException ex) {
        throw new DocumentSpecsIOException("Could not close input stream", ex);
      }
    }
  }
}
