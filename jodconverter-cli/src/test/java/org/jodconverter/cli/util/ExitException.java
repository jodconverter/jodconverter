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

package org.jodconverter.cli.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class while testing that will save the status of the first System.exit called through the
 * program execution.
 */
public final class ExitException extends SecurityException {
  private static final long serialVersionUID = 3259467431505006348L;

  public static final ExitException INSTANCE = new ExitException();

  private final AtomicInteger status = new AtomicInteger(-1);

  private ExitException() {
    super("Exit not allowed");
  }

  /**
   * Sets the exit status. If the exit code was already set, it won't change.
   *
   * @param status the exit status.
   */
  public void setStatus(final int status) {

    this.status.compareAndSet(-1, status);
  }

  /**
   * Gets the exit status.
   *
   * @return the exit status.
   */
  public int getStatus() {
    return status.get();
  }

  /** Resets the exit code status. */
  public void reset() {
    status.set(-1);
  }
}
