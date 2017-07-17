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
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;

import org.jodconverter.document.DocumentFormat;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.TemporaryFileMaker;

/**
 * Base class for all job with source specified implementations.
 *
 * @see ConversionJobWithSourceSpecified
 */
public abstract class AbstractConversionJobWithSourceSpecified
    implements ConversionJobWithSourceSpecified {

  private static final boolean DEFAULT_CLOSE_STREAM = true;

  protected final SourceDocumentSpecs source;
  protected final OfficeManager officeManager;
  protected final DocumentFormatRegistry formatRegistry;

  protected AbstractConversionJobWithSourceSpecified(
      final SourceDocumentSpecs source,
      final OfficeManager officeManager,
      final DocumentFormatRegistry formatRegistry) {
    super();

    this.source = source;
    this.officeManager = officeManager;
    this.formatRegistry = formatRegistry;
  }

  @Override
  public ConversionJob to(final File target) {

    final DocumentFormat format =
        formatRegistry.getFormatByExtension(FilenameUtils.getExtension(target.getName()));
    Validate.notNull(target, "Unsupported target document format");
    return to(new TargetDocumentSpecsFromFile(target, format));
  }

  @Override
  public ConversionJob to(final File target, final DocumentFormat format) {

    Validate.notNull(format, "The document format is null");
    return to(new TargetDocumentSpecsFromFile(target, format));
  }

  @Override
  public ConversionJob to(final OutputStream target, final DocumentFormat format) {

    return to(target, format, DEFAULT_CLOSE_STREAM);
  }

  @Override
  public ConversionJob to(
      final OutputStream target, final DocumentFormat format, final boolean closeStream) {

    Validate.notNull(format, "The document format is null");
    if (officeManager instanceof TemporaryFileMaker) {
      return to(
          new TargetDocumentSpecsFromOutputStream(
              target,
              format,
              ((TemporaryFileMaker) officeManager).makeTemporaryFile(format.getExtension()),
              closeStream));
    }
    throw new IllegalStateException(
        "An office manager must implements the TemporaryFileMaker "
            + "interface in order to be able to convert to output streams");
  }
}
