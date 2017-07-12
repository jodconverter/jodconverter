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

package org.jodconverter.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.star.lang.XComponent;

import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;

/** Base class of a FilterChain. */
public abstract class FilterChainBase implements FilterChain {

  private boolean readOnly;
  private List<Filter> filters;
  private int pos; // to maintain the current position in the filter chain.

  /** Creates a FilterChain. */
  public FilterChainBase() {
    this(new Filter[] {});
  }

  /**
   * Creates a FilterChain that will contains the specified filters.
   *
   * @param readOnly {@code true} If the chain must be read-only (which means that no other filter
   *     can be added to the chain), {@code false} otherwise.
   * @param filters The filters to initially add to the chain.
   */
  public FilterChainBase(final boolean readOnly, final Filter... filters) {

    this.readOnly = readOnly;
    this.pos = 0;
    this.filters = new ArrayList<>();

    if (filters != null) {
      for (final Filter filter : filters) {
        this.filters.add(filter);
      }
    }

    if (readOnly) {
      this.filters = Collections.unmodifiableList(this.filters);
    }
  }

  /**
   * Creates a FilterChain that will contains the specified filters.
   *
   * @param filters The filters to add to the chain.
   */
  public FilterChainBase(final Filter... filters) {
    this(false, filters);
  }

  /**
   * Adds a filter to the chain.
   *
   * @param filter The filter to add at the end of the chain.
   */
  public void addFilter(final Filter filter) {

    if (readOnly) {
      throw new UnsupportedOperationException();
    }
    filters.add(filter);
  }

  @Override
  public void doFilter(final OfficeContext context, final XComponent document)
      throws OfficeException {

    // Call the next filter if there is one
    if (pos < filters.size()) {
      final Filter filter = filters.get(pos++);
      filter.doFilter(context, document, this);
    }
  }

  /** Resets the position in the filter chain to 0, making the chain reusable. */
  public void reset() {

    pos = 0;
  }
}
