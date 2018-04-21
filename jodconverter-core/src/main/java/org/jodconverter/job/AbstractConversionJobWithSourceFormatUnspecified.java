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
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;

import org.jodconverter.document.DocumentFormat;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.TemporaryFileMaker;

/**
 * Base class for all conversion job implementations with source format that is not yet applied to
 * the converter.
 *
 * @see ConversionJobWithOptionalSourceFormatUnspecified
 * @see ConversionJobWithRequiredSourceFormatUnspecified
 */
public abstract class AbstractConversionJobWithSourceFormatUnspecified
    implements ConversionJobWithOptionalSourceFormatUnspecified {

  private static final boolean DEFAULT_CLOSE_STREAM = true;

  protected final AbstractSourceDocumentSpecs source;
  protected final OfficeManager officeManager;
  protected final DocumentFormatRegistry formatRegistry;

  protected AbstractConversionJobWithSourceFormatUnspecified(
      final AbstractSourceDocumentSpecs source,
      final OfficeManager officeManager,
      final DocumentFormatRegistry formatRegistry) {
    super();

    this.source = source;
    this.officeManager = officeManager;
    this.formatRegistry = formatRegistry;
  }

  @Override
  public AbstractConversionJobWithSourceFormatUnspecified as(final DocumentFormat format) {

    source.setDocumentFormat(format);
    return this;
  }

  @Override
  public AbstractConversionJob to(final File target) {

    final TargetDocumentSpecsFromFile specs = new TargetDocumentSpecsFromFile(target);
    final DocumentFormat format =
        formatRegistry.getFormatByExtension(FilenameUtils.getExtension(target.getName()));
    if (format != null) {
      specs.setDocumentFormat(format);
    }

    return toInternal(specs);
  }

  @Override
  public AbstractConversionJob to(final OutputStream target) {

    return to(target, DEFAULT_CLOSE_STREAM);
  }

  @Override
  public AbstractConversionJob to(final OutputStream target, final boolean closeStream) {

    if (officeManager instanceof TemporaryFileMaker) {
      return toInternal(
          new TargetDocumentSpecsFromOutputStream(
              target, (TemporaryFileMaker) officeManager, closeStream));
    }
    throw new IllegalStateException(
        "An office manager must implements the TemporaryFileMaker "
            + "interface in order to be able to convert to OutputStream.");
  }

  /**
   * Configures the current conversion to write the result using the specified specifications.
   *
   * @param target The target specifications to use for the conversion.
   * @return The current conversion specification.
   */
  protected abstract AbstractConversionJob to(AbstractTargetDocumentSpecs target);

  private AbstractConversionJob toInternal(final AbstractTargetDocumentSpecs target) {
    if (FilenameUtils.getExtension(source.getFile().getName()).length() > 0) {
      Validate.notNull(source.getFormat(), "The source format is missing or not supported");
    }

    return to(target);
  }
}
