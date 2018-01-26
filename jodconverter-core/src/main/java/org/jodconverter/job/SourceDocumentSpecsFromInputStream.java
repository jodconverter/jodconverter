/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import org.jodconverter.office.TemporaryFileMaker;

class SourceDocumentSpecsFromInputStream extends AbstractSourceDocumentSpecs
    implements SourceDocumentSpecs {

  private final InputStream inputStream;
  private final boolean closeStream;
  private final TemporaryFileMaker fileMaker;

  public SourceDocumentSpecsFromInputStream(
      final InputStream inputStream,
      final TemporaryFileMaker fileMaker,
      final boolean closeStream) {
    super(fileMaker.makeTemporaryFile());

    Validate.notNull(inputStream, "The inputStream is null");
    this.inputStream = inputStream;
    this.closeStream = closeStream;
    this.fileMaker = fileMaker;
  }

  @Override
  public File getFile() {

    // Write the InputStream to the temp file
    final File tempFile =
        Optional.ofNullable(getFormat())
            .map(format -> fileMaker.makeTemporaryFile(format.getExtension()))
            .orElse(super.getFile());
    try {
      final FileOutputStream outputStream = new FileOutputStream(tempFile); // NOSONAR
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
  public void onConsumed(final File tempFile) {

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
