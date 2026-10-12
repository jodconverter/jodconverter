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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;
import org.jodconverter.local.task.OfficeInfoTask;

/** Contains tests for the {@link OfficeFormatRegistry} class, with a real office. */
@ExtendWith(LocalOfficeManagerExtension.class)
class OfficeFormatRegistryITest {

  private static final File SOURCE_FILE = documentFile("test.odt");

  @Test
  void withAnExtensionOnlyTheOfficeKnows_ShouldConvert(
      final @TempDir File testFolder, final OfficeManager manager) throws Exception {

    final var epub = new File(testFolder, "test.epub");
    final var docm = new File(testFolder, "test.docm");

    // The default registry does not know these formats.
    final var defaults = LocalConverter.builder().officeManager(manager).build();
    assertThatNullPointerException()
        .isThrownBy(() -> defaults.convert(SOURCE_FILE).to(epub).execute())
        .withMessageContaining("target format is missing or not supported");

    final var converter =
        LocalConverter.builder()
            .officeManager(manager)
            .formatRegistry(OfficeFormatRegistry.create(OfficeInfoTask.query(manager)))
            .build();
    converter.convert(SOURCE_FILE).to(epub).execute();
    converter.convert(SOURCE_FILE).to(docm).execute();

    // An EPUB file is a ZIP archive that starts with its media type.
    try (var zip = new ZipFile(epub)) {
      final var entry = zip.getEntry("mimetype");
      assertThat(entry).isNotNull();
      assertThat(new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8))
          .isEqualTo("application/epub+zip");
    }
    // A Word document with macros is a ZIP archive too, of the macro-enabled content type.
    try (var zip = new ZipFile(docm)) {
      assertThat(
              new String(
                  zip.getInputStream(zip.getEntry("[Content_Types].xml")).readAllBytes(),
                  StandardCharsets.UTF_8))
          .contains("application/vnd.ms-word.document.macroEnabled.main+xml");
    }
    assertThat(Files.size(docm.toPath())).isPositive();
  }
}
