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
import java.io.InputStream;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.TemporaryFileMaker;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.core.util.FileUtils;

/**
 * Base class for all document converter implementations.
 *
 * @see DocumentConverter
 */
public abstract class AbstractConverter implements DocumentConverter {

  private static final boolean DEFAULT_CLOSE_STREAM = true;

  protected final OfficeManager officeManager;

  protected final DocumentFormatRegistry formatRegistry;

  protected AbstractConverter(
      final @NonNull OfficeManager officeManager,
      final @NonNull DocumentFormatRegistry formatRegistry) {
    super();

    // Both arguments are required.
    AssertUtils.notNull(officeManager, "officeManager must not be null");
    AssertUtils.notNull(formatRegistry, "formatRegistry must not be null");
    this.officeManager = officeManager;
    this.formatRegistry = formatRegistry;
  }

  @Override
  public @NonNull ConversionJobWithOptionalSourceFormatUnspecified convert(
      final @NonNull File source) {

    final SourceDocumentSpecsFromFile specs = new SourceDocumentSpecsFromFile(source);
    final DocumentFormat format =
        formatRegistry.getFormatByExtension(
            Objects.requireNonNull(FileUtils.getExtension(source.getName())));
    if (format != null) {
      specs.setDocumentFormat(format);
    }

    return convert(specs);
  }

  @Override
  public @NonNull ConversionJobWithOptionalSourceFormatUnspecified convert(
      final @NonNull InputStream source) {

    return convert(source, DEFAULT_CLOSE_STREAM);
  }

  @Override
  public @NonNull ConversionJobWithOptionalSourceFormatUnspecified convert(
      final @NonNull InputStream source, final boolean closeStream) {

    if (officeManager instanceof TemporaryFileMaker) {
      return convert(
          new SourceDocumentSpecsFromInputStream(
              source, (TemporaryFileMaker) officeManager, closeStream));
    }
    throw new IllegalStateException(
        "An office manager must implements the TemporaryFileMaker "
            + "interface in order to be able to convert InputStream");
  }

  /**
   * Converts a source document using the given specifications.
   *
   * @param source The conversion input as a document specifications.
   * @return The current conversion specification.
   */
  protected abstract @NonNull AbstractConversionJobWithSourceFormatUnspecified convert(
      @NonNull AbstractSourceDocumentSpecs source);

  @Override
  public @NonNull DocumentFormatRegistry getFormatRegistry() {
    return formatRegistry;
  }

  /**
   * A builder for constructing an {@link AbstractConverter}.
   *
   * @see AbstractConverter
   */
  @SuppressWarnings("unchecked")
  public abstract static class AbstractConverterBuilder<B extends AbstractConverterBuilder<B>> {

    protected OfficeManager officeManager;
    protected DocumentFormatRegistry formatRegistry;

    // Protected constructor so only subclasses can initialize an instance of this builder.
    protected AbstractConverterBuilder() {
      super();
    }

    /**
     * Creates the converter that is specified by this builder.
     *
     * @return The converter that is specified by this builder.
     */
    protected abstract @NonNull AbstractConverter build();

    /**
     * Specifies the {@link OfficeManager} the converter will use to execute office tasks.
     *
     * @param officeManager The office manager this converter will use.
     * @return This builder instance.
     */
    public @NonNull B officeManager(final @NonNull OfficeManager officeManager) {

      AssertUtils.notNull(officeManager, "officeManager must not be null");
      this.officeManager = officeManager;
      return (B) this;
    }

    /**
     * Specifies the {@link DocumentFormatRegistry} which contains the document formats that will be
     * supported by this converter.
     *
     * @param formatRegistry The registry that contains the supported formats.
     * @return This builder instance.
     */
    public @NonNull B formatRegistry(final @NonNull DocumentFormatRegistry formatRegistry) {

      AssertUtils.notNull(formatRegistry, "formatRegistry must not be null");
      this.formatRegistry = formatRegistry;
      return (B) this;
    }
  }
}
