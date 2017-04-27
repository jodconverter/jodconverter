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
   * @param <T> type of the object to validate.
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
