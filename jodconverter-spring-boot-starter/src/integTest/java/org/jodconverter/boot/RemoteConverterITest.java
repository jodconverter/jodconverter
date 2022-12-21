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

package org.jodconverter.boot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.remote.RemoteConverter;

/** Contains tests for the {@link RemoteConverter} class. */
@SpringBootTest
@TestPropertySource(locations = "classpath:config/application-remote.properties")
class RemoteConverterITest {

  private static final String RESOURCES_PATH = "src/integTest/resources/";
  private static final String SOURCE_FILE_PATH = RESOURCES_PATH + "documents/test1.doc";

  private static final String SERVER_KEYSTORE_PATH = RESOURCES_PATH + "serverkeystore.jks";
  private static final String SERVER_KEYSTORE_PWD = "serverkeystore";

  private static final String SERVER_TRUSTSTORE_PATH = RESOURCES_PATH + "servertruststore.jks";
  private static final String SERVER_TRUSTSTORE_PWD = "servertruststore";

  @Autowired private DocumentConverter converter;

  @Test
  void execute_FromFileToFileReturning200OK_TargetShouldContaingExpectedResult(
      final @TempDir File testFolder) throws OfficeException, IOException {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder, "out.txt");

    final WireMockServer wireMockServer =
        new WireMockServer(
            wireMockConfig()
                .port(8000)
                .httpsPort(8001)
                .keystorePath(SERVER_KEYSTORE_PATH)
                .keystorePassword(SERVER_KEYSTORE_PWD)
                .keyManagerPassword(SERVER_KEYSTORE_PWD)
                .trustStorePath(SERVER_TRUSTSTORE_PATH)
                .trustStorePassword(SERVER_TRUSTSTORE_PWD)
                .needClientAuth(true));
    wireMockServer.start();
    try {

      wireMockServer.stubFor(
          post(urlPathEqualTo("/lool/convert-to/txt"))
              .willReturn(aResponse().withStatus(200).withBody("Test document")));

      // Try to converter the input document
      converter.convert(inputFile).to(outputFile).execute();

      // Check that the output file was created with the expected content.
      final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
      assertThat(content).as("Check content: %s", content).contains("Test document");

      // Verify that it is actually the remote converter that did the conversion.
      configureFor(wireMockServer.port());
      verify(postRequestedFor(urlPathEqualTo("/lool/convert-to/txt")));
    } finally {
      wireMockServer.stop();
    }
  }
}
