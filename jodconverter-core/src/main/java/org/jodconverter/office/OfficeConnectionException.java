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

/** Exceptions thrown when a connection to an office process failed. */
public class OfficeConnectionException extends OfficeException {
  private static final long serialVersionUID = -8746059688633528678L;

  private final String connectString;

  /**
   * Constructs a new connect exception with the specified detail message. The cause is not
   * initialized, and may subsequently be initialized by a call to {@link #initCause}.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   *     {@link #getMessage()} method.
   * @param connectString the connection string of the connection.
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
   * @param message the detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method).
   * @param connectString the connection string of the connection.
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or
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
   * @return the connection string.
   */
  public String getConnectString() {

    return connectString;
  }
}
