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
 * Options that only apply to spreadsheets (Calc documents).
 *
 * @see PdfOptions.Builder#spreadsheet(java.util.function.Consumer)
 */
public final class PdfSpreadsheetOptions extends AbstractPdfOptionGroup {

  /* default */ PdfSpreadsheetOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Specifies whether each sheet is exported as a single page.
   *
   * <p>FilterData: {@code SinglePageSheets}. Requires LibreOffice 6.4 or later.
   *
   * @param singlePage {@code true} to export one page per sheet.
   * @return This group.
   */
  public @NonNull PdfSpreadsheetOptions singlePageSheets(final boolean singlePage) {
    set(PdfOption.SINGLE_PAGE_SHEETS, singlePage);
    return this;
  }

  /**
   * Specifies the sheets to export, by their position, such as "2" or "1-3". All the sheets are
   * exported when not set. The office ignores this option when {@link #singlePageSheets(boolean)}
   * is set.
   *
   * <p>FilterData: {@code SheetRange}. Requires LibreOffice 24.8 or later.
   *
   * @param range The sheet range. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfSpreadsheetOptions sheetRange(final @Nullable String range) {
    setText(PdfOption.SHEET_RANGE, range);
    return this;
  }
}
