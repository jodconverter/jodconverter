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
 * Options for the window of the PDF viewer when a PDF document is opened. Supported by all the
 * versions of LibreOffice and by Apache OpenOffice.
 *
 * @see PdfOptions.Builder#viewer(java.util.function.Consumer)
 */
public final class PdfViewerOptions extends AbstractPdfOptionGroup {

  /* default */ PdfViewerOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Specifies whether the viewer window is resized to the size of the first page.
   *
   * <p>FilterData: {@code ResizeWindowToInitialPage}.
   *
   * @param resize {@code true} to resize the window.
   * @return This group.
   */
  public @NonNull PdfViewerOptions resizeToInitialPage(final boolean resize) {
    set(PdfOption.RESIZE_WINDOW_TO_INITIAL_PAGE, resize);
    return this;
  }

  /**
   * Specifies whether the viewer window is centered on the screen.
   *
   * <p>FilterData: {@code CenterWindow}.
   *
   * @param center {@code true} to center the window.
   * @return This group.
   */
  public @NonNull PdfViewerOptions centerWindow(final boolean center) {
    set(PdfOption.CENTER_WINDOW, center);
    return this;
  }

  /**
   * Specifies whether the document is opened in full screen mode.
   *
   * <p>FilterData: {@code OpenInFullScreenMode}.
   *
   * @param fullScreen {@code true} to open in full screen mode.
   * @return This group.
   */
  public @NonNull PdfViewerOptions fullScreen(final boolean fullScreen) {
    set(PdfOption.OPEN_IN_FULL_SCREEN_MODE, fullScreen);
    return this;
  }

  /**
   * Specifies whether the title bar of the viewer shows the title of the document instead of the
   * file name.
   *
   * <p>FilterData: {@code DisplayPDFDocumentTitle}.
   *
   * @param display {@code true} to show the document title.
   * @return This group.
   */
  public @NonNull PdfViewerOptions displayDocumentTitle(final boolean display) {
    set(PdfOption.DISPLAY_PDF_DOCUMENT_TITLE, display);
    return this;
  }

  /**
   * Specifies whether the menu bar of the viewer is hidden.
   *
   * <p>FilterData: {@code HideViewerMenubar}.
   *
   * @param hide {@code true} to hide the menu bar.
   * @return This group.
   */
  public @NonNull PdfViewerOptions hideMenubar(final boolean hide) {
    set(PdfOption.HIDE_VIEWER_MENUBAR, hide);
    return this;
  }

  /**
   * Specifies whether the toolbar of the viewer is hidden.
   *
   * <p>FilterData: {@code HideViewerToolbar}.
   *
   * @param hide {@code true} to hide the toolbar.
   * @return This group.
   */
  public @NonNull PdfViewerOptions hideToolbar(final boolean hide) {
    set(PdfOption.HIDE_VIEWER_TOOLBAR, hide);
    return this;
  }

  /**
   * Specifies whether the controls of the viewer window are hidden.
   *
   * <p>FilterData: {@code HideViewerWindowControls}.
   *
   * @param hide {@code true} to hide the window controls.
   * @return This group.
   */
  public @NonNull PdfViewerOptions hideWindowControls(final boolean hide) {
    set(PdfOption.HIDE_VIEWER_WINDOW_CONTROLS, hide);
    return this;
  }
}
