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

package org.jodconverter.core.util;

import java.util.Collection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Contains assertions helper functions. */
public final class AssertUtils {

  /**
   * Validates that the argument condition is {@code true}.
   *
   * @param expression The boolean expression to validate.
   * @param message The exception message to use if the assertion fails.
   * @throws IllegalArgumentException If expression is {@code false}.
   */
  public static void isTrue(final boolean expression, @NonNull final String message) {
    if (!expression) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Validates that the specified string is neither {@code null}, nor empty, nor blank (only
   * whitespace).
   *
   * @param str The string to validate.
   * @param message The exception message to use if the assertion fails.
   * @throws NullPointerException If the string is {@code null}
   * @throws IllegalArgumentException If the string is blank.
   */
  public static void notBlank(@Nullable final String str, @NonNull final String message) {
    if (str == null) {
      throw new NullPointerException(message);
    }
    if (str.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    for (int i = 0; i < str.length(); i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return;
      }
    }
    throw new IllegalArgumentException(message);
  }

  /**
   * Validates that the specified argument collection is neither {@code null} nor empty.
   *
   * @param collection The collection to validate.
   * @param message The exception message to use if the assertion fails.
   * @param <T> The type of the elements in the collection.
   * @throws NullPointerException If the collection is {@code null}.
   * @throws IllegalArgumentException If the collection is empty.
   */
  public static <T> void notEmpty(
      @Nullable final Collection<T> collection, @NonNull final String message) {
    if (collection == null) {
      throw new NullPointerException(message);
    }
    if (collection.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Validates that the specified argument array is neither {@code null} nor empty.
   *
   * @param array The array to validate.
   * @param message The exception message to use if the assertion fails.
   * @param <T> The type of the elements in the array.
   * @throws NullPointerException If the array is {@code null}
   * @throws IllegalArgumentException If the array is empty.
   */
  public static <T> void notEmpty(@Nullable final T[] array, @NonNull final String message) {
    if (array == null) {
      throw new NullPointerException(message);
    }
    if (array.length == 0) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Validates that the specified string is neither {@code null} nor empty.
   *
   * @param str The string to validate.
   * @param message The exception message to use if the assertion fails.
   * @throws NullPointerException If the string is {@code null}
   * @throws IllegalArgumentException If the string is empty.
   */
  public static void notEmpty(@Nullable final String str, @NonNull final String message) {
    if (str == null) {
      throw new NullPointerException(message);
    }
    if (str.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Validates that the specified argument is not {@code null}.
   *
   * @param object The object to validate.
   * @param message The exception message to use if the assertion fails.
   * @throws NullPointerException If the object is {@code null}.
   */
  public static void notNull(@Nullable final Object object, @NonNull final String message) {
    if (object == null) {
      throw new NullPointerException(message);
    }
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private AssertUtils() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
