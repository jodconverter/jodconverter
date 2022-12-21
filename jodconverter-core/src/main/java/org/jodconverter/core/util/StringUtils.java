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

package org.jodconverter.core.util;

import org.checkerframework.checker.nullness.qual.Nullable;

/** Contains string helper functions. */
public final class StringUtils {

  /**
   * Appends the suffix to the end of the string if the string does not already end with the suffix.
   *
   * @param str The string.
   * @param suffix The suffix to append to the end of the string.
   * @return A new String if suffix was appended, the same string otherwise.
   */
  public static @Nullable String appendIfMissing(
      final @Nullable String str, final @Nullable String suffix) {
    if (str == null || isEmpty(suffix) || str.endsWith(suffix)) {
      return str;
    }
    return str + suffix;
  }

  /**
   * Check if a string ends with any of the provided case-sensitive suffixes.
   *
   * @param str The string to check, may be null.
   * @param searchStrings The case-sensitive string to find, may be empty or contain {@code null}.
   * @return {@code true} if the input string is {@code null} AND no {@code searchStrings} are
   *     provided, or the input str ends in any of the provided case-sensitive {@code
   *     searchStrings}.
   */
  public static boolean endsWithAny(
      final @Nullable String str, final @Nullable String... searchStrings) {
    if (isEmpty(str) || searchStrings == null || searchStrings.length == 0) {
      return false;
    }
    for (final String searchString : searchStrings) {
      if (searchString != null && str.endsWith(searchString)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets whether the specified string is {@code null} or empty.
   *
   * @param str The string to check.
   * @return {@code true} if the given string is {@code null} or empty; false otherwise.
   */
  public static boolean isEmpty(final @Nullable String str) {
    if (str == null) {
      return true;
    }
    return str.isEmpty();
  }

  /**
   * Gets whether the specified string is not {@code null} nor empty.
   *
   * @param str The string to check.
   * @return {@code true} if the given string is nor {@code null} nor empty; false otherwise.
   */
  public static boolean isNotEmpty(final @Nullable String str) {
    if (str == null) {
      return false;
    }
    return !str.isEmpty();
  }

  /**
   * Gets whether the specified string is {@code null}, empty or blank (only whitespace).
   *
   * @param str The string to check.
   * @return {@code true} if the given string is {@code null}, empty or blank (only whitespace);
   *     false otherwise.
   */
  public static boolean isBlank(final @Nullable String str) {
    if (str == null) {
      return true;
    }
    if (str.isEmpty()) {
      return true;
    }
    for (int i = 0; i < str.length(); i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets whether the specified string is not {@code null}, empty or blank (only whitespace).
   *
   * @param str The string to check.
   * @return {@code true} if the given string is not {@code null}, empty or blank (only whitespace);
   *     false otherwise.
   */
  public static boolean isNotBlank(final @Nullable String str) {
    if (str == null) {
      return false;
    }
    if (str.isEmpty()) {
      return false;
    }
    for (int i = 0; i < str.length(); i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private StringUtils() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
