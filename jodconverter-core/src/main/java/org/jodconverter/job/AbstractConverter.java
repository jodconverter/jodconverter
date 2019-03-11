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
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;

import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.office.InstalledOfficeManagerHolder;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.TemporaryFileMaker;

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
      final OfficeManager officeManager, final DocumentFormatRegistry formatRegistry) {
    super();

    // An office manager is required
    OfficeManager manager = officeManager;
    if (manager == null) {
      manager = InstalledOfficeManagerHolder.getInstance();
      if (manager == null) {
        throw new IllegalStateException(
            "An office manager is required in order to build a converter.");
      }
    }

    this.officeManager = manager;
    this.formatRegistry =
        formatRegistry == null ? DefaultDocumentFormatRegistry.getInstance() : formatRegistry;
  }

  @Override
  public ConversionJobWithOptionalSourceFormatUnspecified convert(final File source) {

    final SourceDocumentSpecsFromFile specs = new SourceDocumentSpecsFromFile(source);
    final DocumentFormat format =
        formatRegistry.getFormatByExtension(FilenameUtils.getExtension(source.getName()));
    if (format != null) {
      specs.setDocumentFormat(format);
    }

    return convert(specs);
  }

  @Override
  public ConversionJobWithOptionalSourceFormatUnspecified convert(final InputStream source) {

    return convert(source, DEFAULT_CLOSE_STREAM);
  }

  @Override
  public ConversionJobWithOptionalSourceFormatUnspecified convert(
      final InputStream source, final boolean closeStream) {

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
  protected abstract AbstractConversionJobWithSourceFormatUnspecified convert(
      AbstractSourceDocumentSpecs source);

  @Override
  public DocumentFormatRegistry getFormatRegistry() {
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

    // Protected ctor so only subclasses can initialize an instance of this builder.
    protected AbstractConverterBuilder() {
      super();
    }

    /**
     * Specifies the {@link OfficeManager} the converter will use to execute office tasks.
     *
     * @param manager The office manager this converter will use.
     * @return This builder instance.
     */
    public B officeManager(final OfficeManager manager) {

      Validate.notNull(manager);
      this.officeManager = manager;
      return (B) this;
    }

    /**
     * Specifies the {@link DocumentFormatRegistry} which contains the document formats that will be
     * supported by this converter.
     *
     * @param registry The registry that contains the supported formats.
     * @return This builder instance.
     */
    public B formatRegistry(final DocumentFormatRegistry registry) {

      this.formatRegistry = registry;
      return (B) this;
    }

    /**
     * Creates the converter that is specified by this builder.
     *
     * @return The converter that is specified by this builder.
     */
    protected abstract AbstractConverter build();
  }
}
