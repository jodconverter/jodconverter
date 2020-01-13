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

import static org.jodconverter.process.ProcessManager.PID_NOT_FOUND;
import static org.jodconverter.process.ProcessManager.PID_UNKNOWN;

import java.io.IOException;

import org.jodconverter.process.ProcessManager;
import org.jodconverter.process.ProcessQuery;

/** Gets the pid of an office process. */
public class FindPidRetryable extends AbstractRetryable<IOException> {

  private long pid = PID_UNKNOWN;
  private final ProcessQuery processQuery;
  private final ProcessManager processManager;

  /**
   * Creates a new instance of the class.
   *
   * @param processQuery The process query used to start the process.
   * @param processManager The process manager used to find the process pid.
   */
  public FindPidRetryable(final ProcessQuery processQuery, final ProcessManager processManager) {
    super();

    this.processQuery = processQuery;
    this.processManager = processManager;
  }

  @Override
  protected void attempt() throws TemporaryException, IOException {

    pid = PID_UNKNOWN;
    if (processManager.canFindPid()) {
      long processId = processManager.findPid(processQuery);
      if (processId <= PID_UNKNOWN) {
        throw new TemporaryException(
            "The process id has not been found; pid: "
                + (pid == PID_NOT_FOUND
                    ? "PID_NOT_FOUND"
                    : pid == PID_UNKNOWN ? "PID_UNKNOWN" : pid));
      }
      pid = processId;
    }
  }

  /**
   * The pid of the process.
   *
   * @return The pid of the process.
   */
  public long getPid() {
    return pid;
  }
}
