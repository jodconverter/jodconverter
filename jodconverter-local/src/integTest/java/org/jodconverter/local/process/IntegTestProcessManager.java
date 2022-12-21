/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

package org.jodconverter.local.process;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.local.office.LocalOfficeUtils;

/**
 * {@link org.jodconverter.local.process.ProcessManager} implementation for testing custom
 * ProcessManager. It will auto-detect the best process manager and use it as delegate.
 */
public class IntegTestProcessManager implements ProcessManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegTestProcessManager.class);

  private final ProcessManager delegate;
  public final AtomicInteger canFindPidCount = new AtomicInteger();
  public final AtomicInteger findPidCount = new AtomicInteger();
  public final AtomicInteger killCount = new AtomicInteger();

  /** Creates a new IntegTestProcessManager. */
  public IntegTestProcessManager() {
    super();

    delegate = LocalOfficeUtils.findBestProcessManager();
  }

  @Override
  public boolean canFindPid() {
    LOGGER.debug("Checking PID findable from {}", getClass().getName());
    canFindPidCount.incrementAndGet();
    return delegate.canFindPid();
  }

  @Override
  public long findPid(@SuppressWarnings("NullableProblems") final ProcessQuery query)
      throws IOException {
    LOGGER.debug("Finding PID from {}", getClass().getName());
    findPidCount.incrementAndGet();
    return delegate.findPid(query);
  }

  @Override
  public void kill(final Process process, final long pid) throws IOException {
    LOGGER.debug("Kill PID {} from {}", pid, getClass().getName());
    killCount.incrementAndGet();
    delegate.kill(process, pid);
  }
}
