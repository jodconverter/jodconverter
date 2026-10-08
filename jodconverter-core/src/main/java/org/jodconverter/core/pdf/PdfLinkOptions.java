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
 * Options for the hyperlinks of a PDF document. Supported by all the versions of LibreOffice and by
 * Apache OpenOffice.
 *
 * @see PdfOptions.Builder#links(java.util.function.Consumer)
 */
public final class PdfLinkOptions extends AbstractPdfOptionGroup {

  /** The way the links to other documents are opened. */
  public enum LinkTarget {
    /** The links are exported as they are, and the viewer decides. */
    DEFAULT(0),

    /** The links are opened with the PDF reader application. */
    PDF_READER(1),

    /** The links are opened with the Internet browser. */
    BROWSER(2);

    private final int value;

    LinkTarget(final int value) {
      this.value = value;
    }
  }

  /* default */ PdfLinkOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Specifies whether the links to files are exported relative to the location of the document.
   *
   * <p>FilterData: {@code ExportLinksRelativeFsys}.
   *
   * @param relative {@code true} to export relative file links.
   * @return This group.
   */
  public @NonNull PdfLinkOptions relativeFileLinks(final boolean relative) {
    set(PdfOption.EXPORT_LINKS_RELATIVE_FSYS, relative);
    return this;
  }

  /**
   * Specifies whether the links to OpenDocument files (.odt, .ods...) are changed to target a file
   * of the same name with the .pdf extension.
   *
   * <p>FilterData: {@code ConvertOOoTargetToPDFTarget}.
   *
   * @param convert {@code true} to change the extension of the link targets to .pdf.
   * @return This group.
   */
  public @NonNull PdfLinkOptions convertOdfTargetsToPdf(final boolean convert) {
    set(PdfOption.CONVERT_OOO_TARGET_TO_PDF_TARGET, convert);
    return this;
  }

  /**
   * Specifies how the links from the PDF document to other documents are handled.
   *
   * <p>FilterData: {@code PDFViewSelection}.
   *
   * @param target The way the links are opened.
   * @return This group.
   */
  public @NonNull PdfLinkOptions crossDocumentLinks(final @NonNull LinkTarget target) {
    Objects.requireNonNull(target, "target must not be null");
    set(PdfOption.PDF_VIEW_SELECTION, target.value);
    return this;
  }
}
