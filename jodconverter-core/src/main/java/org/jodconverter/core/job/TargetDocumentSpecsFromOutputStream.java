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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.office.TemporaryFileMaker;

/** Target document specifications for from an input stream. */
public class TargetDocumentSpecsFromOutputStream extends AbstractTargetDocumentSpecs
    implements TargetDocumentSpecs {

  private final OutputStream outputStream;
  private final boolean closeStream;
  private final TemporaryFileMaker fileMaker;

  /**
   * Creates specs for the specified output stream.
   *
   * @param outputStream The target output stream.
   * @param fileMaker Temporary file maker.
   * @param closeStream If we close the stream on completion.
   */
  public TargetDocumentSpecsFromOutputStream(
      @NonNull final OutputStream outputStream,
      @NonNull final TemporaryFileMaker fileMaker,
      final boolean closeStream) {
    super(fileMaker.makeTemporaryFile());

    Validate.notNull(outputStream, "outputStream must not be null");
    Validate.notNull(fileMaker, "fileMaker must not be null");
    this.outputStream = outputStream;
    this.closeStream = closeStream;
    this.fileMaker = fileMaker;
  }

  @NonNull
  @Override
  public File getFile() {

    return Optional.ofNullable(getFormat())
        .map(format -> fileMaker.makeTemporaryFile(format.getExtension()))
        .orElse(super.getFile());
  }

  @Override
  public void onComplete(@NonNull final File tempFile) {

    // Copy the content of the tempFile, which is the result
    // of the conversion, to the outputStream
    try {
      FileUtils.copyFile(tempFile, outputStream);
      if (closeStream) {
        outputStream.close();
      }

    } catch (IOException ex) {
      throw new DocumentSpecsIOException("Could not write file '" + tempFile + "' to stream", ex);
    } finally {

      // Ensure the created tempFile is deleted
      FileUtils.deleteQuietly(tempFile);
    }
  }

  @Override
  public void onFailure(@NonNull final File tempFile, @NonNull final Exception exception) {

    // Ensure the created tempFile is deleted
    FileUtils.deleteQuietly(tempFile);
  }
}
