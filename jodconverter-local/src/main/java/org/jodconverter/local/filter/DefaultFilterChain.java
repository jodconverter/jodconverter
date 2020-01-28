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

package org.jodconverter.local.filter;

import com.sun.star.lang.XComponent;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;

/** Default implementation of FilterChain. */
public class DefaultFilterChain extends AbstractFilterChain {

  private final boolean endsWithRefreshFilter;

  /**
   * Creates a FilterChain without any filters that will always apply a {@link RefreshFilter} at the
   * end of the chain. Filters can later on be added using {@link #addFilter(Filter)}.
   */
  public DefaultFilterChain() {
    this(true);
  }

  /**
   * Creates a FilterChain that will contains the specified filters and will always apply a {@link
   * RefreshFilter} at the end of the chain.
   *
   * @param filters The filters to add to the chain.
   */
  public DefaultFilterChain(final Filter... filters) {
    this(true, filters);
  }

  /**
   * Creates a FilterChain without any filters. Filters can later on be added using {@link
   * #addFilter(Filter)}.
   *
   * @param endsWithRefreshFilter If {@code true}, a {@link RefreshFilter} will always be applied at
   *     the end of the chain.
   */
  public DefaultFilterChain(final boolean endsWithRefreshFilter) {
    super();

    this.endsWithRefreshFilter = endsWithRefreshFilter;
  }

  /**
   * Creates a FilterChain that will contains the specified filters.
   *
   * @param endsWithRefreshFilter If {@code true}, a {@link RefreshFilter} will always be applied at
   *     the end of the chain.
   * @param filters The filters to add to the chain.
   */
  public DefaultFilterChain(final boolean endsWithRefreshFilter, final Filter... filters) {
    super(filters);

    this.endsWithRefreshFilter = endsWithRefreshFilter;
  }

  @Override
  public FilterChain copy() {
    return new DefaultFilterChain(endsWithRefreshFilter, filters.toArray(new Filter[0]));
  }

  @Override
  public void doFilter(final OfficeContext context, final XComponent document)
      throws OfficeException {

    // Call the RefreshFilter if we are at the end of the chain
    if (pos == filters.size() && endsWithRefreshFilter) {
      doFilter(RefreshFilter.LAST_REFRESH, context, document);
    } else {
      super.doFilter(context, document);
    }
  }
}
