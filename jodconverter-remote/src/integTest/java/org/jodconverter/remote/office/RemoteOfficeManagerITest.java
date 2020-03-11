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

package org.jodconverter.remote.office;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.office.SimpleOfficeTask;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.remote.RemoteConverter;

/** Contains tests for the {@link RemoteOfficeManager} class. */
public class RemoteOfficeManagerITest {

  private static final String RESOURCES_PATH = "src/integTest/resources/";
  private static final String SOURCE_FILE_PATH = RESOURCES_PATH + "documents/test1.doc";

  @Test
  public void install_ShouldSetInstalledOfficeManagerHolder() {

    // Ensure we do not replace the current installed manager
    final OfficeManager installedManager = InstalledOfficeManagerHolder.getInstance();
    try {
      final OfficeManager manager = RemoteOfficeManager.install("localhost");
      assertThat(InstalledOfficeManagerHolder.getInstance()).isEqualTo(manager);
    } finally {
      InstalledOfficeManagerHolder.setInstance(installedManager);
    }
  }

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = RemoteOfficeManager.make("localhost");

    assertThat(manager).isInstanceOf(RemoteOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(RemoteOfficeManagerPoolEntry.class)
                    .extracting(
                        "connectionUrl",
                        "sslConfig",
                        "connectTimeout",
                        "socketTimeout",
                        "taskExecutionTimeout")
                    .containsExactly("localhost", null, 60_000L, 120_000L, 120_000L));
  }

  @Test
  public void build_WithNullValues_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager =
        RemoteOfficeManager.builder()
            .workingDir((File) null)
            .workingDir((String) null)
            .poolSize(null)
            .urlConnection("localhost")
            .connectTimeout(null)
            .socketTimeout(null)
            .taskExecutionTimeout(null)
            .taskQueueTimeout(null)
            .build();

    assertThat(manager).isInstanceOf(RemoteOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(RemoteOfficeManagerPoolEntry.class)
                    .extracting(
                        "connectionUrl",
                        "sslConfig",
                        "connectTimeout",
                        "socketTimeout",
                        "taskExecutionTimeout")
                    .containsExactly("localhost", null, 60_000L, 120_000L, 120_000L));
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues() {

    final OfficeManager manager =
        RemoteOfficeManager.builder()
            .workingDir(OfficeUtils.getDefaultWorkingDir())
            .poolSize(5)
            .urlConnection("localhost")
            .connectTimeout(50_000L)
            .socketTimeout(40_000L)
            .taskExecutionTimeout(20_000L)
            .taskQueueTimeout(1_000L)
            .build();

    assertThat(manager).isInstanceOf(RemoteOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 1_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(5)
        .allSatisfy(
            o ->
                assertThat(o)
                    .isInstanceOf(RemoteOfficeManagerPoolEntry.class)
                    .extracting(
                        "connectionUrl",
                        "sslConfig",
                        "connectTimeout",
                        "socketTimeout",
                        "taskExecutionTimeout")
                    .containsExactly("localhost", null, 50_000L, 40_000L, 20_000L));
  }

  @Test
  public void build_WithValuesAsString_ShouldInitializedOfficeManagerWithCustomValues() {

    final OfficeManager manager =
        RemoteOfficeManager.builder()
            .urlConnection("localhost")
            .workingDir(OfficeUtils.getDefaultWorkingDir().getPath())
            .build();

    assertThat(manager).isInstanceOf(RemoteOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(RemoteOfficeManagerPoolEntry.class)
                    .extracting(
                        "connectionUrl",
                        "sslConfig",
                        "connectTimeout",
                        "socketTimeout",
                        "taskExecutionTimeout")
                    .containsExactly("localhost", null, 60_000L, 120_000L, 120_000L));
  }

  @Test
  public void build_WithEmptyValuesAsString_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager =
        RemoteOfficeManager.builder().urlConnection("localhost").workingDir("   ").build();

    assertThat(manager).isInstanceOf(RemoteOfficeManager.class);
    assertThat(manager)
        .extracting("workingDir", "taskQueueTimeout")
        .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .isInstanceOf(RemoteOfficeManagerPoolEntry.class)
                    .extracting(
                        "connectionUrl",
                        "sslConfig",
                        "connectTimeout",
                        "socketTimeout",
                        "taskExecutionTimeout")
                    .containsExactly("localhost", null, 60_000L, 120_000L, 120_000L));
  }

  @Test
  public void build_WithMissingUrlConnection_ThrowIllegalArgumentException() {

    assertThatNullPointerException().isThrownBy(() -> RemoteOfficeManager.builder().build());
  }

  @Test
  public void start_StartTwice_ThrowIllegalStateException() throws OfficeException {

    final RemoteOfficeManager manager = RemoteOfficeManager.make("localhost");
    try {
      manager.start();
      assertThatIllegalStateException().isThrownBy(manager::start);
    } finally {
      manager.stop();
    }
  }

  @Test
  public void start_WhenTerminated_ThrowIllegalStateException() throws OfficeException {

    final RemoteOfficeManager manager = RemoteOfficeManager.make("localhost");
    manager.start();
    manager.stop();
    assertThatIllegalStateException().isThrownBy(manager::start);
  }

  @Test
  public void stop_WhenTerminated_SecondStopIgnored() throws OfficeException {

    final RemoteOfficeManager manager = RemoteOfficeManager.make("localhost");
    manager.start();
    manager.stop();
    assertThatCode(manager::stop).doesNotThrowAnyException();
  }

  @Test
  public void execute_WithoutBeeingStarted_ThrowIllegalStateException() {

    assertThatIllegalStateException()
        .isThrownBy(() -> RemoteOfficeManager.make("localhost").execute(new SimpleOfficeTask()));
  }

  @Test
  public void execute_WhenTerminated_ThrowIllegalStateException() throws OfficeException {

    final RemoteOfficeManager manager = RemoteOfficeManager.make("localhost");
    try {
      manager.start();
    } finally {
      manager.stop();
    }

    assertThatIllegalStateException().isThrownBy(() -> manager.execute(new SimpleOfficeTask()));
  }

  @Test
  public void execute_WithBadUrl_ThrowOfficeException() throws OfficeException {

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
  public void execute_WhenReturnNot200OK_ShouldThrowOfficeException(final @TempDir File testFolder)
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
  public void execute_FromFileToFileReturning200OK_TargetShouldContaingExpectedResult(
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
  public void
      execute_FromInputStreamToOutputStreamReturning200OK_TargetShouldContaingExpectedResult(
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
