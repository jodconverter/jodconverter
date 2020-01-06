/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

package org.jodconverter.office;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** A ThreadFactory that allows for custom thread names. */
class NamedThreadFactory implements ThreadFactory {

  private static final AtomicInteger THREAD_INDEX = new AtomicInteger(0);

  private final String basename;
  private final boolean daemon;

  /**
   * Creates a new instance of the factory.
   *
   * @param basename Basename of a new tread created by this factory.
   */
  public NamedThreadFactory(final String basename) {
    this(basename, true);
  }

  /**
   * Creates a new instance of the factory.
   *
   * @param basename Basename of a new tread created by this factory.
   * @param daemon If true, marks new thread as a daemon thread
   */
  public NamedThreadFactory(final String basename, final boolean daemon) {

    this.basename = basename;
    this.daemon = daemon;
  }

  @Override
  public Thread newThread(final Runnable runnable) {

    final Thread thread = new Thread(runnable, basename + "-" + THREAD_INDEX.getAndIncrement());
    thread.setDaemon(daemon);
    return thread;
  }
}
