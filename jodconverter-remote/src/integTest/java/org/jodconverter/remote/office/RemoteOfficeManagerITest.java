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

package org.jodconverter.remote.office;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.task.SimpleOfficeTask;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.remote.RemoteConverter;

/** Contains tests for the {@link RemoteOfficeManager} class. */
class RemoteOfficeManagerITest {

  private static final String RESOURCES_PATH = "src/integTest/resources/";
  private static final String SOURCE_FILE_PATH = RESOURCES_PATH + "documents/test1.doc";

  @Nested
  class Execute {
    @Test
    void withBadUrl_ShouldThrowOfficeException() throws OfficeException {

      final RemoteOfficeManager manager =
          RemoteOfficeManager.builder().urlConnection("url_that_could_not_work").build();
      try {
        manager.start();

        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(() -> manager.execute(new SimpleOfficeTask()));

      } finally {
        manager.stop();
      }
    }

    @Test
    void whenReturnNot200OK_ShouldThrowOfficeException(final @TempDir File testFolder)
        throws OfficeException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer = new WireMockServer(options().port(8000));
      wireMockServer.start();
      try {
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("http://localhost:8000/lool/convert-to/")
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt")).willReturn(aResponse().withStatus(400)));

          assertThatExceptionOfType(OfficeException.class)
              .isThrownBy(
                  () -> RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute());

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {
        wireMockServer.stop();
      }
    }

    @Test
    void fromFileToFileReturning200OK_TargetShouldContaingExpectedResult(
        final @TempDir File testFolder) throws OfficeException, IOException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer = new WireMockServer(options().port(8000));
      wireMockServer.start();
      try {
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("http://localhost:8000/lool/convert-to/")
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withStatus(200).withBody("Test Document")));

          // Try to converter the input document
          RemoteConverter.make(manager).convert(inputFile).to(outputFile).execute();

          // Check that the output file was created with the expected content.
          final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
          assertThat(content).as("Check content: %s", content).contains("Test Document");
        } finally {
          manager.stop();
        }
      } finally {
        wireMockServer.stop();
      }
    }

    @Test
    void fromInputStreamToOutputStreamReturning200OK_TargetShouldContainExpectedResult(
        final @TempDir File testFolder) throws OfficeException, IOException {

      final File inputFile = new File(SOURCE_FILE_PATH);
      final File outputFile = new File(testFolder, "out.txt");

      final WireMockServer wireMockServer = new WireMockServer(options().port(8000));
      wireMockServer.start();
      try {
        final OfficeManager manager =
            RemoteOfficeManager.builder()
                .urlConnection("http://localhost:8000/lool/convert-to/")
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/txt"))
                  .willReturn(aResponse().withStatus(200).withBody("Test Document")));

          // Try to converter the input document
          try (InputStream inputStream = Files.newInputStream(inputFile.toPath());
              OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
            RemoteConverter.make(manager)
                .convert(inputStream)
                .as(DefaultDocumentFormatRegistry.DOC)
                .to(outputStream)
                .as(DefaultDocumentFormatRegistry.TXT)
                .execute();
          }

          // Check that the output file was created with the expected content.
          final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
          assertThat(content).as("Check content: %s", content).contains("Test Document");
        } finally {
          manager.stop();
        }
      } finally {
        wireMockServer.stop();
      }
    }
  }
}
