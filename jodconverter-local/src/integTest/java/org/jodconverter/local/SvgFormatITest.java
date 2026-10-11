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

package org.jodconverter.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;

/**
 * Tests which documents are converted to SVG: the office exports presentations and drawings, and
 * fails to store a whole text document or spreadsheet with its SVG filters, which the registry
 * therefore does not offer.
 */
@ExtendWith(LocalOfficeManagerExtension.class)
class SvgFormatITest {

  @Test
  void fromPresentationOrDrawing_ShouldExportSvg(
      final @TempDir File testFolder, final DocumentConverter converter) throws Exception {

    for (final var source : new String[] {"test.odp", "test.odg"}) {
      final var target = new File(testFolder, source + ".svg");

      converter.convert(documentFile(source)).to(target).execute();

      assertThat(Files.readString(target.toPath(), StandardCharsets.UTF_8)).contains("<svg");
    }
  }

  @Test
  void fromTextDocumentOrSpreadsheet_ShouldBeAnUnsupportedConversion(
      final @TempDir File testFolder, final DocumentConverter converter) {

    for (final var source : new String[] {"test.odt", "test.xls"}) {
      final var target = new File(testFolder, source + ".svg");

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> converter.convert(documentFile(source)).to(target).execute())
          .havingRootCause()
          .isInstanceOf(IllegalArgumentException.class)
          .withMessage("Unsupported conversion");
      assertThat(target).doesNotExist();
    }
  }
}
