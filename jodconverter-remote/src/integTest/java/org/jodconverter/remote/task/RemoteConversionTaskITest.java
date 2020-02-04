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

package org.jodconverter.remote.task;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.remote.RemoteConverter;
import org.jodconverter.remote.office.RemoteOfficeManager;

/** Contains tests for the {@link RemoteOfficeManager} class. */
public class RemoteConversionTaskITest {

  private static final String RESOURCES_PATH = "src/integTest/resources/";
  private static final String SOURCE_FILE_PATH = RESOURCES_PATH + "documents/test1.doc";

  @Test
  public void execute_WithCustomProperties_ShouldHavePropertiesAsParameters(
      final @TempDir File testFolder) throws OfficeException {

    final File inputFile = new File(SOURCE_FILE_PATH);
    final File outputFile = new File(testFolder, "out.pdf");

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
            post(urlPathEqualTo("/lool/convert-to/pdf")).willReturn(aResponse().withStatus(200)));

        final Map<String, Object> filterData = new HashMap<>();
        filterData.put("PageRange", "2");
        final Map<String, Object> customProperties = new HashMap<>();
        customProperties.put("FilterData", filterData);

        final DocumentFormat pdf = DocumentFormat.copy(DefaultDocumentFormatRegistry.PDF);
        pdf.getStoreProperties(DocumentFamily.TEXT).putAll(customProperties);
        RemoteConverter.make(manager).convert(inputFile).to(outputFile).as(pdf).execute();

        wireMockServer.verify(
            postRequestedFor(urlPathEqualTo("/lool/convert-to/pdf"))
                .withQueryParam("sFilterName", equalTo("writer_pdf_Export"))
                .withQueryParam("sfdPageRange", equalTo("2")));

      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    } finally {
      wireMockServer.stop();
    }
  }
}
