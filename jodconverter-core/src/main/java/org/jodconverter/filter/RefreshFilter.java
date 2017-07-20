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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XRefreshable;

import org.jodconverter.office.OfficeContext;

/** This filter is used to refresh a document. */
public class RefreshFilter implements Filter {

  private static final Logger logger = LoggerFactory.getLogger(RefreshFilter.class);

  /**
   * Singleton instance of refresh filter.
   *
   * @since 4.1.0
   */
  public static final RefreshFilter REFRESH = new RefreshFilter();

  /**
   * Singleton instance of refresh filter. Please use the identical {@link RefreshFilter#REFRESH}
   * constant. The new name is more friendly as it doesn't clash with other values when using static
   * imports.
   */
  @Deprecated public static final RefreshFilter INSTANCE = REFRESH;

  /**
   * Singleton instance of a {@link FilterChain} that will always contain a single {@link
   * RefreshFilter}. If a document is just convert from a format to another format, this chain
   * should be used.
   *
   * @since 4.1.0
   */
  public static final FilterChain CHAIN = new UnmodifiableFilterChain(REFRESH);

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws Exception {

    logger.debug("Applying the RefreshFilter");

    final XRefreshable refreshable = UnoRuntime.queryInterface(XRefreshable.class, document);
    if (refreshable != null) {
      logger.debug("Refreshing...");
      refreshable.refresh();
    }
    chain.doFilter(context, document);
  }
}
