/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.filter;

import com.sun.star.lang.XComponent;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

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
   * @param context the OfficeContext in use to pass along the chain.
   * @param document the XComponent being converted to pass along the chain.
   * @throws OfficeException if an error processing the filter.
   */
  void doFilter(final OfficeContext context, final XComponent document, final FilterChain chain)
      throws OfficeException;
}
