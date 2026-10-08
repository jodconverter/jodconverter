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

import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Options for the pages and the content exported to a PDF document.
 *
 * @see PdfOptions.Builder#pages(java.util.function.Consumer)
 */
public final class PdfPageOptions extends AbstractPdfOptionGroup {

  /* default */ PdfPageOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Specifies the pages to export, such as "1-3;7". All the pages are exported when not set.
   *
   * <p>FilterData: {@code PageRange}.
   *
   * @param range The page range. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfPageOptions range(final @Nullable String range) {
    setText(PdfOption.PAGE_RANGE, range);
    return this;
  }

  /**
   * Specifies whether the blank pages that Writer inserts automatically are left out.
   *
   * <p>FilterData: {@code IsSkipEmptyPages}.
   *
   * @param skip {@code true} to leave out the automatically inserted blank pages.
   * @return This group.
   */
  public @NonNull PdfPageOptions skipEmptyPages(final boolean skip) {
    set(PdfOption.IS_SKIP_EMPTY_PAGES, skip);
    return this;
  }

  /**
   * Specifies whether the placeholder fields of a Writer document are exported.
   *
   * <p>FilterData: {@code ExportPlaceholders}. Requires LibreOffice 5.1 or later.
   *
   * @param export {@code true} to export the placeholder fields.
   * @return This group.
   */
  public @NonNull PdfPageOptions placeholders(final boolean export) {
    set(PdfOption.EXPORT_PLACEHOLDERS, export);
    return this;
  }

  /**
   * Specifies whether the tracked changes are shown in the exported document.
   *
   * <p>FilterData: {@code ExportTrackedChanges}. Requires LibreOffice 26.2 or later.
   *
   * @param export {@code true} to show the tracked changes.
   * @return This group.
   */
  public @NonNull PdfPageOptions trackedChanges(final boolean export) {
    set(PdfOption.EXPORT_TRACKED_CHANGES, export);
    return this;
  }
}
