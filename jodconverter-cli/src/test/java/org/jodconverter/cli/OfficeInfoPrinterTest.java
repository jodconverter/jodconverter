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

package org.jodconverter.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.jodconverter.local.office.OfficeFilterInfo;
import org.jodconverter.local.office.OfficeInfo;

/** Contains tests for the {@link OfficeInfoPrinter} class. */
class OfficeInfoPrinterTest {

  @Test
  void format_ShouldListTheImportAndExportFiltersInColumns() {

    final var info =
        new OfficeInfo(
            "LibreOffice",
            "25.2.7.2",
            List.of(
                new OfficeFilterInfo(
                    "MS Word 97", "Word 97", "t", "s", "m", List.of("doc", "dot"), true, true),
                new OfficeFilterInfo(
                    "writer_pdf_Export", "PDF", "t", "s", "m", List.of("pdf"), false, true),
                new OfficeFilterInfo("x_import", "X", null, null, null, List.of(), true, false)));

    assertThat(OfficeInfoPrinter.format(info).lines())
        .containsExactly(
            "LibreOffice 25.2.7.2",
            "",
            "Import filters (2):",
            "  MS Word 97  Word 97 (doc, dot)",
            "  x_import    X",
            "",
            "Export filters (2):",
            "  MS Word 97         Word 97 (doc, dot)",
            "  writer_pdf_Export  PDF (pdf)");
  }

  @Test
  void format_WithoutNameVersionOrFilter_ShouldSaySo() {

    assertThat(OfficeInfoPrinter.format(new OfficeInfo(null, null, List.of())).lines())
        .containsExactly(
            "Unknown office (unknown version)",
            "",
            "Import filters (0):",
            "",
            "Export filters (0):");
  }
}
