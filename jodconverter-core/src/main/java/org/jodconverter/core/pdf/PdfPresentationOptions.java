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

/**
 * Options that only apply to presentations (Impress documents).
 *
 * @see PdfOptions.Builder#presentation(java.util.function.Consumer)
 */
public final class PdfPresentationOptions extends AbstractPdfOptionGroup {

  /* default */ PdfPresentationOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Specifies whether the hidden slides are exported. Not supported by Apache OpenOffice.
   *
   * <p>FilterData: {@code ExportHiddenSlides}.
   *
   * @param export {@code true} to export the hidden slides.
   * @return This group.
   */
  public @NonNull PdfPresentationOptions hiddenSlides(final boolean export) {
    set(PdfOption.EXPORT_HIDDEN_SLIDES, export);
    return this;
  }

  /**
   * Specifies whether the notes pages are exported after the slides.
   *
   * <p>FilterData: {@code ExportNotesPages}.
   *
   * @param export {@code true} to export the notes pages.
   * @return This group.
   */
  public @NonNull PdfPresentationOptions notesPages(final boolean export) {
    set(PdfOption.EXPORT_NOTES_PAGES, export);
    return this;
  }

  /**
   * Specifies whether only the notes pages are exported, without the slides.
   *
   * <p>FilterData: {@code ExportOnlyNotesPages}. Requires LibreOffice 5.2 or later.
   *
   * @param only {@code true} to export the notes pages only.
   * @return This group.
   */
  public @NonNull PdfPresentationOptions onlyNotesPages(final boolean only) {
    set(PdfOption.EXPORT_ONLY_NOTES_PAGES, only);
    return this;
  }

  /**
   * Specifies whether the slide transitions are exported.
   *
   * <p>FilterData: {@code UseTransitionEffects}.
   *
   * @param export {@code true} to export the slide transitions.
   * @return This group.
   */
  public @NonNull PdfPresentationOptions transitions(final boolean export) {
    set(PdfOption.USE_TRANSITION_EFFECTS, export);
    return this;
  }
}
