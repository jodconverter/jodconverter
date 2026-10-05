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

package org.jodconverter.core.pdf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Describes which office installations support a PDF option or a PDF version. */
final class OfficeSupport {

  /** Supported by all the versions of LibreOffice and by Apache OpenOffice. */
  /* default */ static final OfficeSupport ALL = new OfficeSupport(0, 0, true);

  private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)(?:\\.(\\d+))?");

  private final int major;
  private final int minor;
  private final boolean openOffice;

  private OfficeSupport(final int major, final int minor, final boolean openOffice) {
    this.major = major;
    this.minor = minor;
    this.openOffice = openOffice;
  }

  /**
   * Supported by LibreOffice only, since the given version.
   *
   * @param major The major number of the first LibreOffice version.
   * @param minor The minor number of the first LibreOffice version.
   * @return The office support.
   */
  /* default */ static OfficeSupport libreOffice(final int major, final int minor) {
    return new OfficeSupport(major, minor, false);
  }

  /**
   * Gets whether the given office installation is supported. A version that cannot be read is
   * considered supported, since nothing can be said about it.
   *
   * @param libreOffice {@code true} for LibreOffice, {@code false} for Apache OpenOffice.
   * @param officeVersion The version of the office installation.
   * @return {@code true} if supported, {@code false} otherwise.
   */
  /* default */ boolean isSupportedBy(final boolean libreOffice, final String officeVersion) {

    if (!libreOffice) {
      return openOffice;
    }

    final Matcher matcher = VERSION_PATTERN.matcher(officeVersion.trim());
    if (!matcher.find()) {
      return true;
    }
    final int officeMajor = Integer.parseInt(matcher.group(1));
    final int officeMinor = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
    return officeMajor > major || officeMajor == major && officeMinor >= minor;
  }

  /**
   * Describes the office installations that are supported, such as "LibreOffice 7.4 or later".
   *
   * @return The description.
   */
  /* default */ String describe() {
    if (openOffice) {
      return "all versions";
    }
    return "LibreOffice " + major + "." + minor + " or later";
  }
}
