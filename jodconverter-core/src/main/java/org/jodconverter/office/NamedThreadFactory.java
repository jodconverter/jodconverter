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

package org.jodconverter.office;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** A ThreadFactory that allows for custom thread names. */
class NamedThreadFactory implements ThreadFactory {

  private static final AtomicInteger threadIndex = new AtomicInteger(0);

  private final String basename;
  private final boolean daemon;

  /**
   * Creates a new instance of the factory.
   *
   * @param basename basename of a new tread created by this factory.
   */
  public NamedThreadFactory(final String basename) {
    this(basename, true);
  }

  /**
   * Creates a new instance of the factory.
   *
   * @param basename basename of a new tread created by this factory.
   * @param daemon if true, marks new thread as a daemon thread
   */
  public NamedThreadFactory(final String basename, final boolean daemon) {

    this.basename = basename;
    this.daemon = daemon;
  }

  @Override
  public Thread newThread(final Runnable runnable) {

    final Thread thread = new Thread(runnable, basename + "-" + threadIndex.getAndIncrement());
    thread.setDaemon(daemon);
    return thread;
  }
}
