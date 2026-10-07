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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
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

  private final List<TargetOptions> defaultTargetOptions;

  protected AbstractConverter(
      final @NonNull OfficeManager officeManager,
      final @NonNull DocumentFormatRegistry formatRegistry) {
    this(officeManager, formatRegistry, null);
  }

  protected AbstractConverter(
      final @NonNull OfficeManager officeManager,
      final @NonNull DocumentFormatRegistry formatRegistry,
      final @Nullable List<@NonNull TargetOptions> defaultTargetOptions) {
    super();

    // Both arguments are required.
    Objects.requireNonNull(officeManager, "officeManager must not be null");
    Objects.requireNonNull(formatRegistry, "formatRegistry must not be null");
    this.officeManager = officeManager;
    this.formatRegistry = formatRegistry;
    this.defaultTargetOptions =
        defaultTargetOptions == null ? List.of() : List.copyOf(defaultTargetOptions);
  }

  @Override
  public @NonNull ConversionJobWithOptionalSourceFormatUnspecified convert(
      final @NonNull File source) {
    return newJob(sourceSpecs(source));
  }

  /**
   * Creates the specifications of a source file, with the format of its extension when the registry
   * knows it.
   *
   * @param source The source file.
   * @return The specifications.
   */
  protected @NonNull SourceDocumentSpecsFromFile sourceSpecs(final @NonNull File source) {
    final var specs = new SourceDocumentSpecsFromFile(source);
    final var format =
        formatRegistry.getFormatByExtension(
            Objects.requireNonNull(FileUtils.getExtension(source.getName())));
    if (format != null) {
      specs.setDocumentFormat(format);
    }
    return specs;
  }

  @Override
  public @NonNull ConversionJobWithOptionalSourceFormatUnspecified convert(
      final @NonNull InputStream source) {

    return convert(source, DEFAULT_CLOSE_STREAM);
  }

  @Override
  public @NonNull ConversionJobWithOptionalSourceFormatUnspecified convert(
      final @NonNull InputStream source, final boolean closeStream) {

    return newJob(new SourceDocumentSpecsFromInputStream(source, officeManager, closeStream));
  }

  /**
   * Converts a source document using the given specifications.
   *
   * @param source The conversion input as document specifications.
   * @return The current conversion specification.
   */
  protected abstract @NonNull AbstractConversionJobWithSourceFormatUnspecified convert(
      @NonNull AbstractSourceDocumentSpecs source);

  // Creates the conversion job of a source document, which knows the default target options.
  private AbstractConversionJobWithSourceFormatUnspecified newJob(
      final AbstractSourceDocumentSpecs source) {

    final var job = convert(source);
    job.setDefaultTargetOptions(defaultTargetOptions);
    return job;
  }

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
    protected final List<TargetOptions> defaultTargetOptions = new ArrayList<>();

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

      Objects.requireNonNull(officeManager, "officeManager must not be null");
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

      Objects.requireNonNull(formatRegistry, "formatRegistry must not be null");
      this.formatRegistry = formatRegistry;
      return (B) this;
    }

    /**
     * Specifies options that the converter applies to the target document of every conversion whose
     * target format they support, such as {@link org.jodconverter.core.pdf.PdfOptions} for all the
     * conversions to PDF. This method can be called several times, for options of different
     * formats; when several options support a target format, the first ones win.
     *
     * <p>The options given to a conversion with {@link ConversionJob#with(TargetOptions)} replace
     * these default options for that conversion.
     *
     * @param options The default options.
     * @return This builder instance.
     */
    public @NonNull B defaultTargetOptions(final @NonNull TargetOptions options) {

      Objects.requireNonNull(options, "options must not be null");
      this.defaultTargetOptions.add(options);
      return (B) this;
    }
  }
}
