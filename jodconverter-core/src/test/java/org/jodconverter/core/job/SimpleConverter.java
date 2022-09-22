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

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.SimpleOfficeManager;
import org.jodconverter.core.task.SimpleOfficeTask;

/** Converter for testing purposes. */
public final class SimpleConverter extends AbstractConverter {

  /** Builder for our simple converter. */
  public static Builder builder() {
    return new Builder();
  }

  /** Make for our simple converter with default values. */
  public static SimpleConverter make() {
    return new Builder()
        .officeManager(SimpleOfficeManager.make())
        .formatRegistry(DefaultDocumentFormatRegistry.getInstance())
        .build();
  }

  private SimpleConverter(
      final OfficeManager officeManager, final DocumentFormatRegistry formatRegistry) {
    super(officeManager, formatRegistry);
  }

  @Override
  protected AbstractConversionJobWithSourceFormatUnspecified convert(
      final AbstractSourceDocumentSpecs source) {
    return new SimpleConversionJobWithSourceFormatUnspecified(
        source, officeManager, formatRegistry);
  }

  /** Local implementation of a conversion job with source format unspecified. */
  public static class SimpleConversionJobWithSourceFormatUnspecified
      extends AbstractConversionJobWithSourceFormatUnspecified {

    /** Job constructor. */
    public SimpleConversionJobWithSourceFormatUnspecified(
        final AbstractSourceDocumentSpecs source,
        final OfficeManager officeManager,
        final DocumentFormatRegistry formatRegistry) {
      super(source, officeManager, formatRegistry);
    }

    @Override
    protected AbstractConversionJob to(final AbstractTargetDocumentSpecs target) {
      return new SimpleConversionJob(officeManager, source, target);
    }
  }

  /** Job for testing purposes. */
  public static class SimpleConversionJob extends AbstractConversionJob {

    private final OfficeManager officeManager;

    /** Create a new job. */
    public SimpleConversionJob(
        final OfficeManager officeManager,
        final AbstractSourceDocumentSpecs source,
        final AbstractTargetDocumentSpecs target) {
      super(source, target);

      this.officeManager = officeManager;
    }

    @Override
    public void doExecute() throws OfficeException {

      // Create a default conversion task and execute it
      final SimpleOfficeTask task = new SimpleOfficeTask();
      officeManager.execute(task);
    }
  }

  /** Builder for our simple converter. */
  public static final class Builder extends AbstractConverterBuilder<Builder> {

    private Builder() {
      super();
    }

    @Override
    public SimpleConverter build() {
      return new SimpleConverter(officeManager, formatRegistry);
    }
  }
}
