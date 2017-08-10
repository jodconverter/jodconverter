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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

class TargetDocumentSpecsFromOutputStream extends AbstractTargetDocumentSpecs
    implements TargetDocumentSpecs {

  private final OutputStream outputStream;
  private final boolean closeStream;

  public TargetDocumentSpecsFromOutputStream(
      final OutputStream outputStream, final File tempFile, final boolean closeStream) {
    super(tempFile);

    Validate.notNull(outputStream, "The outputStream is null");
    this.outputStream = outputStream;
    this.closeStream = closeStream;
  }

  @Override
  public void onComplete(final File tempFile) {

    // Copy the content of the tempFile, which is the result
    // of the conversion, to the outputStream
    try {
      FileUtils.copyFile(tempFile, outputStream);
      if (closeStream) {
        IOUtils.closeQuietly(outputStream);
      }

    } catch (IOException ex) {
      throw new DocumentSpecsIOException("Could not write file '" + tempFile + "' to stream", ex);
    }
  }

  @Override
  public void onFailure(final File tempFile, final Exception ex) {

    // Ensure the created tempFile is deleted
    FileUtils.deleteQuietly(tempFile);
  }
}
