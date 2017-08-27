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

package org.jodconverter.office;

/** Gets the exit code value of an office process. */
public class ExitCodeRetryable extends AbstractRetryable {

  private final Process process;
  private int exitCode;

  /**
   * Creates a new instance of the class for the specified process.
   *
   * @param process The process whose exit code is to be retrieved.
   */
  public ExitCodeRetryable(final Process process) {
    super();

    this.process = process;
  }

  @Override
  protected void attempt() throws TemporaryException {

    try {
      exitCode = process.exitValue();
    } catch (IllegalThreadStateException illegalThreadStateEx) {
      throw new TemporaryException(illegalThreadStateEx);
    }
  }

  /**
   * The exit code of the process.
   *
   * @return The exit value of the process. The value 0 indicates normal termination.
   */
  public int getExitCode() {
    return exitCode;
  }
}
