/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

/** Exceptions thrown when a connection to an office process failed. */
public class OfficeConnectionException extends OfficeException {
  private static final long serialVersionUID = -8746059688633528678L;

  private final String connectString;

  /**
   * Constructs a new connect exception with the specified detail message. The cause is not
   * initialized, and may subsequently be initialized by a call to {@link #initCause}.
   *
   * @param message The detail message. The detail message is saved for later retrieval by the
   *     {@link #getMessage()} method.
   * @param connectString The connection string of the connection.
   */
  public OfficeConnectionException(final String message, final String connectString) {
    super(message);

    this.connectString = connectString;
  }

  /**
   * Constructs a new connect exception with the specified detail message and cause.
   *
   * <p>Note that the detail message associated with {@code cause} is <i>not</i> automatically
   * incorporated in this exception's detail message.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method).
   * @param connectString The connection string of the connection.
   * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public OfficeConnectionException(
      final String message, final String connectString, final Throwable cause) {
    super(message, cause);

    this.connectString = connectString;
  }

  /**
   * Gets the connection strong of the connection.
   *
   * @return The connection string.
   */
  public String getConnectString() {

    return connectString;
  }
}
