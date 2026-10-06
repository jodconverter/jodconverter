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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.task.OfficeTask;

/**
 * Base class for all conversion job implementations.
 *
 * @see org.jodconverter.core.job.ConversionJob
 */
public abstract class AbstractConversionJob
    implements ConversionJobWithOptionalTargetFormatUnspecified {

  protected final AbstractSourceDocumentSpecs source;
  protected final AbstractTargetDocumentSpecs target;

  private List<TargetOptions> defaultTargetOptions = List.of();

  protected AbstractConversionJob(
      final @NonNull AbstractSourceDocumentSpecs source,
      final @NonNull AbstractTargetDocumentSpecs target) {
    super();

    // Both arguments are required.
    Objects.requireNonNull(source, "source must not be null");
    Objects.requireNonNull(target, "target must not be null");
    this.source = source;
    this.target = target;
  }

  @Override
  public @NonNull AbstractConversionJob as(final @NonNull DocumentFormat format) {

    target.setDocumentFormat(format);
    return this;
  }

  @Override
  public @NonNull AbstractConversionJob with(final @NonNull TargetOptions options) {

    Objects.requireNonNull(options, "options must not be null");
    target.setOptions(options);
    return this;
  }

  @Override
  public final void execute() throws OfficeException {

    getOfficeManager().execute(prepareTask());
  }

  @Override
  public final @NonNull CompletableFuture<Void> executeAsync() {

    return getOfficeManager().submit(prepareTask());
  }

  // Checks the target format and options of the conversion, then creates its task.
  private OfficeTask prepareTask() {

    final var format = target.getFormat();
    Objects.requireNonNull(format, "The target format is missing or not supported");
    var options = target.getOptions();
    if (options == null) {
      // No options for this conversion: use the first default options of the converter that
      // support the target format, if any.
      options =
          defaultTargetOptions.stream()
              .filter(defaultOptions -> defaultOptions.supports(format))
              .findFirst()
              .orElse(null);
      if (options != null) {
        target.setOptions(options);
      }
    } else if (!options.supports(format)) {
      throw new IllegalArgumentException(
          options.getClass().getSimpleName()
              + " cannot be applied to a target document of format '"
              + format.getExtension()
              + "'");
    }
    return createTask();
  }

  /**
   * Sets the options of the converter to apply when this conversion has no options of its own.
   *
   * @param defaultTargetOptions The default options.
   */
  /* default */ void setDefaultTargetOptions(final List<TargetOptions> defaultTargetOptions) {
    this.defaultTargetOptions = defaultTargetOptions;
  }

  /**
   * Gets the office manager that executes the task of this conversion.
   *
   * @return The office manager.
   */
  protected abstract @NonNull OfficeManager getOfficeManager();

  /**
   * Creates the task of this conversion. Both source and target document formats are known and
   * valid at this point.
   *
   * @return The task to execute.
   */
  protected abstract @NonNull OfficeTask createTask();
}
