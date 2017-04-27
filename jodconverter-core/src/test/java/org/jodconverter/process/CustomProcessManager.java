/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.process;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.office.OfficeUtils;

/**
 * {@link org.jodconverter.process.ProcessManager} implementation for testing custom ProcessManager.
 * It will auto-detect the best process manager and use it as delegate.
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
