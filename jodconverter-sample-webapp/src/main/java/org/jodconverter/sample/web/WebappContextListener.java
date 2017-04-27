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

package org.jodconverter.sample.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jodconverter.office.OfficeException;

public class WebappContextListener implements ServletContextListener {

  @Override
  public void contextInitialized(final ServletContextEvent event) {

    try {
      WebappContext.init(event.getServletContext());
    } catch (OfficeException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    try {
      WebappContext.destroy(event.getServletContext());
    } catch (OfficeException e) {
      throw new RuntimeException(e);
    }
  }
}
