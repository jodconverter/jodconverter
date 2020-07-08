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
import java.io.OutputStream;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.TemporaryFileMaker;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.core.util.FileUtils;

/**
 * Base class for all conversion job implementations with source format that is not yet applied to
 * the converter.
 *
 * @see ConversionJobWithOptionalSourceFormatUnspecified
 */
public abstract class AbstractConversionJobWithSourceFormatUnspecified
    implements ConversionJobWithOptionalSourceFormatUnspecified {

  private static final boolean DEFAULT_CLOSE_STREAM = true;

  protected final AbstractSourceDocumentSpecs source;
  protected final OfficeManager officeManager;
  protected final DocumentFormatRegistry formatRegistry;

  protected AbstractConversionJobWithSourceFormatUnspecified(
      final @NonNull AbstractSourceDocumentSpecs source,
      final @NonNull OfficeManager officeManager,
      final @NonNull DocumentFormatRegistry formatRegistry) {
    super();

    // All arguments are required.
    AssertUtils.notNull(source, "source must not be null");
    AssertUtils.notNull(officeManager, "officeManager must not be null");
    AssertUtils.notNull(formatRegistry, "formatRegistry must not be null");
    this.source = source;
    this.officeManager = officeManager;
    this.formatRegistry = formatRegistry;
  }

  @Override
  public @NonNull AbstractConversionJobWithSourceFormatUnspecified as(
      final @NonNull DocumentFormat format) {

    source.setDocumentFormat(format);
    return this;
  }

  @Override
  public @NonNull AbstractConversionJob to(final @NonNull File target) {

    final TargetDocumentSpecsFromFile specs = new TargetDocumentSpecsFromFile(target);
    final DocumentFormat format =
        formatRegistry.getFormatByExtension(
            Objects.requireNonNull(FileUtils.getExtension(target.getName())));
    if (format != null) {
      specs.setDocumentFormat(format);
    }

    return toInternal(specs);
  }

  @Override
  public @NonNull AbstractConversionJob to(final @NonNull OutputStream target) {

    return to(target, DEFAULT_CLOSE_STREAM);
  }

  @Override
  public @NonNull AbstractConversionJob to(
      final @NonNull OutputStream target, final boolean closeStream) {

    if (officeManager instanceof TemporaryFileMaker) {
      return toInternal(
          new TargetDocumentSpecsFromOutputStream(
              target, (TemporaryFileMaker) officeManager, closeStream));
    }
    throw new IllegalStateException(
        "An office manager must implements the TemporaryFileMaker "
            + "interface in order to be able to convert to OutputStream");
  }

  /**
   * Configures the current conversion to write the result using the specified specifications.
   *
   * @param target The target specifications to use for the conversion.
   * @return The current conversion specification.
   */
  protected abstract @NonNull AbstractConversionJob to(@NonNull AbstractTargetDocumentSpecs target);

  private AbstractConversionJob toInternal(final AbstractTargetDocumentSpecs target) {

    // No need to validate that the source format is provided. We will let
    // OOo deal with the detection of the source file format.

    return to(target);
  }
}
