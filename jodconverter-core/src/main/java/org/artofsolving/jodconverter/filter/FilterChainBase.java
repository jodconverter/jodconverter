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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.star.lang.XComponent;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

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
   * @param filters the filters to add to the chain.
   */
  public FilterChainBase(final Filter... filters) {
    this(false, filters);
  }

  /**
   * Creates a FilterChain that will contains the specified filters.
   *
   * @param readOnly {@code true} if the chain must be read-only (which means that no other filter
   *     can be added to the chain), {@code false} otherwise.
   * @param filters the filters to initially add to the chain.
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
   * Adds a filter to the chain.
   *
   * @param filter the filter to add at the end of the chain.
   */
  public void addFilter(final Filter filter) {

    if (readOnly) {
      throw new UnsupportedOperationException();
    }
    filters.add(filter);
  }

  /** Resets the position in the filter chain to 0, making the chain reusable. */
  public void reset() {

    pos = 0;
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
}
