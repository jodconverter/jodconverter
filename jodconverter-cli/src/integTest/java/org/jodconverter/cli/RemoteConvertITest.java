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

package org.jodconverter.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.cli.util.ExitException;
import org.jodconverter.cli.util.NoExitExtension;
import org.jodconverter.cli.util.ResetExitExceptionExtension;

/**
 * This class tests the {@link Convert} class, which contains the main function of the cli module.
 */
@ExtendWith({NoExitExtension.class, ResetExitExceptionExtension.class})
public class RemoteConvertITest {

  private static final String RESOURCES_PATH = "src/integTest/resources/";
  private static final String CONFIG_DIR = RESOURCES_PATH + "config/";
  private static final String SOURCE_FILE_DOC = RESOURCES_PATH + "documents/test1.doc";
  private static final String SERVER_KEYSTORE_PATH = RESOURCES_PATH + "serverkeystore.jks";
  private static final String SERVER_KEYSTORE_PWD = "serverkeystore";

  @Test
  public void convert_WithConnectionOption_ShouldSucceed(final @TempDir File testFolder) {

    final File inputFile = new File(SOURCE_FILE_DOC);
    final File outputFile = new File(testFolder, "out.txt");

    final WireMockServer wireMockServer = new WireMockServer(options().port(8000));
    wireMockServer.start();
    try {
      assertThatExceptionOfType(ExitException.class)
          .isThrownBy(
              () -> {
                wireMockServer.stubFor(
                    post(urlPathEqualTo("/lool/convert-to/txt"))
                        .willReturn(aResponse().withBody("Test Document")));

                Convert.main(
                    new String[] {
                      inputFile.getPath(),
                      outputFile.getPath(),
                      "-c",
                      "http://localhost:8000/lool/convert-to/"
                    });
              })
          .satisfies(
              e -> {
                assertThat(e).hasFieldOrPropertyWithValue("status", 0);

                try {
                  final String content =
                      FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
                  assertThat(content).as("Check content: %s", content).contains("Test Document");
                } catch (IOException ex) {
                  assertThat(ex).isNull();
                }
              });
    } finally {
      wireMockServer.stop();
    }
  }

  @Test
  public void convert_WithConnectionOptionAndSslConfig_ShouldSucceed(
      final @TempDir File testFolder) {

    final File inputFile = new File(SOURCE_FILE_DOC);
    final File outputFile = new File(testFolder, "out.txt");
    final File contextFile = new File(CONFIG_DIR + "applicationContext_sslConfig.xml");

    final WireMockServer wireMockServer =
        new WireMockServer(
            options()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD));
    wireMockServer.start();
    try {
      assertThatExceptionOfType(ExitException.class)
          .isThrownBy(
              () -> {
                wireMockServer.stubFor(
                    post(urlPathEqualTo("/lool/convert-to/txt"))
                        .willReturn(aResponse().withBody("Test Document")));

                Convert.main(
                    new String[] {
                      inputFile.getPath(),
                      outputFile.getPath(),
                      "-c",
                      "https://localhost:8001/lool/convert-to/",
                      "-a",
                      contextFile.getPath()
                    });
              })
          .satisfies(
              e -> {
                assertThat(e).hasFieldOrPropertyWithValue("status", 0);

                try {
                  final String content =
                      FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
                  assertThat(content).as("Check content: %s", content).contains("Test Document");
                } catch (IOException ex) {
                  assertThat(ex).isNull();
                }
              });
    } finally {
      wireMockServer.stop();
    }
  }
}
