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

package org.artofsolving.jodconverter.util;

import org.apache.commons.lang3.StringUtils;

public final class PlatformUtils {

  private static final String OS_NAME = StringUtils.lowerCase(System.getProperty("os.name"));

  private PlatformUtils() {
    throw new AssertionError("utility class must not be instantiated");
  }

  public static boolean isLinux() {
    return StringUtils.startsWith(OS_NAME, "linux");
  }

  public static boolean isMac() {
    return StringUtils.startsWith(OS_NAME, "mac");
  }

  public static boolean isWindows() {
    return StringUtils.startsWith(OS_NAME, "windows");
  }
}
