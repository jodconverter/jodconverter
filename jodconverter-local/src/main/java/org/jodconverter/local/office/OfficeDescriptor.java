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

package org.jodconverter.local.office;

import java.util.Locale;

import org.checkerframework.checker.nullness.qual.NonNull;

/** Contains basic information about the office installation being used. */
public final class OfficeDescriptor {

  private static final String LIBRE_OFFICE = "LibreOffice";
  private static final String OPEN_OFFICE = "OpenOffice";

  private String product = "???";
  private boolean useLongOptionNameGnuStyle;

  private OfficeDescriptor() {}

  /**
   * Creates descriptor from the office installation path.
   *
   * @param path The installation path.
   * @return The descriptor.
   */
  public static @NonNull OfficeDescriptor fromExecutablePath(final @NonNull String path) {

    final OfficeDescriptor desc = new OfficeDescriptor();

    final String lowerPath = path.toLowerCase(Locale.ROOT);
    if (lowerPath.contains(LIBRE_OFFICE.toLowerCase(Locale.ROOT))) {
      desc.product = LIBRE_OFFICE;
      desc.useLongOptionNameGnuStyle = true;
    } else if (lowerPath.contains(OPEN_OFFICE.toLowerCase(Locale.ROOT))) {
      desc.product = OPEN_OFFICE;
      desc.useLongOptionNameGnuStyle = false;
    }

    return desc;
  }

  /**
   * Gets the product name of the office installation being used.
   *
   * @return LibreOffice or OpenOffice or ??? if unknown.
   */
  public @NonNull String getProduct() {
    return product;
  }

  /**
   * Gets whether we must use the lone option name GNU style (--) when setting command line options
   * to start an office instance.
   *
   * @return {@code true} to use the lone option name GNU style,{@code false} otherwise.
   */
  public boolean useLongOptionNameGnuStyle() {
    return useLongOptionNameGnuStyle;
  }

  @Override
  public @NonNull String toString() {
    return String.format(
        "Product: %s - useLongOptionNameGnuStyle: %s", getProduct(), useLongOptionNameGnuStyle());
  }
}
