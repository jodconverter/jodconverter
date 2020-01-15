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

package org.jodconverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.jodconverter.ResourceUtil.documentFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.document.DefaultDocumentFormatRegistry;

@ExtendWith(LocalOfficeManagerExtension.class)
public class LocalConverterITest {

  private static final File SOURCE_FILE = documentFile("/test.doc");

  @Test
  public void convert_FromFileToFile_ShouldSucceeded(
      @TempDir File testFolder, DocumentConverter converter) {

    final File outputFile = new File(testFolder, "out.pdf");

    assertThatCode(() -> converter.convert(SOURCE_FILE).to(outputFile).execute())
        .doesNotThrowAnyException();

    assertThat(outputFile).isFile();
    assertThat(outputFile.length()).isGreaterThan(0L);
  }

  @Test
  public void convert_FromStreamToFileWithNullInputFormat_ShouldThrowNullPointerException(
      @TempDir File testFolder, DocumentConverter converter) throws IOException {

    final File outputFile = new File(testFolder, "out.pdf");

    try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
      assertThatNullPointerException()
          .isThrownBy(() -> converter.convert(stream).as(null).to(outputFile).execute());
    }
  }

  @Test
  public void convert_FromStreamToFileWithoutInputFormat_ShouldSucceeded(
      @TempDir File testFolder, DocumentConverter converter) throws IOException {

    final File outputFile = new File(testFolder, "out.pdf");
    try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
      assertThatCode(() -> converter.convert(stream).to(outputFile).execute())
          .doesNotThrowAnyException();
    }
  }

  @Test
  public void convert_FromStreamToFileWithSupportedInputFormat_ShouldSucceeded(
      @TempDir File testFolder, DocumentConverter converter) throws IOException {

    final File outputFile = new File(testFolder, "out.pdf");

    final InputStream stream = Files.newInputStream(SOURCE_FILE.toPath());

    assertThatCode(
            () ->
                converter
                    .convert(stream)
                    .as(DefaultDocumentFormatRegistry.getFormatByExtension("doc"))
                    .to(outputFile)
                    .execute())
        .doesNotThrowAnyException();

    assertThat(outputFile).isFile();
    assertThat(outputFile.length()).isGreaterThan(0L);
  }

  @Test
  public void convert_FromFileToStreamWithMissingOutputFormat_ShouldThrowNullPointerException(
      @TempDir File testFolder, DocumentConverter converter) throws IOException {

    final File outputFile = new File(testFolder, "out.pdf");

    try (OutputStream stream = Files.newOutputStream(outputFile.toPath())) {
      assertThatNullPointerException()
          .isThrownBy(() -> converter.convert(SOURCE_FILE).to(stream).as(null).execute());
    }
  }

  @Test
  public void convert_FromFileToStreamWithSupportedOutputFormat_ShouldSucceeded(
      @TempDir File testFolder, DocumentConverter converter) throws IOException {

    final File outputFile = new File(testFolder, "out.pdf");

    final OutputStream stream = Files.newOutputStream(outputFile.toPath());

    assertThatCode(
            () ->
                converter
                    .convert(SOURCE_FILE)
                    .to(stream)
                    .as(DefaultDocumentFormatRegistry.getFormatByExtension("pdf"))
                    .execute())
        .doesNotThrowAnyException();

    assertThat(outputFile).isFile();
    assertThat(outputFile.length()).isGreaterThan(0L);
  }

  @Test
  public void convert_FromFileWithoutExtensionToFile_ShouldSucceeded(
      @TempDir File testFolder, DocumentConverter converter) throws IOException {

    final File outputFile = new File(testFolder, "out.pdf");

    final OutputStream stream = Files.newOutputStream(outputFile.toPath());

    final File sourceFile = documentFile("test");
    assertThatCode(
            () ->
                converter
                    .convert(sourceFile)
                    .to(stream)
                    .as(DefaultDocumentFormatRegistry.getFormatByExtension("txt"))
                    .execute())
        .doesNotThrowAnyException();

    final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
    assertThat(content).as("Check content: %s", content).contains("Test document");
  }
}
