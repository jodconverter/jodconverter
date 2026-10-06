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
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Options for the way a PDF document is displayed when it is opened. Supported by all the versions
 * of LibreOffice and by Apache OpenOffice.
 *
 * @see PdfOptions.Builder#initialView(java.util.function.Consumer)
 */
public final class PdfInitialViewOptions extends AbstractPdfOptionGroup {

  /** The pane shown beside the page. */
  public enum Pane {
    /** No pane, the page only. */
    NONE(0),

    /** The bookmarks (outline) pane. */
    BOOKMARKS(1),

    /** The page thumbnails pane. */
    THUMBNAILS(2);

    private final int value;

    Pane(final int value) {
      this.value = value;
    }
  }

  /** The magnification of the page. */
  public enum Magnification {
    /** The default magnification of the viewer. */
    DEFAULT(0),

    /** The whole page fits in the window. */
    FIT_PAGE(1),

    /** The width of the page fits in the window. */
    FIT_WIDTH(2),

    /** The width of the content of the page, without margins, fits in the window. */
    FIT_VISIBLE(3);

    private final int value;

    Magnification(final int value) {
      this.value = value;
    }
  }

  /** The layout of the pages. */
  public enum PageLayout {
    /** The default layout of the viewer. */
    DEFAULT(0),

    /** One page at a time. */
    SINGLE_PAGE(1),

    /** The pages in one continuous column. */
    CONTINUOUS(2),

    /** The pages in two continuous columns. */
    CONTINUOUS_FACING(3);

    private final int value;

    PageLayout(final int value) {
      this.value = value;
    }
  }

  /* default */ PdfInitialViewOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Specifies the pane shown beside the page when the document is opened.
   *
   * <p>FilterData: {@code InitialView}.
   *
   * @param pane The pane to show.
   * @return This group.
   */
  public @NonNull PdfInitialViewOptions pane(final @NonNull Pane pane) {
    Objects.requireNonNull(pane, "pane must not be null");
    set(PdfOption.INITIAL_VIEW, pane.value);
    return this;
  }

  /**
   * Specifies the page shown when the document is opened.
   *
   * <p>FilterData: {@code InitialPage}.
   *
   * @param page The page number, starting at 1.
   * @return This group.
   */
  public @NonNull PdfInitialViewOptions page(final int page) {
    setAtLeast(PdfOption.INITIAL_PAGE, "page", page, 1);
    return this;
  }

  /**
   * Specifies the magnification of the page when the document is opened.
   *
   * <p>FilterData: {@code Magnification}.
   *
   * @param magnification The magnification.
   * @return This group.
   */
  public @NonNull PdfInitialViewOptions magnification(final @NonNull Magnification magnification) {
    Objects.requireNonNull(magnification, "magnification must not be null");
    set(PdfOption.MAGNIFICATION, magnification.value);
    return this;
  }

  /**
   * Specifies the layout of the pages when the document is opened.
   *
   * <p>FilterData: {@code PageLayout}.
   *
   * @param layout The page layout.
   * @return This group.
   */
  public @NonNull PdfInitialViewOptions layout(final @NonNull PageLayout layout) {
    Objects.requireNonNull(layout, "layout must not be null");
    set(PdfOption.PAGE_LAYOUT, layout.value);
    return this;
  }

  /**
   * Specifies the zoom level of the page when the document is opened.
   *
   * <p>FilterData: {@code Magnification} set to 4, and {@code Zoom}.
   *
   * @param percent The zoom level, in percent.
   * @return This group.
   */
  public @NonNull PdfInitialViewOptions zoom(final int percent) {
    setAtLeast(PdfOption.ZOOM, "zoom", percent, 1);
    // 4: opens with the zoom level of the Zoom property
    set(PdfOption.MAGNIFICATION, 4);
    return this;
  }
}
