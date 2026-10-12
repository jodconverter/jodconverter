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

package org.jodconverter.local.office;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A filter of an office installation, which loads documents of a format, stores them, or both.
 *
 * @param name The name of the filter, as given to the {@code FilterName} load or store property,
 *     {@code writer_pdf_Export} for example.
 * @param uiName The name of the filter shown by the office in its dialogs, in the language of the
 *     office; the name of the filter when it has none.
 * @param type The name of the file type of the filter, {@code pdf_Portable_Document_Format} for
 *     example; may be null.
 * @param documentService The service of the documents the filter applies to, {@code
 *     com.sun.star.text.TextDocument} for example; may be null.
 * @param mediaType The media type of the file type; may be null.
 * @param extensions The file extensions of the file type, without the dot; may be empty.
 * @param canImport {@code true} if the filter loads documents.
 * @param canExport {@code true} if the filter stores documents.
 * @see OfficeInfo
 */
public record OfficeFilterInfo(
    @NonNull String name,
    @NonNull String uiName,
    @Nullable String type,
    @Nullable String documentService,
    @Nullable String mediaType,
    @NonNull List<@NonNull String> extensions,
    boolean canImport,
    boolean canExport) {

  /**
   * Creates the description of a filter.
   *
   * @param name The name of the filter.
   * @param uiName The name shown by the office.
   * @param type The name of the file type.
   * @param documentService The service of the documents.
   * @param mediaType The media type of the file type.
   * @param extensions The file extensions of the file type.
   * @param canImport Whether the filter loads documents.
   * @param canExport Whether the filter stores documents.
   */
  public OfficeFilterInfo {
    extensions = List.copyOf(extensions);
  }
}
