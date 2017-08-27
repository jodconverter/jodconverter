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

import org.jodconverter.office.OfficeContext;

/** This filter does nothing except calling the next filter in the chain. */
public class NoopFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(NoopFilter.class);

  /**
   * Singleton instance of refresh filter that won't call the next filter in the chain. Use this
   * filter only when you are absolutely sure that it will be used as last filter in a filter chain.
   *
   * @since 4.1.0
   */
  public static final NoopFilter NOOP = new NoopFilter();

  /**
   * Singleton instance of a {@link FilterChain} that will always contain a single {@link
   * NoopFilter} that won't call the next filter in the chain. If a document is just converted from
   * a format to another format, this chain should be used.
   *
   * @since 4.1.0
   */
  public static final FilterChain CHAIN = new UnmodifiableFilterChain(NOOP);

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws Exception {

    LOGGER.debug("Applying the NoopFilter");
    chain.doFilter(context, document);
  }
}
