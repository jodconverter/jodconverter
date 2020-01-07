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

package org.jodconverter.filter;

import com.sun.star.lang.XComponent;
import com.sun.star.util.XRefreshable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.utils.Lo;

/** This filter is used to refresh a document. */
public class RefreshFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RefreshFilter.class);

  /**
   * Singleton instance of refresh filter that won't call the next filter in the chain. Use this
   * filter only when you are absolutely sure that it will be used as last filter in a filter chain.
   *
   * @since 4.1.0
   */
  public static final RefreshFilter LAST_REFRESH = new RefreshFilter(true);

  /** @deprecated Use {@link RefreshFilter#LAST_REFRESH}. */
  @Deprecated public static final RefreshFilter INSTANCE = LAST_REFRESH;

  /**
   * Singleton instance of a {@link FilterChain} that will always contain a single {@link
   * RefreshFilter} that won't call the next filter in the chain. If a document is just converted
   * from a format to another format, this chain should be used.
   *
   * @since 4.1.0
   */
  public static final FilterChain CHAIN = new UnmodifiableFilterChain(LAST_REFRESH);

  private final boolean lastFilter;

  /** Creates a new refresh filter. */
  public RefreshFilter() {
    this(false);
  }

  /**
   * Creates a new refresh filter that will call or not the next filter in the chain according to
   * the specified argument.
   *
   * @param lastFilter If {@code true}, then the filter won't call the next filter in the chain. If
   *     {@code false}, the next filter in the chain, if any, will be applied.
   */
  public RefreshFilter(final boolean lastFilter) {
    super();

    this.lastFilter = lastFilter;
  }

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws Exception {

    LOGGER.debug("Applying the RefreshFilter");
    Lo.qiOptional(XRefreshable.class, document).ifPresent(XRefreshable::refresh);

    if (!lastFilter) {
      chain.doFilter(context, document);
    }
  }
}
