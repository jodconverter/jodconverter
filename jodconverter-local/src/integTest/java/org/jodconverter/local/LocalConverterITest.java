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

import static org.assertj.core.api.Assertions.*;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.JsonDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;
import org.jodconverter.local.task.LoadDocumentMode;

/** Contains tests for the {@link LocalConverter} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
class LocalConverterITest {

  private static final File SOURCE_FILE = documentFile("/test.doc");

  @Nested
  class FromFileToFile {

    @Test
    void docToPdf_ShouldSucceed(final @TempDir File testFolder, final DocumentConverter converter) {

      final var outputFile = new File(testFolder, "out.pdf");

      assertThatCode(() -> converter.convert(SOURCE_FILE).to(outputFile).execute())
          .doesNotThrowAnyException();

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withOfficeExecutable_ShouldStartTheExecutableAndConvert(final @TempDir File testFolder)
        throws OfficeException {

      // The office home doesn't contain any office: the process can only be started from the
      // specified executable.
      final var emptyOfficeHome = new File(testFolder, "empty");
      assertThat(emptyOfficeHome.mkdir()).isTrue();
      final var executable =
          LocalOfficeUtils.getOfficeExecutable(LocalOfficeUtils.getDefaultOfficeHome());
      final var outputFile = new File(testFolder, "out.pdf");

      final var manager =
          LocalOfficeManager.builder()
              .officeHome(emptyOfficeHome)
              .officeExecutable(executable)
              .portNumbers(2012)
              .build();
      manager.start();
      try {
        LocalConverter.make(manager).convert(SOURCE_FILE).to(outputFile).execute();
      } finally {
        manager.stop();
      }

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withPoolSize_ShouldStartProcessesOnFreePortsAndConvert(final @TempDir File testFolder)
        throws OfficeException {

      final var outputFile = new File(testFolder, "out.pdf");
      final var manager = LocalOfficeManager.builder().poolSize(2).build();
      manager.start();
      try {
        LocalConverter.make(manager).convert(SOURCE_FILE).to(outputFile).execute();
      } finally {
        manager.stop();
      }

      assertThat(outputFile).isFile();
      assertThat(outputFile.length()).isGreaterThan(0L);
    }

    @Test
    void withNumericFilterDataFromJsonRegistry_ShouldApplyIt(
        final @TempDir File testFolder, final OfficeManager manager)
        throws IOException, OfficeException {

      // SelectPdfVersion 15 = PDF 1.5; LibreOffice writes PDF 1.7 when it ignores the value
      final var registry =
          JsonDocumentFormatRegistry.create(
              """
                                    [
                                      {
                                        "name": "Microsoft Word 97-2003",
                                        "extensions": ["doc"],
                                        "mediaType": "application/msword",
                                        "inputFamily": "TEXT"
                                      },
                                      {
                                        "name": "Portable Document Format",
                                        "extensions": ["pdf"],
                                        "mediaType": "application/pdf",
                                        "storeProperties": {
                                          "TEXT": {
                                            "FilterName": "writer_pdf_Export",
                                            "FilterData": { "SelectPdfVersion": 15 }
                                          }
                                        }
                                      }
                                    ]""");
      final var outputFile = new File(testFolder, "out.pdf");

      LocalConverter.builder()
          .officeManager(manager)
          .formatRegistry(registry)
          .build()
          .convert(SOURCE_FILE)
          .to(outputFile)
          .execute();

      final var header = new byte[8];
      try (var in = Files.newInputStream(outputFile.toPath())) {
        assertThat(in.read(header)).isEqualTo(header.length);
      }
      assertThat(new String(header, StandardCharsets.US_ASCII)).isEqualTo("%PDF-1.5");
    }

    @Test
    void withLoadDocumentModeLocal_ShouldSucceed(
        final @TempDir File testFolder, final OfficeManager manager) {

      final var outputFile = new File(testFolder, "out.pdf");

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

      final var outputFile = new File(testFolder, "out.pdf");

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

      final var outputFile = new File(testFolder, "out.pdf");

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
  class ExecuteAsync {

    @Test
    void withSeveralConversions_ShouldRunThemAndCompleteEachFuture(
        final @TempDir File testFolder, final DocumentConverter converter) {

      final var outputFiles =
          List.of(
              new File(testFolder, "out1.pdf"),
              new File(testFolder, "out2.txt"),
              new File(testFolder, "out3.odt"));

      // The conversions are submitted at once, and joined later.
      final var futures =
          outputFiles.stream()
              .map(outputFile -> converter.convert(SOURCE_FILE).to(outputFile).executeAsync())
              .toList();
      CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

      for (final var outputFile : outputFiles) {
        assertThat(outputFile).isFile();
        assertThat(outputFile.length()).isGreaterThan(0L);
      }
      assertThat(futures).allSatisfy(future -> assertThat(future).isCompleted());
    }

    @Test
    void whenTheConversionFails_ShouldCompleteExceptionallyWithOfficeException(
        final @TempDir File testFolder, final OfficeManager manager) {

      // A filter that fails every conversion.
      final var converter =
          LocalConverter.builder()
              .officeManager(manager)
              .filterChain(
                  (context, document, chain) -> {
                    throw new OfficeException("The filter failed");
                  })
              .build();
      final var outputFile = new File(testFolder, "out.pdf");

      final var future = converter.convert(SOURCE_FILE).to(outputFile).executeAsync();

      assertThatExceptionOfType(CompletionException.class)
          .isThrownBy(future::join)
          .withCauseInstanceOf(OfficeException.class);
    }
  }

  @Nested
  class FromStreamToFile {

    @Test
    @SuppressWarnings("ConstantConditions")
    void withNullInputFormat_ShouldThrowNullPointerException(
        final @TempDir File testFolder, final DocumentConverter converter) throws IOException {

      final var outputFile = new File(testFolder, "out.pdf");

      try (var stream = Files.newInputStream(SOURCE_FILE.toPath())) {
        assertThatNullPointerException()
            .isThrownBy(() -> converter.convert(stream).as(null).to(outputFile).execute());
      }
    }

    @Test
    void withoutInputFormat_ShouldSucceed(
        final @TempDir File testFolder, final DocumentConverter converter) throws IOException {

      final var outputFile = new File(testFolder, "out.pdf");
      try (var stream = Files.newInputStream(SOURCE_FILE.toPath())) {
        assertThatCode(() -> converter.convert(stream).to(outputFile).execute())
            .doesNotThrowAnyException();
      }
    }

    @Test
    void withSupportedInputFormat_ShouldSucceed(
        final @TempDir File testFolder, final DocumentConverter converter) throws IOException {

      final var outputFile = new File(testFolder, "out.pdf");

      try (var stream = Files.newInputStream(SOURCE_FILE.toPath())) {
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

      final var outputFile = new File(testFolder, "out.pdf");

      try (var stream = Files.newInputStream(SOURCE_FILE.toPath())) {
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

      final var outputFile = new File(testFolder, "out.pdf");

      try (var stream = Files.newInputStream(SOURCE_FILE.toPath())) {
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

      final var outputFile = new File(testFolder, "out.pdf");

      try (var stream = Files.newInputStream(SOURCE_FILE.toPath())) {
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

      final var outputFile = new File(testFolder, "out.pdf");

      try (var stream = Files.newOutputStream(outputFile.toPath())) {
        assertThatNullPointerException()
            .isThrownBy(() -> converter.convert(SOURCE_FILE).to(stream).as(null).execute());
      }
    }

    @Test
    void withSupportedOutputFormat_ShouldSucceed(
        final @TempDir File testFolder, final DocumentConverter converter) throws IOException {

      final var outputFile = new File(testFolder, "out.pdf");

      try (var stream = Files.newOutputStream(outputFile.toPath())) {
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

      final var sourceFile = documentFile("test");
      final var outputFile = new File(testFolder, "out.pdf");

      try (var stream = Files.newOutputStream(outputFile.toPath())) {
        assertThatCode(
                () ->
                    converter
                        .convert(sourceFile)
                        .to(stream)
                        .as(DefaultDocumentFormatRegistry.TXT)
                        .execute())
            .doesNotThrowAnyException();
      }

      final var content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
      assertThat(content).as("Check content: %s", content).contains("Test document");
    }

    @Test
    void withLoadDocumentModeLocal_ShouldSucceed(
        final @TempDir File testFolder, final OfficeManager manager) throws IOException {

      final var outputFile = new File(testFolder, "out.pdf");

      try (var stream = Files.newOutputStream(outputFile.toPath())) {
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

      final var outputFile = new File(testFolder, "out.pdf");

      try (var stream = Files.newOutputStream(outputFile.toPath())) {
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

      final var outputFile = new File(testFolder, "out.pdf");

      try (var stream = Files.newOutputStream(outputFile.toPath())) {
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
