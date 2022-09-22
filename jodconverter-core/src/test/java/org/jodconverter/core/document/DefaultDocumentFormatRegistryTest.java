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
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.BMP;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.CSV;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.DOC;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.DOCX;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.DOTX;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.FODG;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.FODP;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.FODS;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.FODT;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.GIF;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.HTML;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.JPEG;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.ODG;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.ODP;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.ODS;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.ODT;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.OTG;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.OTP;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.OTS;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.OTT;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.PDF;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.PNG;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.POTX;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.PPT;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.PPTX;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.RTF;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.SVG;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.SWF;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.SXC;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.SXI;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.SXW;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.TIFF;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.TSV;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.TXT;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.VSD;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.VSDX;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.WPD;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.XHTML;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.XLS;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.XLSX;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.XLTX;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.getFormatByExtension;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.getFormatByMediaType;
import static org.jodconverter.core.document.DefaultDocumentFormatRegistry.getOutputFormats;

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
          "odt",
          "ott",
          "fodt",
          "docx",
          "dotx",
          "doc",
          "html",
          "xhtml",
          "rtf",
          "txt",
          "sxw",
          "pdf",
          "jpg",
          "png",
          "svg");

      // SPREADSHEET output format
      assertExpectedExtensions(
          ass,
          getOutputFormats(DocumentFamily.SPREADSHEET),
          "html",
          "xhtml",
          "ods",
          "ots",
          "fods",
          "xlsx",
          "xltx",
          "xls",
          "csv",
          "tsv",
          "sxc",
          "pdf",
          "jpg",
          "png",
          "svg");

      // PRESENTATION output format
      assertExpectedExtensions(
          ass,
          getOutputFormats(DocumentFamily.PRESENTATION),
          "html",
          "xhtml",
          "odp",
          "otp",
          "fodp",
          "pptx",
          "potx",
          "ppt",
          "sxi",
          "pdf",
          "swf",
          "bmp",
          "gif",
          "jpg",
          "png",
          "svg",
          "tif");

      // DRAWING output format
      assertExpectedExtensions(
          ass,
          getOutputFormats(DocumentFamily.DRAWING),
          "odg",
          "otg",
          "fodg",
          "pdf",
          "swf",
          "vsdx",
          "vsd",
          "bmp",
          "gif",
          "jpg",
          "png",
          "svg",
          "tif");

      // WEB output format
      assertExpectedExtensions(
          ass, getOutputFormats(DocumentFamily.WEB), "odt", "ott", "pdf", "jpg", "png", "svg");
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
      assertByExt(ass, "dotx", DOTX);
      assertByExt(ass, "rtf", RTF);
      assertByExt(ass, "wpd", WPD);
      assertByExt(ass, "txt", TXT);
      assertByExt(ass, "ods", ODS);
      assertByExt(ass, "ots", OTS);
      assertByExt(ass, "fods", FODS);
      assertByExt(ass, "sxc", SXC);
      assertByExt(ass, "xls", XLS);
      assertByExt(ass, "xlsx", XLSX);
      assertByExt(ass, "xltx", XLTX);
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
      assertByType(
          ass, "application/vnd.openxmlformats-officedocument.wordprocessingml.template", DOTX);
      assertByType(ass, "text/rtf", RTF);
      assertByType(ass, "application/wordperfect", WPD);
      assertByType(ass, "text/plain", TXT);
      assertByType(ass, "application/vnd.oasis.opendocument.spreadsheet", ODS);
      assertByType(ass, "application/vnd.oasis.opendocument.spreadsheet-template", OTS);
      assertByType(ass, "application/vnd.oasis.opendocument.spreadsheet-flat-xml", FODS);
      assertByType(ass, "application/vnd.sun.xml.calc", SXC);
      assertByType(ass, "application/vnd.ms-excel", XLS);
      assertByType(ass, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", XLSX);
      assertByType(
          ass, "application/vnd.openxmlformats-officedocument.spreadsheetml.template", XLTX);
      assertByType(ass, "text/csv", CSV);
      assertByType(ass, "text/tab-separated-values", TSV);
      assertByType(ass, "application/vnd.oasis.opendocument.presentation", ODP);
      assertByType(ass, "application/vnd.oasis.opendocument.presentation-template", OTP);
      assertByType(ass, "application/vnd.oasis.opendocument.presentation-flat-xml", FODP);
      assertByType(ass, "application/vnd.sun.xml.impress", SXI);
      assertByType(ass, "application/vnd.ms-powerpoint", PPT);
      assertByType(
          ass, "application/vnd.openxmlformats-officedocument.presentationml.presentation", PPTX);
      assertByType(
          ass, "application/vnd.openxmlformats-officedocument.presentationml.template", POTX);
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
