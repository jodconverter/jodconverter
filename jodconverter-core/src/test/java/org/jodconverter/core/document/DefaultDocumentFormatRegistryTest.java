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

package org.jodconverter.core.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link DefaultDocumentFormatRegistry} class. */
class DefaultDocumentFormatRegistryTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(DefaultDocumentFormatRegistry.class);
  }

  private void assertExpectedExtensions(
      final SoftAssertions soft, final Set<DocumentFormat> formats, final String... extensions) {

    soft.assertThat(formats).hasSize(extensions.length);
    formats.forEach(format -> soft.assertThat(format.getExtension()).isIn((Object[]) extensions));
  }

  /** Tests all the default output formats are load successfully. */
  @Test
  void getInstance_AllOutputFormatsLoadedSuccessfully() {

    try (AutoCloseableSoftAssertions ass = new AutoCloseableSoftAssertions()) {
      // TEXT output format
      assertExpectedExtensions(
          ass,
          getOutputFormats(DocumentFamily.TEXT),
          "doc",
          "docx",
          "html",
          "xhtml",
          "jpg",
          "odt",
          "ott",
          "fodt",
          "pdf",
          "png",
          "rtf",
          "sxw",
          "txt");

      // SPREADSHEET output format
      assertExpectedExtensions(
          ass,
          getOutputFormats(DocumentFamily.SPREADSHEET),
          "csv",
          "html",
          "xhtml",
          "jpg",
          "ods",
          "ots",
          "fods",
          "pdf",
          "png",
          "sxc",
          "tsv",
          "xls",
          "xlsx");

      // PRESENTATION output format
      assertExpectedExtensions(
          ass,
          getOutputFormats(DocumentFamily.PRESENTATION),
          "gif",
          "html",
          "xhtml",
          "jpg",
          "odp",
          "otp",
          "fodp",
          "pdf",
          "png",
          "ppt",
          "pptx",
          "swf",
          "sxi",
          "tif",
          "bmp");

      // DRAWING output format
      assertExpectedExtensions(
          ass,
          getOutputFormats(DocumentFamily.DRAWING),
          "gif",
          "jpg",
          "odg",
          "otg",
          "fodg",
          "pdf",
          "png",
          "svg",
          "swf",
          "tif",
          "vsd",
          "vsdx",
          "bmp");
    }
  }

  private void assertByExt(
      final SoftAssertions soft, final String ext, final DocumentFormat expected) {
    soft.assertThat(getFormatByExtension(ext)).isEqualTo(expected);
  }

  @Test
  void getFormatByExtension_AllFormatsLoadedSuccessfully() {

    try (AutoCloseableSoftAssertions ass = new AutoCloseableSoftAssertions()) {
      assertByExt(ass, "pdf", PDF);
      assertByExt(ass, "swf", SWF);
      assertByExt(ass, "html", HTML);
      assertByExt(ass, "xhtml", XHTML);
      assertByExt(ass, "odt", ODT);
      assertByExt(ass, "ott", OTT);
      assertByExt(ass, "fodt", FODT);
      assertByExt(ass, "sxw", SXW);
      assertByExt(ass, "doc", DOC);
      assertByExt(ass, "docx", DOCX);
      assertByExt(ass, "rtf", RTF);
      assertByExt(ass, "wpd", WPD);
      assertByExt(ass, "txt", TXT);
      assertByExt(ass, "ods", ODS);
      assertByExt(ass, "ots", OTS);
      assertByExt(ass, "fods", FODS);
      assertByExt(ass, "sxc", SXC);
      assertByExt(ass, "xls", XLS);
      assertByExt(ass, "xlsx", XLSX);
      assertByExt(ass, "csv", CSV);
      assertByExt(ass, "tsv", TSV);
      assertByExt(ass, "odp", ODP);
      assertByExt(ass, "otp", OTP);
      assertByExt(ass, "fodp", FODP);
      assertByExt(ass, "sxi", SXI);
      assertByExt(ass, "ppt", PPT);
      assertByExt(ass, "pptx", PPTX);
      assertByExt(ass, "odg", ODG);
      assertByExt(ass, "otg", OTG);
      assertByExt(ass, "fodg", FODG);
      assertByExt(ass, "svg", SVG);
      assertByExt(ass, "png", PNG);
      assertByExt(ass, "jpg", JPEG);
      assertByExt(ass, "jpeg", JPEG);
      assertByExt(ass, "tif", TIFF);
      assertByExt(ass, "tiff", TIFF);
      assertByExt(ass, "gif", GIF);
      assertByExt(ass, "bmp", BMP);
      assertByExt(ass, "vsd", VSD);
      assertByExt(ass, "vsdx", VSDX);
    }
  }

  private void assertByType(
      final SoftAssertions soft, final String mediaType, final DocumentFormat expected) {
    soft.assertThat(getFormatByMediaType(mediaType)).isEqualTo(expected);
  }

  @Test
  void getFormatByMediaType_AllFormatsLoadedSuccessfully() {

    try (AutoCloseableSoftAssertions ass = new AutoCloseableSoftAssertions()) {
      assertByType(ass, "application/pdf", PDF);
      assertByType(ass, "application/x-shockwave-flash", SWF);
      assertByType(ass, "text/html", HTML);
      assertByType(ass, "application/xhtml+xml", XHTML);
      assertByType(ass, "application/vnd.oasis.opendocument.text", ODT);
      assertByType(ass, "application/vnd.oasis.opendocument.text-template", OTT);
      assertByType(ass, "application/vnd.oasis.opendocument.text-flat-xml", FODT);
      assertByType(ass, "application/vnd.sun.xml.writer", SXW);
      assertByType(ass, "application/msword", DOC);
      assertByType(
          ass, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", DOCX);
      assertByType(ass, "text/rtf", RTF);
      assertByType(ass, "application/wordperfect", WPD);
      assertByType(ass, "text/plain", TXT);
      assertByType(ass, "application/vnd.oasis.opendocument.spreadsheet", ODS);
      assertByType(ass, "application/vnd.oasis.opendocument.spreadsheet-template", OTS);
      assertByType(ass, "application/vnd.oasis.opendocument.spreadsheet-flat-xml", FODS);
      assertByType(ass, "application/vnd.sun.xml.calc", SXC);
      assertByType(ass, "application/vnd.ms-excel", XLS);
      assertByType(ass, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", XLSX);
      assertByType(ass, "text/csv", CSV);
      assertByType(ass, "text/tab-separated-values", TSV);
      assertByType(ass, "application/vnd.oasis.opendocument.presentation", ODP);
      assertByType(ass, "application/vnd.oasis.opendocument.presentation-template", OTP);
      assertByType(ass, "application/vnd.oasis.opendocument.presentation-flat-xml", FODP);
      assertByType(ass, "application/vnd.sun.xml.impress", SXI);
      assertByType(ass, "application/vnd.ms-powerpoint", PPT);
      assertByType(
          ass, "application/vnd.openxmlformats-officedocument.presentationml.presentation", PPTX);
      assertByType(ass, "application/vnd.oasis.opendocument.graphics", ODG);
      assertByType(ass, "application/vnd.oasis.opendocument.graphics-template", OTG);
      assertByType(ass, "application/vnd.oasis.opendocument.graphics-flat-xml", FODG);
      assertByType(ass, "image/svg+xml", SVG);
      assertByType(ass, "image/png", PNG);
      assertByType(ass, "image/jpeg", JPEG);
      assertByType(ass, "image/gif", GIF);
      assertByType(ass, "image/tiff", TIFF);
      assertByType(ass, "image/bmp", BMP);
      assertByType(ass, "application/vnd-visio", VSD);
      assertByType(ass, "application/vnd-ms-visio.drawing", VSDX);
    }
  }

  @Test
  void getFormatX_ReturnReadOnlyFormat() {

    final DocumentFormat format = CSV;

    assertThat(format.getLoadProperties()).isNotNull();
    assertThat(format.getStoreProperties()).isNotNull();

    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> format.getLoadProperties().put("newKey", "newValue"));

    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> format.getStoreProperties().put(DocumentFamily.DRAWING, new HashMap<>()));

    final Map<String, Object> map = format.getStoreProperties(DocumentFamily.SPREADSHEET);
    assertThat(map).isNotNull();

    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> map.put("newKey", "newValue"));
  }
}
