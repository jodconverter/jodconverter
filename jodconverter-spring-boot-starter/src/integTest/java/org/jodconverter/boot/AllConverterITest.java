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

package org.jodconverter.boot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.jodconverter.DocumentConverter;

@SpringBootTest
@TestPropertySource(locations = "classpath:config/application-all.properties")
public class AllConverterITest {

  private static final String RESOURCES_PATH = "src/integTest/resources/";
  private static final String SOURCE_FILE_PATH = RESOURCES_PATH + "documents/test1.doc";

  @Resource(name = "localDocumentConverter")
  private DocumentConverter localConverter;

  @Resource(name = "onlineDocumentConverter")
  private DocumentConverter onlineConverter;

  @Test
  public void execute_UsingLocalConverter_TargetShouldContaingExpectedResult(
      @TempDir File testFolder) throws Exception {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder, "local_out.txt");

    final WireMockServer wireMockServer = new WireMockServer(options().port(8000));
    wireMockServer.start();
    try {
      localConverter.convert(inputFile).to(outputFile).execute();

      // Check that the output file was created with the expected content.
      final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
      assertThat(content).contains("Test document");
    } finally {
      wireMockServer.stop();
    }
  }

  @Test
  public void execute_UsingOnlineConverter_TargetShouldContaingExpectedResult(
      @TempDir File testFolder) throws Exception {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder, "online_out.txt");

    final WireMockServer wireMockServer = new WireMockServer(options().port(8000));
    wireMockServer.start();
    try {
      wireMockServer.stubFor(
          post(urlPathEqualTo("/lool/convert-to/txt"))
              .willReturn(aResponse().withStatus(200).withBody("Test document")));

      // Try to converter the input document
      onlineConverter.convert(inputFile).to(outputFile).execute();

      // Check that the output file was created with the expected content.
      final String content = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
      assertThat(content).contains("Test document");

      // Verify that a it is actually the online converter that did the conversion.
      configureFor(wireMockServer.port());
      verify(postRequestedFor(urlPathEqualTo("/lool/convert-to/txt")));
    } finally {
      wireMockServer.stop();
    }
  }
}
