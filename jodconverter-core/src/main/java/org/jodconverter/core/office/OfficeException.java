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

package org.jodconverter.core.office;

import org.checkerframework.checker.nullness.qual.NonNull;

/** An exception that provides information on an error while dealing with office. */
public class OfficeException extends Exception {
  private static final long serialVersionUID = -1360754252407765922L;
  private final int errorCode;

  /**
   * Constructs a new office exception with the specified detail message. The cause is not
   * initialized, and may subsequently be initialized by a call to {@link #initCause}.
   *
   * @param message The detail message. The detail message is saved for later retrieval by the
   *     {@link #getMessage()} method.
   */
  public OfficeException(final @NonNull String message) {
    super(message);
    errorCode = -1;
  }

  /**
   * Constructs a new office exception with the specified detail message and cause.
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
  public OfficeException(final @NonNull String message, final @NonNull Throwable cause) {
    super(message, cause);
    errorCode = -1;
  }

  /**
   * Constructs a new office exception with the specified detail message and cause.
   *
   * <p>Note that the detail message associated with {@code cause} is <i>not</i> automatically
   * incorporated in this exception's detail message.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method).
   * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   * @param errorCode the errorcode for this specific expection. Currently only used for document
   *     password protection
   */
  public OfficeException(
      final @NonNull String message, final @NonNull Throwable cause, final int errorCode) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  /**
   * the error code for this specific exception
   *
   * @return the errorcode
   */
  public int getErrorCode() {
    return errorCode;
  }
}
