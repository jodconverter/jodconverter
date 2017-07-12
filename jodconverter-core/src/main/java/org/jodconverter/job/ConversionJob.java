/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeException;

/** A fully specified conversion that is not yet applied to the converter. */
public interface ConversionJob {

  /**
   * Executes a conversion and blocks until the conversion terminates.
   *
   * @throws OfficeException If the conversion failed.
   */
  void execute() throws OfficeException;

  /**
   * Specifies the whole filter chain to apply when converting a document. A FilterChain is used to
   * modify the document before the conversion. Filters are applied in the same order they appear in
   * the chain.
   *
   * @param filterChain The FilterChain to be applied after the document is loaded and before it is
   *     stored (converted) in the new document format.
   * @return The current conversion specification.
   */
  ConversionJob with(FilterChain filterChain);
}
