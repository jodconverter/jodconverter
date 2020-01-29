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

package org.jodconverter.local.office;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Contains basic information about the office installation being used. */
public final class OfficeDescriptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeDescriptor.class);

  private String product = "???";
  private String version = "???";
  private boolean useLongOptionNameGnuStyle;

  private OfficeDescriptor() {}

  /**
   * Creates descriptor from the command line output using the help option.
   *
   * @param lines The output lines of the execution.
   * @return The descriptor.
   */
  public static OfficeDescriptor fromHelpOutput(final List<String> lines) {

    final OfficeDescriptor desc = new OfficeDescriptor();

    LOGGER.debug("Building {} from help output lines", OfficeDescriptor.class.getName());

    String productLine = null;
    for (final String line : lines) {
      if (line.contains("--help")) {
        desc.useLongOptionNameGnuStyle = true;
      } else {
        final String lowerLine = line.trim().toLowerCase(Locale.ROOT);
        if (lowerLine.startsWith("openoffice") || lowerLine.startsWith("libreoffice")) {
          productLine = line.trim();
        }
      }
    }

    if (productLine != null) {
      final String[] parts = productLine.split(" ");
      if (parts.length > 0) {
        desc.product = parts[0];
      }
      if (parts.length > 1) {
        desc.version = parts[1];
      }
    }

    LOGGER.info("soffice info (from help output): {}", desc.toString());
    return desc;
  }

  /**
   * Creates descriptor from the office installation path.
   *
   * @param path The installaiton path.
   * @return The descriptor.
   */
  public static OfficeDescriptor fromExecutablePath(final String path) {

    final OfficeDescriptor desc = new OfficeDescriptor();

    final String lowerPath = path.toLowerCase(Locale.ROOT);
    if (lowerPath.contains("libreoffice")) {
      desc.product = "LibreOffice";
      desc.useLongOptionNameGnuStyle = true;
    } else if (lowerPath.contains("openoffice")) {
      desc.product = "OpenOffice";
      desc.useLongOptionNameGnuStyle = false;
    }

    // Version cannot be known from the installation path.

    LOGGER.info("soffice info (from exec path): {}", desc.toString());
    return desc;
  }

  /**
   * Gets the product name of the office installation being used.
   *
   * @return LibreOffice or OpenOffice or ??? if unknown.
   */
  public String getProduct() {
    return product;
  }

  /**
   * Gets the version of the office installation being used.
   *
   * @return The version or ??? if unknown.
   */
  public String getVersion() {
    return version;
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
  public String toString() {
    return String.format(
        "Product: %s - Version: %s - useLongOptionNameGnuStyle: %s",
        getProduct(), getVersion(), useLongOptionNameGnuStyle());
  }
}
