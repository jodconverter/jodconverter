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

package org.artofsolving.jodconverter.office;

import java.io.File;

/** Validation helper class. */
public final class ValidateUtils {

  /**
   * Validates that the specified File instance is an existing file.
   *
   * @param file the file to check.
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *     null.
   * @throws OfficeException if file does not exists.
   */
  public static void fileExists(final File file, final String message) throws OfficeException {

    if (file == null || !file.isFile()) {
      throw new OfficeException(String.format(message, file));
    }
  }

  /**
   * Validates that the specified object is not null.
   *
   * @param object to check.
   * @param message the exception message if invalid, not null.
   * @throws OfficeException if object is null.
   */
  public static <T> void notNull(final T object, final String message) throws OfficeException {

    if (object == null) {
      throw new OfficeException(message);
    }
  }

  // Private ctor.
  private ValidateUtils() { // NOSONAR
    throw new AssertionError("utility class must not be instantiated");
  }
}
