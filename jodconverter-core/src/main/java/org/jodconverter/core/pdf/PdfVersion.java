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

/**
 * The version of the PDF specification, or the PDF/A conformance, of a PDF document.
 *
 * <p>An office installation that does not know a version produces its default PDF version instead,
 * without any error.
 *
 * @see PdfOptions.Builder#version(PdfVersion)
 */
public enum PdfVersion {

  /**
   * The default version of the office installation: PDF 1.7 since LibreOffice 7.6, PDF 1.6 from
   * LibreOffice 7.0 to 7.5, PDF 1.5 from LibreOffice 6.1 to 6.4, and PDF 1.4 before that and with
   * Apache OpenOffice.
   */
  DEFAULT(0, OfficeSupport.ALL),

  /** PDF 1.5. Requires LibreOffice 7.0 or later. */
  PDF_1_5(15, OfficeSupport.libreOffice(7, 0)),

  /** PDF 1.6. Requires LibreOffice 6.2 or later. */
  PDF_1_6(16, OfficeSupport.libreOffice(6, 2)),

  /** PDF 1.7. Requires LibreOffice 7.5 or later. */
  PDF_1_7(17, OfficeSupport.libreOffice(7, 5)),

  /** PDF 2.0. Requires LibreOffice 25.2 or later. */
  PDF_2_0(20, OfficeSupport.libreOffice(25, 2)),

  /** PDF/A-1b (ISO 19005-1). Supported by all the versions of LibreOffice and by OpenOffice. */
  PDF_A_1B(1, OfficeSupport.ALL),

  /** PDF/A-2b (ISO 19005-2). Requires LibreOffice 6.3 or later. */
  PDF_A_2B(2, OfficeSupport.libreOffice(6, 3)),

  /** PDF/A-3b (ISO 19005-3). Requires LibreOffice 7.0 or later. */
  PDF_A_3B(3, OfficeSupport.libreOffice(7, 0)),

  /** PDF/A-4 (ISO 19005-4). Requires LibreOffice 25.2 or later. */
  PDF_A_4(4, OfficeSupport.libreOffice(25, 2));

  private final int value;
  private final OfficeSupport support;

  PdfVersion(final int value, final OfficeSupport support) {
    this.value = value;
    this.support = support;
  }

  /**
   * Gets the value of the {@code SelectPdfVersion} property of the PDF export filter for this
   * version.
   *
   * @return The property value.
   */
  public int getValue() {
    return value;
  }

  /**
   * Gets whether this version is a PDF/A conformance. PDF/A does not allow encryption.
   *
   * @return {@code true} if this is a PDF/A conformance, {@code false} otherwise.
   */
  public boolean isPdfA() {
    return this == PDF_A_1B || this == PDF_A_2B || this == PDF_A_3B || this == PDF_A_4;
  }

  /* default */ OfficeSupport getSupport() {
    return support;
  }

  /* default */ static PdfVersion fromValue(final int value) {
    for (final PdfVersion version : values()) {
      if (version.value == value) {
        return version;
      }
    }
    return null;
  }
}
