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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.util.OSUtils;
import org.jodconverter.local.office.PasswordProtectedException;

/** Contains tests for the {@link DocumentConverter} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
class DocumentConverterFunctionalITest {

  @Test
  void htmlWithImageConversion(final @TempDir File testFolder, final DocumentConverter converter) {

    final File source = documentFile("index.html");
    final File target = new File(testFolder, "index.pdf");

    // Convert the file to PDF
    converter.convert(source).to(target);
  }

  @Test
  void testHtmlConversion(final @TempDir File testFolder, final DocumentConverter converter) {

    final File source = documentFile("test.html");
    final File target = new File(testFolder, "test.pdf");

    // Convert the file to PDF
    converter.convert(source).to(target);
  }

  @Test
  void testHtmConversion(final @TempDir File testFolder, final DocumentConverter converter) {

    final File source = documentFile("test.htm");
    final File target = new File(testFolder, "test.pdf");

    // Convert the file to PDF
    converter.convert(source).to(target);
  }

  @Test
  void testCustomDocumentFormats(
      final @TempDir File testFolder, final DocumentConverter converter) {

    // TODO: This test does not work on macos (cirrus-ci). Find out why.
    assumeFalse(OSUtils.IS_OS_MAC);

    // This test is done to ensure that the custom-document-formats.json is loaded properly.
    final DocumentFormat format = converter.getFormatRegistry().getFormatByExtension("html");
    assertThat(format).isNotNull();

    final Map<String, Object> properties = format.getStoreProperties(DocumentFamily.PRESENTATION);
    assertThat(properties).isNotNull();

    final Object filterData = properties.get("FilterData");
    assertThat(filterData).isNotNull();
    assertThat(filterData).asInstanceOf(InstanceOfAssertFactories.MAP).containsKey("PublishMode");

    final File inputFile = documentFile("/test_custom_formats.odp");
    final File outputFile = new File(testFolder, "test_out.html");

    assertThatCode(() -> converter.convert(inputFile).to(outputFile).execute())
        .doesNotThrowAnyException();

    assertThat(outputFile).isFile();
    assertThat(testFolder.list()).hasSizeGreaterThanOrEqualTo(1);
  }

  @Test
  void testPasswordProtectedFiles(
      final @TempDir File testFolder, final DocumentConverter converter) {

    final File source = documentFile("test_password.odt");

    // Convert the file to PDF
    final Throwable throwable =
        catchThrowable(
            () -> ConvertUtil.convertFileToSupportedFormats(source, testFolder, converter));

    assertThat(throwable)
        .isNotNull()
        .hasCauseInstanceOf(PasswordProtectedException.class)
        .hasMessageContaining("Document password requested for");
  }

  /** Test the conversion of all the supported documents format. */
  @Test
  void runAllPossibleConversions(
      final @TempDir File testFolder, final DocumentConverter converter) {

    for (final File sourceFile :
        Objects.requireNonNull(
            new File("src/integTest/resources/documents")
                .listFiles((dir, name) -> name.startsWith("test.")))) {
      ConvertUtil.convertFileToSupportedFormats(sourceFile, testFolder, converter);
    }
  }
}
