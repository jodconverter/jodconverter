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

import java.util.Locale;
import java.util.Set;

/** Contains os helper functions. */
public final class OSUtils {

  private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ROOT);

  /** {@code true} if the current OS is MAC, false otherwise. */
  public static final boolean IS_OS_FREE_BSD = OS_NAME.startsWith("freebsd");

  /** {@code true} if the current OS is MAC, false otherwise. */
  public static final boolean IS_OS_MAC = OS_NAME.startsWith("mac");

  /** {@code true} if the current OS is Unix, false otherwise. */
  public static final boolean IS_OS_UNIX =
      Set.of(
              "aix",
              "freebsd",
              "hp-ux",
              "irix",
              "linux",
              "mac os x",
              "netbsd",
              "openbsd",
              "solaris",
              "sunos")
          .stream()
          .anyMatch(OS_NAME::startsWith);

  /** {@code true} if the current OS is Windows, false otherwise. */
  public static final boolean IS_OS_WINDOWS = OS_NAME.startsWith("windows");

  // Suppresses default constructor, ensuring non-instantiability.
  private OSUtils() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
