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

package org.jodconverter.core.job;

import java.util.List;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.document.DocumentFormat;

/**
 * Options that apply to the target document of a single conversion, given with {@link
 * ConversionJob#with(TargetOptions)}.
 *
 * @see org.jodconverter.core.pdf.PdfOptions
 */
public interface TargetOptions {

  /**
   * Gets whether these options can be applied to a target document of the given format.
   *
   * @param format The target format of the conversion.
   * @return {@code true} if these options apply to the format, {@code false} otherwise.
   */
  boolean supports(@NonNull DocumentFormat format);

  /**
   * Applies these options to the properties that will be used to store (save) the target document.
   * It is called after the store properties of the target format and of the converter have been
   * added to the given map, so these options take precedence over them.
   *
   * @param storeProperties The store properties to modify.
   */
  void applyTo(@NonNull Map<@NonNull String, @NonNull Object> storeProperties);

  /**
   * Gets a description of each of these options that the given office installation does not
   * support, and would thus ignore.
   *
   * @param libreOffice {@code true} if the office installation is LibreOffice, {@code false} if it
   *     is Apache OpenOffice.
   * @param officeVersion The version of the office installation, such as "25.2" or "4.1.15".
   * @return The descriptions, empty when all these options are supported.
   */
  default @NonNull List<@NonNull String> getUnsupportedOptions(
      final boolean libreOffice, final @NonNull String officeVersion) {
    return List.of();
  }
}
