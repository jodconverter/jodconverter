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

import org.jodconverter.core.util.AssertUtils;

/**
 * Options for the bookmarks (outline) of a PDF document. Supported by all the versions of
 * LibreOffice and by Apache OpenOffice.
 *
 * @see PdfOptions.Builder#bookmarks(java.util.function.Consumer)
 */
public final class PdfBookmarkOptions extends AbstractPdfOptionGroup {

  /* default */ PdfBookmarkOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Specifies whether the headings of the document are exported as PDF bookmarks.
   *
   * <p>FilterData: {@code ExportBookmarks}.
   *
   * @param export {@code true} to export the bookmarks.
   * @return This group.
   */
  public @NonNull PdfBookmarkOptions export(final boolean export) {
    set(PdfOption.EXPORT_BOOKMARKS, export);
    return this;
  }

  /**
   * Specifies whether the bookmarks of the source document are exported as PDF named destinations.
   *
   * <p>FilterData: {@code ExportBookmarksToPDFDestination}.
   *
   * @param export {@code true} to export the bookmarks as named destinations.
   * @return This group.
   */
  public @NonNull PdfBookmarkOptions asNamedDestinations(final boolean export) {
    set(PdfOption.EXPORT_BOOKMARKS_TO_PDF_DESTINATION, export);
    return this;
  }

  /**
   * Specifies how many bookmark levels are open when the document is opened.
   *
   * <p>FilterData: {@code OpenBookmarkLevels}.
   *
   * @param levels The number of open levels, or -1 for all the levels.
   * @return This group.
   */
  public @NonNull PdfBookmarkOptions openLevels(final int levels) {
    AssertUtils.isTrue(levels == -1 || levels >= 1, "openLevels must be -1 or at least 1");
    set(PdfOption.OPEN_BOOKMARK_LEVELS, levels);
    return this;
  }
}
