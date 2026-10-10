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

package org.jodconverter.local.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalOfficeManagerExtension;
import org.jodconverter.local.office.OfficeFilterInfo;

/** Contains tests for the {@link OfficeInfoTask} class, with a real office. */
@ExtendWith(LocalOfficeManagerExtension.class)
class OfficeInfoTaskITest {

  @Test
  void query_ShouldDescribeTheOffice(final OfficeManager manager) throws OfficeException {

    final var info = OfficeInfoTask.query(manager);

    // The product and the version depend on the installation.
    assertThat(info.productName()).isNotBlank();
    assertThat(info.version()).matches("\\d+\\.\\d+.*");

    assertThat(info.filters())
        .isNotEmpty()
        .isSortedAccordingTo(Comparator.comparing(OfficeFilterInfo::name))
        .allSatisfy(filter -> assertThat(filter.uiName()).isNotBlank());

    // An export only filter, with the file type it writes.
    assertThat(info.exportFilters())
        .filteredOn(filter -> "writer_pdf_Export".equals(filter.name()))
        .singleElement()
        .satisfies(
            filter -> {
              assertThat(filter.canImport()).isFalse();
              assertThat(filter.documentService()).isEqualTo("com.sun.star.text.TextDocument");
              assertThat(filter.mediaType()).isEqualTo("application/pdf");
              assertThat(filter.extensions()).containsExactly("pdf");
            });
    assertThat(info.importFilters())
        .extracting(OfficeFilterInfo::name)
        .doesNotContain("writer_pdf_Export");

    // A filter that loads and stores.
    assertThat(info.importFilters())
        .filteredOn(filter -> "MS Word 97".equals(filter.name()))
        .singleElement()
        .satisfies(
            filter -> {
              assertThat(filter.canExport()).isTrue();
              assertThat(filter.extensions()).contains("doc");
            });
  }
}
