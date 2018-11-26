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

package org.jodconverter.filter;

import com.sun.star.lang.XComponent;

import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;

/**
 * A FilterChain is an object that is responsible to managed an invocation chain of filters. Filters
 * use the FilterChain to invoke the next filter in the chain, or if the calling filter is the last
 * filter in the chain, to end the invocation chain.
 */
public interface FilterChain {

  /**
   * Adds a filter to the chain.
   *
   * @param filter The filter to add at the end of the chain.
   */
  void addFilter(Filter filter);

  /**
   * Causes the next filter in the chain to be invoked, or if the calling filter is the last filter
   * in the chain, do nothing.
   *
   * @param context The OfficeContext in use to pass along the chain.
   * @param document The XComponent being converted to pass along the chain.
   * @throws OfficeException If an error occurs processing the filter.
   */
  void doFilter(final OfficeContext context, final XComponent document) throws OfficeException;

  /**
   * Creates and returns a copy of this object. The precise meaning of "copy" may depend on the
   * class of the chain.
   *
   * @return The copy of this chain.
   */
  FilterChain copy();
}
