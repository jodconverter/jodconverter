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

package org.jodconverter.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XRefreshable;

import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;

/** This filter is used to refresh a document. */
public class RefreshFilter implements Filter {

  private static final Logger logger = LoggerFactory.getLogger(RefreshFilter.class);

  public static final RefreshFilter INSTANCE = new RefreshFilter();

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws OfficeException {

    logger.debug("Applying the RefreshFilter");

    final XRefreshable refreshable = UnoRuntime.queryInterface(XRefreshable.class, document);
    if (refreshable != null) {
      logger.debug("Refreshing...");
      refreshable.refresh();
    }
    chain.doFilter(context, document);
  }
}
