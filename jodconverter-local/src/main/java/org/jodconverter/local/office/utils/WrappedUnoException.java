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

package org.jodconverter.local.office.utils;

import org.checkerframework.checker.nullness.qual.NonNull;

/** This is an exception that wraps a checked exception thrown by office. */
public class WrappedUnoException extends RuntimeException {
  private static final long serialVersionUID = -319689113848560152L;

  /**
   * Constructs a new exception with the specified detail message and wrapped exception.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method).
   * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public WrappedUnoException(
      @NonNull final String message, final com.sun.star.uno.Exception cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified wrapped exception.
   *
   * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public WrappedUnoException(final com.sun.star.uno.Exception cause) {
    super(cause);
  }
}
