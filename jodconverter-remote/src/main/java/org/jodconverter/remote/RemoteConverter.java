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

package org.jodconverter.remote;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.job.AbstractConversionJob;
import org.jodconverter.core.job.AbstractConversionJobWithSourceFormatUnspecified;
import org.jodconverter.core.job.AbstractConverter;
import org.jodconverter.core.job.AbstractSourceDocumentSpecs;
import org.jodconverter.core.job.AbstractTargetDocumentSpecs;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.remote.office.RemoteOfficeManager;
import org.jodconverter.remote.task.RemoteConversionTask;

/**
 * A remote converter will send conversion request to a LibreOffice Online server. It must be used
 * with an RemoteOfficeManager in order to work as expected.
 *
 * @see org.jodconverter.core.DocumentConverter
 * @see RemoteOfficeManager
 */
public final class RemoteConverter extends AbstractConverter {

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link RemoteConverter} with default configuration. The {@link
   * org.jodconverter.core.office.OfficeManager} that will be used is the one holden by the {@link
   * org.jodconverter.core.office.InstalledOfficeManagerHolder} class, if any.
   *
   * @return A {@link RemoteConverter} with default configuration.
   */
  public static @NonNull RemoteConverter make() {

    return builder().build();
  }

  /**
   * Creates a new {@link RemoteConverter} using the specified {@link
   * org.jodconverter.core.office.OfficeManager} with default configuration.
   *
   * @param officeManager The {@link org.jodconverter.core.office.OfficeManager} the converter will
   *     use to convert document.
   * @return A {@link RemoteConverter} with default configuration.
   */
  public static @NonNull RemoteConverter make(final @NonNull OfficeManager officeManager) {
    return builder().officeManager(officeManager).build();
  }

  private RemoteConverter(
      final OfficeManager officeManager, final DocumentFormatRegistry formatRegistry) {
    super(officeManager, formatRegistry);
  }

  @Override
  protected @NonNull AbstractConversionJobWithSourceFormatUnspecified convert(
      final @NonNull AbstractSourceDocumentSpecs source) {

    return new RemoteConversionJobWithSourceFormatUnspecified(source);
  }

  /** Remote implementation of a conversion job with source format unspecified. */
  private class RemoteConversionJobWithSourceFormatUnspecified
      extends AbstractConversionJobWithSourceFormatUnspecified {

    private RemoteConversionJobWithSourceFormatUnspecified(
        final AbstractSourceDocumentSpecs source) {
      super(source, RemoteConverter.this.officeManager, RemoteConverter.this.formatRegistry);
    }

    @Override
    protected @NonNull AbstractConversionJob to(final @NonNull AbstractTargetDocumentSpecs target) {

      return new RemoteConversionJob(source, target);
    }
  }

  /** Remote implementation of a conversion job. */
  private class RemoteConversionJob extends AbstractConversionJob {

    private RemoteConversionJob(
        final AbstractSourceDocumentSpecs source, final AbstractTargetDocumentSpecs target) {
      super(source, target);
    }

    @Override
    public void doExecute() throws OfficeException {

      // Create a default conversion task and execute it
      final RemoteConversionTask task = new RemoteConversionTask(source, target);
      officeManager.execute(task);
    }
  }

  /**
   * A builder for constructing a {@link RemoteConverter}.
   *
   * @see RemoteConverter
   */
  public static final class Builder extends AbstractConverterBuilder<Builder> {

    // Private constructor so only RemoteConverter can create an instance of this builder.
    private Builder() {
      super();
    }

    @Override
    public @NonNull RemoteConverter build() {

      // An office manager is required.
      OfficeManager manager = officeManager;
      if (manager == null) {
        manager = InstalledOfficeManagerHolder.getInstance();
        if (manager == null) {
          throw new IllegalStateException(
              "An office manager is required in order to build a converter.");
        }
      }

      // Create the converter
      return new RemoteConverter(
          manager,
          formatRegistry == null ? DefaultDocumentFormatRegistry.getInstance() : formatRegistry);
    }
  }
}
