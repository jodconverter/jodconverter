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

import java.util.Locale;

/** Contains os helper functions. */
public final class OSUtils {

  private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ROOT);

  /** {@code true} if the current OS is AIX, false otherwise. */
  public static final boolean IS_OS_AIX = OS_NAME.startsWith("aix");

  /** {@code true} if the current OS is MAC, false otherwise. */
  public static final boolean IS_OS_FREE_BSD = OS_NAME.startsWith("freebsd");

  /** {@code true} if the current OS is HP-UX, false otherwise. */
  public static final boolean IS_OS_HP_UX = OS_NAME.startsWith("hp-ux");

  /** {@code true} if the current OS is Irix, false otherwise. */
  public static final boolean IS_OS_IRIX = OS_NAME.startsWith("irix");

  /** {@code true} if the current OS is Linux, false otherwise. */
  public static final boolean IS_OS_LINUX = OS_NAME.startsWith("linux");

  /** {@code true} if the current OS is MAC, false otherwise. */
  public static final boolean IS_OS_MAC = OS_NAME.startsWith("mac");

  /** {@code true} if the current OS is Mac OS X, false otherwise. */
  public static final boolean IS_OS_MAC_OSX = OS_NAME.startsWith("mac os x");

  /** {@code true} if the current OS is NetBSD, false otherwise. */
  public static final boolean IS_OS_NET_BSD = OS_NAME.startsWith("netbsd");

  /** {@code true} if the current OS is OpenBSD, false otherwise. */
  public static final boolean IS_OS_OPEN_BSD = OS_NAME.startsWith("openbsd");

  /** {@code true} if the current OS is Solaris, false otherwise. */
  public static final boolean IS_OS_SOLARIS = OS_NAME.startsWith("solaris");

  /** {@code true} if the current OS is SunOS, false otherwise. */
  public static final boolean IS_OS_SUN_OS = OS_NAME.startsWith("sunos");

  /** {@code true} if the current OS is Unix, false otherwise. */
  public static final boolean IS_OS_UNIX =
      IS_OS_AIX
          || IS_OS_FREE_BSD
          || IS_OS_HP_UX
          || IS_OS_IRIX
          || IS_OS_LINUX
          || IS_OS_MAC_OSX
          || IS_OS_NET_BSD
          || IS_OS_OPEN_BSD
          || IS_OS_SOLARIS
          || IS_OS_SUN_OS;

  /** {@code true} if the current OS is Windows, false otherwise. */
  public static final boolean IS_OS_WINDOWS = OS_NAME.startsWith("windows");

  // Suppresses default constructor, ensuring non-instantiability.
  private OSUtils() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
