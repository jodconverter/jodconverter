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

package org.jodconverter.job;

import org.apache.commons.lang3.Validate;

import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;

/**
 * Base class for all conversion job implementations.
 *
 * @see ConversionJob
 */
public abstract class AbstractConversionJob
    implements ConversionJobWithOptionalTargetFormatUnspecified {

  protected AbstractSourceDocumentSpecs source;
  protected AbstractTargetDocumentSpecs target;

  protected AbstractConversionJob(
      final AbstractSourceDocumentSpecs source, final AbstractTargetDocumentSpecs target) {
    super();

    this.source = source;
    this.target = target;
  }

  @Override
  public ConversionJob as(final DocumentFormat format) {

    target.setDocumentFormat(format);
    return this;
  }

  @Override
  public final void execute() throws OfficeException {

    Validate.notNull(target.getFormat(), "The target format is missing or not supported");
    doExecute();
  }

  /**
   * Executes the conversion and blocks until the conversion terminates. Both source and target
   * document formats are known and valid at this point.
   *
   * @throws OfficeException If the conversion failed.
   */
  protected abstract void doExecute() throws OfficeException;
}
