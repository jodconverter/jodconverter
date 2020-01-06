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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.jodconverter.DocumentConverter;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:config/application-online.properties")
public class OnlineConverterITest {

  private static final String RESOURCES_PATH = "src/integTest/resources/";
  private static final String SOURCE_FILE_PATH = RESOURCES_PATH + "documents/test1.doc";

  private static final String SERVER_KEYSTORE_PATH = RESOURCES_PATH + "serverkeystore.jks";
  private static final String SERVER_KEYSTORE_PWD = "serverkeystore";

  private static final String SERVER_TRUSTSTORE_PATH = RESOURCES_PATH + "servertruststore.jks";
  private static final String SERVER_TRUSTSTORE_PWD = "servertruststore";

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  @Autowired private DocumentConverter converter;

  @Rule
  public WireMockRule wireMockRule =
      new WireMockRule(
          options()
              .port(8000)
              .httpsPort(8001)
              .keystorePath(SERVER_KEYSTORE_PATH)
              .keystorePassword(SERVER_KEYSTORE_PWD)
              .trustStorePath(SERVER_TRUSTSTORE_PATH)
              .trustStorePassword(SERVER_TRUSTSTORE_PWD)
              .needClientAuth(true));

  @Test
  public void execute_FromFileToFileReturning200OK_TargetShouldContaingExpectedResult()
      throws Exception {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder.getRoot(), "out.txt");

    assertThat(outputFile).doesNotExist();

    stubFor(
        post(urlPathEqualTo("/lool/convert-to/txt"))
            .willReturn(aResponse().withStatus(200).withBody("Test document")));

    // Try to converter the input document
    converter.convert(inputFile).to(outputFile).execute();

    // Check that the output file was created with the expected content.
    final String content = FileUtils.readFileToString(outputFile, Charset.forName("UTF-8"));
    assertThat(content).contains("Test document");

    // Verify that a it is actually the online converter that did the conversion.
    verify(postRequestedFor(urlPathEqualTo("/lool/convert-to/txt")));
  }
}
