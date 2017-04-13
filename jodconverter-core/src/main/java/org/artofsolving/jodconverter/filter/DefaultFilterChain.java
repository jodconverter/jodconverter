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

/** Default implementation of FilterChain. */
public class DefaultFilterChain extends FilterChainBase {

  /**
   * Creates a FilterChain without any filters. Filters can later on be added using {@link
   * #addFilter(Filter)}.
   */
  public DefaultFilterChain() {
    super();
  }

  /**
   * Creates a FilterChain that will contains the specified filters.
   *
   * @param filters the filters to add to the chain.
   */
  public DefaultFilterChain(final Filter... filters) {
    super(filters);
  }
}
