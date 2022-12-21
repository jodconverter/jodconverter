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

package org.jodconverter.local.office;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.office.OfficeException;

/** Exceptions thrown when a password-protected file cannot be opened. */
public class PasswordProtectedException extends OfficeException {
  private static final long serialVersionUID = -9200513745869729664L;

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * <p>Note that the detail message associated with {@code cause} is <i>not</i> automatically
   * incorporated in this exception's detail message.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method).
   * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public PasswordProtectedException(final @NonNull String message, final @NonNull Throwable cause) {
    super(message, cause);
  }
}
