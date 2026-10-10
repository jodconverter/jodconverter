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
 * What an office installation says about itself: its product name, its version and its filters.
 * {@link org.jodconverter.local.task.OfficeInfoTask} reads it from a running office.
 *
 * @param productName The name of the product, {@code LibreOffice} or {@code OpenOffice} for
 *     example; null if the office does not tell.
 * @param version The version of the product, as shown in its about box, {@code 25.2.7.2} for
 *     example; null if the office does not tell.
 * @param filters The filters of the office, sorted by name.
 */
public record OfficeInfo(
    @Nullable String productName,
    @Nullable String version,
    @NonNull List<@NonNull OfficeFilterInfo> filters) {

  /**
   * Creates the description of an office installation.
   *
   * @param productName The name of the product.
   * @param version The version of the product.
   * @param filters The filters of the office.
   */
  public OfficeInfo {
    filters = List.copyOf(filters);
  }

  /**
   * Gets the filters that load documents.
   *
   * @return The import filters, sorted by name.
   */
  public @NonNull List<@NonNull OfficeFilterInfo> importFilters() {
    return filters.stream().filter(OfficeFilterInfo::canImport).toList();
  }

  /**
   * Gets the filters that store documents.
   *
   * @return The export filters, sorted by name.
   */
  public @NonNull List<@NonNull OfficeFilterInfo> exportFilters() {
    return filters.stream().filter(OfficeFilterInfo::canExport).toList();
  }
}
