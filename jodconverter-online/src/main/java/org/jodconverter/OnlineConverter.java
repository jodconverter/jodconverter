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

package org.jodconverter;

import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.job.AbstractConversionJob;
import org.jodconverter.job.AbstractConversionJobWithSourceFormatUnspecified;
import org.jodconverter.job.AbstractConverter;
import org.jodconverter.job.AbstractSourceDocumentSpecs;
import org.jodconverter.job.AbstractTargetDocumentSpecs;
import org.jodconverter.office.InstalledOfficeManagerHolder;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.task.OnlineConversionTask;

/**
 * An online converter will send conversion request to a LibreOffice Online server. It must be used
 * with an OnlineOfficeManager in order to work as expected.
 *
 * @see org.jodconverter.DocumentConverter
 * @see org.jodconverter.office.OnlineOfficeManager
 */
public class OnlineConverter extends AbstractConverter {

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link OnlineConverter} with default configuration. The {@link OfficeManager}
   * that will be used is the one holden by the {@link InstalledOfficeManagerHolder} class, if any.
   *
   * @return A {@link OnlineConverter} with default configuration.
   */
  public static OnlineConverter make() {

    return builder().build();
  }

  /**
   * Creates a new {@link OnlineConverter} using the specified {@link OfficeManager} with default
   * configuration.
   *
   * @param officeManager The {@link OfficeManager} the converter will use to convert document.
   * @return A {@link OnlineConverter} with default configuration.
   */
  public static OnlineConverter make(final OfficeManager officeManager) {
    return builder().officeManager(officeManager).build();
  }

  private OnlineConverter(
      final OfficeManager officeManager, final DocumentFormatRegistry formatRegistry) {
    super(officeManager, formatRegistry);
  }

  @Override
  protected AbstractConversionJobWithSourceFormatUnspecified convert(
      final AbstractSourceDocumentSpecs source) {

    return new OnlineConversionJobWithSourceFormatUnspecified(source);
  }

  private class OnlineConversionJobWithSourceFormatUnspecified
      extends AbstractConversionJobWithSourceFormatUnspecified {

    private OnlineConversionJobWithSourceFormatUnspecified(
        final AbstractSourceDocumentSpecs source) {
      super(source, OnlineConverter.this.officeManager, OnlineConverter.this.formatRegistry);
    }

    @Override
    protected AbstractConversionJob to(final AbstractTargetDocumentSpecs target) {

      return new OnlineConversionJob(source, target);
    }
  }

  private class OnlineConversionJob extends AbstractConversionJob {

    private OnlineConversionJob(
        final AbstractSourceDocumentSpecs source, final AbstractTargetDocumentSpecs target) {
      super(source, target);
    }

    @Override
    public void doExecute() throws OfficeException {

      // Create a default conversion task and execute it
      final OnlineConversionTask task = new OnlineConversionTask(source, target);
      officeManager.execute(task);
    }
  }

  /**
   * A builder for constructing a {@link OnlineConverter}.
   *
   * @see OnlineConverter
   */
  public static final class Builder extends AbstractConverterBuilder<Builder> {

    // Private ctor so only OnlineConverter can create an instance of this builder.
    private Builder() {
      super();
    }

    @Override
    public OnlineConverter build() {

      // Create the converter
      return new OnlineConverter(officeManager, formatRegistry);
    }
  }
}
