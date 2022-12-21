/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.local.task.LoadDocumentMode;

/** Contains tests for the {@link LocalConverter} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
class LocalConverterITest {

  private static final File SOURCE_FILE = documentFile("/test.doc");

  @Nested
  class FromFileToFile {

    @Test
    void docToPdf_ShouldSucceed(final @TempDir File testFolder, final DocumentConverter converter) {

      final File outputFile = new File(testFolder, "out.pdf");

      assertThatCode(() -> converter.convert(SOURCE_FILE).to(outputFile).execute())
          .doesNotThrowAnyException();

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withLoadDocumentModeLocal_ShouldSucceed(
        final @TempDir File testFolder, final OfficeManager manager) {

      final File outputFile = new File(testFolder, "out.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(manager)
                      .loadDocumentMode(LoadDocumentMode.LOCAL)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(outputFile)
                      .execute())
          .doesNotThrowAnyException();

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withLoadDocumentModeRemote_ShouldSucceed(
        final @TempDir File testFolder, final OfficeManager manager) {

      final File outputFile = new File(testFolder, "out.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(manager)
                      .loadDocumentMode(LoadDocumentMode.REMOTE)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(outputFile)
                      .execute())
          .doesNotThrowAnyException();

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withLoadDocumentModeAuto_ShouldSucceed(
        final @TempDir File testFolder, final OfficeManager manager) {

      final File outputFile = new File(testFolder, "out.pdf");

      assertThatCode(
              () ->
                  LocalConverter.builder()
                      .officeManager(manager)
                      .loadDocumentMode(LoadDocumentMode.AUTO)
                      .build()
                      .convert(SOURCE_FILE)
                      .to(outputFile)
                      .execute())
          .doesNotThrowAnyException();

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }
  }

  @Nested
  class FromStreamToFile {

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNullInputFormat_ShouldThrowNullPointerException(
        final @TempDir File testFolder, final DocumentConverter converter) throws IOException {

      final File outputFile = new File(testFolder, "out.pdf");

      try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
        assertThatNullPointerException()
            .isThrownBy(() -> converter.convert(stream).as(null).to(outputFile).execute());
      }
    }

    @Test
    void withoutInputFormat_ShouldSucceed(
        final @TempDir File testFolder, final DocumentConverter converter) throws IOException {

      final File outputFile = new File(testFolder, "out.pdf");
      try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
        assertThatCode(() -> converter.convert(stream).to(outputFile).execute())
            .doesNotThrowAnyException();
      }
    }

    @Test
    void withSupportedInputFormat_ShouldSucceed(
        final @TempDir File testFolder, final DocumentConverter converter) throws IOException {

      final File outputFile = new File(testFolder, "out.pdf");

      try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
        assertThatCode(
                () ->
                    converter
                        .convert(stream)
                        .as(DefaultDocumentFormatRegistry.DOC)
                        .to(outputFile)
                        .execute())
            .doesNotThrowAnyException();
      }

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withLoadDocumentModeLocal_ShouldSucceed(
        final @TempDir File testFolder, final OfficeManager manager) throws IOException {

      final File outputFile = new File(testFolder, "out.pdf");

      try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
        assertThatCode(
                () ->
                    LocalConverter.builder()
                        .officeManager(manager)
                        .loadDocumentMode(LoadDocumentMode.LOCAL)
                        .build()
                        .convert(stream)
                        .to(outputFile)
                        .execute())
            .doesNotThrowAnyException();
      }

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withLoadDocumentModeRemote_ShouldSucceed(
        final @TempDir File testFolder, final OfficeManager manager) throws IOException {

      final File outputFile = new File(testFolder, "out.pdf");

      try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
        assertThatCode(
                () ->
                    LocalConverter.builder()
                        .officeManager(manager)
                        .loadDocumentMode(LoadDocumentMode.REMOTE)
                        .build()
                        .convert(stream)
                        .to(outputFile)
                        .execute())
            .doesNotThrowAnyException();
      }

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withLoadDocumentModeAuto_ShouldSucceed(
        final @TempDir File testFolder, final OfficeManager manager) throws IOException {

      final File outputFile = new File(testFolder, "out.pdf");

      try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
        assertThatCode(
                () ->
                    LocalConverter.builder()
                        .officeManager(manager)
                        .loadDocumentMode(LoadDocumentMode.AUTO)
                        .build()
                        .convert(stream)
                        .to(outputFile)
                        .execute())
            .doesNotThrowAnyException();
      }

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }
  }

  @Nested
  class FromFileToStream {

    @Test
    @SuppressWarnings("ConstantConditions")
    void withMissingOutputFormat_ShouldThrowNullPointerException(
        final @TempDir File testFolder, final DocumentConverter converter) throws IOException {

      final File outputFile = new File(testFolder, "out.pdf");

      try (OutputStream stream = Files.newOutputStream(outputFile.toPath())) {
        assertThatNullPointerException()
            .isThrownBy(() -> converter.convert(SOURCE_FILE).to(stream).as(null).execute());
      }
    }

    @Test
    void withSupportedOutputFormat_ShouldSucceed(
        final @TempDir File testFolder, final DocumentConverter converter) throws IOException {

      final File outputFile = new File(testFolder, "out.pdf");

      try (OutputStream stream = Files.newOutputStream(outputFile.toPath())) {
        assertThatCode(
                () ->
                    converter
                        .convert(SOURCE_FILE)
                        .to(stream)
                        .as(DefaultDocumentFormatRegistry.PDF)
                        .execute())
            .doesNotThrowAnyException();
      }

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withoutExtension_ShouldSucceed(
        final @TempDir File testFolder, final DocumentConverter converter) throws IOException {

      final File sourceFile = documentFile("test");
      final File outputFile = new File(testFolder, "out.pdf");

      try (OutputStream stream = Files.newOutputStream(outputFile.toPath())) {
        assertThatCode(
                () ->
                    converter
                        .convert(sourceFile)
                        .to(stream)
                        .as(DefaultDocumentFormatRegistry.TXT)
                        .execute())
            .doesNotThrowAnyException();
      }

      final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
      assertThat(content).as("Check content: %s", content).contains("Test document");
    }

    @Test
    void withLoadDocumentModeLocal_ShouldSucceed(
        final @TempDir File testFolder, final OfficeManager manager) throws IOException {

      final File outputFile = new File(testFolder, "out.pdf");

      try (OutputStream stream = Files.newOutputStream(outputFile.toPath())) {
        assertThatCode(
                () ->
                    LocalConverter.builder()
                        .officeManager(manager)
                        .loadDocumentMode(LoadDocumentMode.LOCAL)
                        .build()
                        .convert(SOURCE_FILE)
                        .to(stream)
                        .as(DefaultDocumentFormatRegistry.PDF)
                        .execute())
            .doesNotThrowAnyException();
      }

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withLoadDocumentModeRemote_ShouldSucceed(
        final @TempDir File testFolder, final OfficeManager manager) throws IOException {

      final File outputFile = new File(testFolder, "out.pdf");

      try (OutputStream stream = Files.newOutputStream(outputFile.toPath())) {
        assertThatCode(
                () ->
                    LocalConverter.builder()
                        .officeManager(manager)
                        .loadDocumentMode(LoadDocumentMode.REMOTE)
                        .build()
                        .convert(SOURCE_FILE)
                        .to(stream)
                        .as(DefaultDocumentFormatRegistry.PDF)
                        .execute())
            .doesNotThrowAnyException();
      }

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withLoadDocumentModeAuto_ShouldSucceed(
        final @TempDir File testFolder, final OfficeManager manager) throws IOException {

      final File outputFile = new File(testFolder, "out.pdf");

      try (OutputStream stream = Files.newOutputStream(outputFile.toPath())) {
        assertThatCode(
                () ->
                    LocalConverter.builder()
                        .officeManager(manager)
                        .loadDocumentMode(LoadDocumentMode.AUTO)
                        .build()
                        .convert(SOURCE_FILE)
                        .to(stream)
                        .as(DefaultDocumentFormatRegistry.PDF)
                        .execute())
            .doesNotThrowAnyException();
      }

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }
  }
}
