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
 * Options for the form fields of a PDF document. Supported by all the versions of LibreOffice and
 * by Apache OpenOffice.
 *
 * @see PdfOptions.Builder#forms(java.util.function.Consumer)
 */
public final class PdfFormOptions extends AbstractPdfOptionGroup {

  /** The format in which a PDF form is submitted. */
  public enum SubmitFormat {
    /** Forms Data Format. */
    FDF(0),

    /** The whole PDF document. */
    PDF(1),

    /** HTML form data. */
    HTML(2),

    /** XML form data. */
    XML(3);

    private final int value;

    SubmitFormat(final int value) {
      this.value = value;
    }
  }

  /* default */ PdfFormOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Specifies whether the form fields are exported as fillable PDF fields, instead of their printed
   * representation only.
   *
   * <p>FilterData: {@code ExportFormFields}.
   *
   * @param export {@code true} to export fillable form fields.
   * @return This group.
   */
  public @NonNull PdfFormOptions export(final boolean export) {
    set(PdfOption.EXPORT_FORM_FIELDS, export);
    return this;
  }

  /**
   * Specifies the format in which the PDF form is submitted.
   *
   * <p>FilterData: {@code FormsType}.
   *
   * @param format The submit format.
   * @return This group.
   */
  public @NonNull PdfFormOptions submitFormat(final @NonNull SubmitFormat format) {
    Objects.requireNonNull(format, "format must not be null");
    set(PdfOption.FORMS_TYPE, format.value);
    return this;
  }

  /**
   * Specifies whether several form fields may have the same name.
   *
   * <p>FilterData: {@code AllowDuplicateFieldNames}.
   *
   * @param allow {@code true} to allow duplicate field names.
   * @return This group.
   */
  public @NonNull PdfFormOptions allowDuplicateNames(final boolean allow) {
    set(PdfOption.ALLOW_DUPLICATE_FIELD_NAMES, allow);
    return this;
  }
}
