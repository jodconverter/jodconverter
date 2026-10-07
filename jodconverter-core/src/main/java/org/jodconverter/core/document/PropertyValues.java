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

package org.jodconverter.core.document;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Converts the text of an office property, as written on a command line or in a configuration file,
 * to the value office expects: a load or store property, or a {@code FilterData} entry.
 */
public final class PropertyValues {

  // Suppresses default constructor, ensuring non-instantiability.
  private PropertyValues() {
    throw new AssertionError("Utility class must not be instantiated");
  }

  /**
   * Converts the text of a property to its value: {@code true} and {@code false} (in any case)
   * become booleans, a whole number becomes an integer, and anything else stays a text.
   *
   * @param value The text of the property.
   * @return The value of the property.
   */
  public static @NonNull Object parse(final @NonNull String value) {

    if ("true".equalsIgnoreCase(value)) {
      return Boolean.TRUE;
    }
    if ("false".equalsIgnoreCase(value)) {
      return Boolean.FALSE;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      return value;
    }
  }
}
