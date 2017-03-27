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

/**
 * A FilterChain is an object that is responsible to managed an invocation chain of filters. Filters
 * use the FilterChain to invoke the next filter in the chain, or if the calling filter is the last
 * filter in the chain, to end the invocation chain.
 */
public interface FilterChain {

  /**
   * Causes the next filter in the chain to be invoked, or if the calling filter is the last filter
   * in the chain, do nothing.
   *
   * @param context the OfficeContext in use to pass along the chain.
   * @param document the XComponent being converted to pass along the chain.
   * @throws OfficeException if an error processing the filter.
   */
  void doFilter(final OfficeContext context, final XComponent document) throws OfficeException;
}
