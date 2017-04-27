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

/** Gets the exit code value of an office process. */
public class ExitCodeRetryable extends Retryable {

  private final Process process;
  private int exitCode;

  /**
   * Creates a new instance of the class for the specified process.
   *
   * @param process the process whose exit code is to be retrieved.
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
   * @return the exit value of the process. The value 0 indicates normal termination.
   */
  public int getExitCode() {
    return exitCode;
  }
}
