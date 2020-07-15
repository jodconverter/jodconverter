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

package org.jodconverter.local.process;
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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.local.office.LocalOfficeUtils;

/**
 * {@link org.jodconverter.local.process.ProcessManager} implementation for testing custom
 * ProcessManager. It will auto-detect the best process manager and use it as delegate.
 */
public class TestProcessManager implements ProcessManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestProcessManager.class);

  private final ProcessManager delegate;

  /** Creates a new TestProcessManager. */
  public TestProcessManager() {
    super();

    delegate = LocalOfficeUtils.findBestProcessManager();
  }

  @Override
  public boolean canFindPid() {
    LOGGER.debug("Checking PID findable from {}", getClass().getName());
    return delegate.canFindPid();
  }

  @Override
  public long findPid(@SuppressWarnings("NullableProblems") final ProcessQuery query)
      throws IOException {
    LOGGER.debug("Finding PID from {}", getClass().getName());
    return delegate.findPid(query);
  }

  @Override
  public void kill(final Process process, final long pid) throws IOException {
    LOGGER.debug("Kill PID {} from {}", pid, getClass().getName());
    delegate.kill(process, pid);
  }
}
