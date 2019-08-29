/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2019 Simon Braconnier and contributors
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

package org.jodconverter.filter;

import com.sun.star.lang.XComponent;

import org.jodconverter.office.OfficeContext;

/** Represents a step where a document is transformed. */
public interface Filter {

  /**
   * The <code>doFilter</code> method of the Filter is called each time a document is passed through
   * the chain due to a conversion request. The FilterChain passed in to this method allows the
   * Filter to pass on the document to the next entity in the chain.
   *
   * <p>A typical implementation of this method would <strong>either</strong> invoke the next filter
   * in the chain using the FilterChain object (<code>chain.doFilter()</code>), <strong>or</strong>
   * not pass on the document to the next filter in the filter chain to block the conversion
   * processing.
   *
   * @param context The OfficeContext in use to pass along the chain.
   * @param document The XComponent being converted to pass along the chain.
   * @param chain The chain.
   * @throws Exception If an error occurs processing the filter.
   */
  void doFilter(final OfficeContext context, final XComponent document, final FilterChain chain)
      throws Exception; // NOSONAR
}
