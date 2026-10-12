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

package org.jodconverter.cli;

import java.util.List;

import org.jodconverter.local.office.OfficeFilterInfo;
import org.jodconverter.local.office.OfficeInfo;

/** Formats the description of an office for the {@code --info} option. */
final class OfficeInfoPrinter {

  private static final String UNKNOWN = "unknown";

  /**
   * Formats the description of an office: its product name and its version on the first line, then
   * its import filters and its export filters, one per line, with the name of the filter, the name
   * the office shows for it and the extensions of its file type.
   *
   * @param info The description of the office.
   * @return The text to print, without a trailing line separator.
   */
  /* default */ static String format(final OfficeInfo info) {

    final var text = new StringBuilder(8192);
    text.append(info.productName() == null ? "Unknown office" : info.productName())
        .append(' ')
        .append(info.version() == null ? "(" + UNKNOWN + " version)" : info.version());
    append(text, "Import filters", info.importFilters());
    append(text, "Export filters", info.exportFilters());
    return text.toString();
  }

  private static void append(
      final StringBuilder text, final String title, final List<OfficeFilterInfo> filters) {

    final var nameWidth =
        filters.stream().mapToInt(filter -> filter.name().length()).max().orElse(0);
    final var format = "%n  %-" + Math.max(1, nameWidth) + "s  %s%s";

    text.append(String.format("%n%n%s (%d):", title, filters.size()));
    for (final var filter : filters) {
      text.append(String.format(format, filter.name(), filter.uiName(), extensions(filter)));
    }
  }

  private static String extensions(final OfficeFilterInfo filter) {
    return filter.extensions().isEmpty() ? "" : " (" + String.join(", ", filter.extensions()) + ")";
  }

  private OfficeInfoPrinter() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
