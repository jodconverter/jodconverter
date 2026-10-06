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

package org.jodconverter.remote.task;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.core.pdf.PdfVersion;
import org.jodconverter.remote.RemoteConverter;
import org.jodconverter.remote.office.RemoteOfficeManager;

/** Contains tests for the {@link RemoteOfficeManager} class. */
class RemoteConversionTaskITest {

  private static final String RESOURCES_PATH = "src/integTest/resources/";
  private static final String SOURCE_FILE_PATH = RESOURCES_PATH + "documents/test1.doc";

  @Nested
  class Execute {

    @Test
    void withCustomProperties_ShouldHavePropertiesAsParameters(final @TempDir File testFolder)
        throws OfficeException {

      final var inputFile = new File(SOURCE_FILE_PATH);
      final var outputFile = new File(testFolder, "out.pdf");

      final var wireMockServer = new WireMockServer(options().port(8000));
      wireMockServer.start();
      try {
        final var manager =
            RemoteOfficeManager.builder()
                .urlConnection("http://localhost:8000/lool/convert-to/")
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/pdf")).willReturn(aResponse().withStatus(200)));

          final var filterData = new HashMap<String, Object>();
          filterData.put("PageRange", "2");
          filterData.put("RestrictPermissions", true);
          filterData.put("Printing", 0);
          final var customProperties = new HashMap<String, Object>();
          customProperties.put("FilterData", filterData);
          customProperties.put("Overwrite", true);
          customProperties.put("Primitive", 10);
          customProperties.put("NonStringNorPrimitive", Collections.EMPTY_LIST);

          final var builder = DocumentFormat.builder(DefaultDocumentFormatRegistry.PDF);
          customProperties.forEach(
              (name, value) -> builder.storeProperty(DocumentFamily.TEXT, name, value));
          final var pdf = builder.build();
          RemoteConverter.make(manager).convert(inputFile).to(outputFile).as(pdf).execute();

          wireMockServer.verify(
              postRequestedFor(urlPathEqualTo("/lool/convert-to/pdf"))
                  .withQueryParam("sFilterName", equalTo("writer_pdf_Export"))
                  .withQueryParam("sOverwrite", equalTo("true"))
                  .withQueryParam("sPrimitive", equalTo("10"))
                  .withQueryParam("sNonStringNorPrimitive", equalTo("[]"))
                  .withQueryParam("sfdPageRange", equalTo("2"))
                  .withQueryParam("sfdRestrictPermissions", equalTo("true"))
                  .withQueryParam("sfdPrinting", equalTo("0")));

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {
        wireMockServer.stop();
      }
    }

    @Test
    void withTargetOptions_ShouldSendThemWithPrecedenceOverTheTargetFormat(
        final @TempDir File testFolder) throws OfficeException {

      final var inputFile = new File(SOURCE_FILE_PATH);
      final var outputFile = new File(testFolder, "out.pdf");

      final var wireMockServer = new WireMockServer(options().port(8000));
      wireMockServer.start();
      try {
        final var manager =
            RemoteOfficeManager.builder()
                .urlConnection("http://localhost:8000/lool/convert-to/")
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/pdf")).willReturn(aResponse().withStatus(200)));

          final var filterData = new HashMap<String, Object>();
          filterData.put("PageRange", "2");
          filterData.put("SelectPdfVersion", 16);
          final var pdf =
              DocumentFormat.builder(DefaultDocumentFormatRegistry.PDF)
                  .storeProperty(DocumentFamily.TEXT, "FilterData", filterData)
                  .build();

          RemoteConverter.make(manager)
              .convert(inputFile)
              .to(outputFile)
              .as(pdf)
              .with(PdfOptions.builder().version(PdfVersion.PDF_A_2B).tagged(true).build())
              .execute();

          wireMockServer.verify(
              postRequestedFor(urlPathEqualTo("/lool/convert-to/pdf"))
                  .withQueryParam("sFilterName", equalTo("writer_pdf_Export"))
                  .withQueryParam("sfdPageRange", equalTo("2"))
                  .withQueryParam("sfdSelectPdfVersion", equalTo("2"))
                  .withQueryParam("sfdUseTaggedPDF", equalTo("true")));

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {
        wireMockServer.stop();
      }
    }

    @Test
    void withLoadProperties_ShouldSendSourceFormatLoadPropertiesOnly(final @TempDir File testFolder)
        throws OfficeException {

      final var inputFile = new File(SOURCE_FILE_PATH);
      final var outputFile = new File(testFolder, "out.pdf");

      final var wireMockServer = new WireMockServer(options().port(8000));
      wireMockServer.start();
      try {
        final var manager =
            RemoteOfficeManager.builder()
                .urlConnection("http://localhost:8000/lool/convert-to/")
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/pdf")).willReturn(aResponse().withStatus(200)));

          // Load properties are used to load the source document
          final var doc =
              DocumentFormat.builder()
                  .from(DefaultDocumentFormatRegistry.DOC)
                  .loadProperty("Password", "secret")
                  .build();
          final var pdf =
              DocumentFormat.builder()
                  .from(DefaultDocumentFormatRegistry.PDF)
                  .loadProperty("TargetOnly", "ignored")
                  .build();
          RemoteConverter.make(manager).convert(inputFile).as(doc).to(outputFile).as(pdf).execute();

          wireMockServer.verify(
              postRequestedFor(urlPathEqualTo("/lool/convert-to/pdf"))
                  .withQueryParam("lPassword", equalTo("secret"))
                  .withoutQueryParam("lTargetOnly"));

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {
        wireMockServer.stop();
      }
    }

    @Test
    void withFilterDataNotMap_ShouldHaveNormalFilterDataPropertyAsParameters(
        final @TempDir File testFolder) throws OfficeException {

      final var inputFile = new File(SOURCE_FILE_PATH);
      final var outputFile = new File(testFolder, "out.pdf");

      final var wireMockServer = new WireMockServer(options().port(8000));
      wireMockServer.start();
      try {
        final var manager =
            RemoteOfficeManager.builder()
                .urlConnection("http://localhost:8000/lool/convert-to/")
                .build();
        try {
          manager.start();
          wireMockServer.stubFor(
              post(urlPathEqualTo("/lool/convert-to/pdf")).willReturn(aResponse().withStatus(200)));

          final var customProperties = new HashMap<String, Object>();
          customProperties.put("FilterData", "foo");

          final var builder = DocumentFormat.builder(DefaultDocumentFormatRegistry.PDF);
          customProperties.forEach(
              (name, value) -> builder.storeProperty(DocumentFamily.TEXT, name, value));
          final var pdf = builder.build();
          RemoteConverter.make(manager).convert(inputFile).to(outputFile).as(pdf).execute();

          wireMockServer.verify(
              postRequestedFor(urlPathEqualTo("/lool/convert-to/pdf"))
                  .withQueryParam("sFilterName", equalTo("writer_pdf_Export"))
                  .withQueryParam("sFilterData", equalTo("foo")));

        } finally {
          OfficeUtils.stopQuietly(manager);
        }
      } finally {
        wireMockServer.stop();
      }
    }
  }
}
