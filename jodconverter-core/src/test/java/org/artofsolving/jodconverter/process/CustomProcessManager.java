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

package org.artofsolving.jodconverter.process;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.artofsolving.jodconverter.office.OfficeUtils;

/**
 * {@link ProcessManager} implementation for testing custom ProcessManager. It will auto-detect the
 * best process manager and use it as delegate.
 */
public class CustomProcessManager implements ProcessManager {

  private static final Logger logger = LoggerFactory.getLogger(CustomProcessManager.class);

  private final ProcessManager delegate;

  /** Creates a new CustomProcessManager. */
  public CustomProcessManager() {
    super();

    delegate = OfficeUtils.findBestProcessManager();
  }

  @Override
  public long findPid(final ProcessQuery query) throws IOException {
    logger.info("Finding PID from {}", getClass().getName());
    return delegate.findPid(query);
  }

  @Override
  public void kill(final Process process, final long pid) throws IOException {
    logger.info("Kill PID {} from {}", pid, getClass().getName());
    delegate.kill(process, pid);
  }
}
