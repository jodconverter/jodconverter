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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link DefaultDocumentFormatRegistry} class. */
public class DefaultDocumentFormatRegistryTest {

  @Test
  public void new_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(DefaultDocumentFormatRegistry.class);
  }

  private void assertExpectedExtensions(
      final Set<DocumentFormat> formats, final String... extensions) {

    assertThat(formats).hasSize(extensions.length);
    formats.forEach(format -> assertThat(format.getExtension()).isIn((Object[]) extensions));
  }

  /** Tests all the default output formats are load successfully. */
  @Test
  public void getInstance_AllOutputFormatsLoadedSuccessfully() {

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();

    // TEXT output format
    Set<DocumentFormat> outputFormats = registry.getOutputFormats(DocumentFamily.TEXT);
    assertExpectedExtensions(
        outputFormats,
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
    outputFormats = registry.getOutputFormats(DocumentFamily.SPREADSHEET);
    assertExpectedExtensions(
        outputFormats,
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
    outputFormats = registry.getOutputFormats(DocumentFamily.PRESENTATION);
    assertExpectedExtensions(
        outputFormats,
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
    outputFormats = registry.getOutputFormats(DocumentFamily.DRAWING);
    assertExpectedExtensions(
        outputFormats,
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

  @Test
  public void getFormatByExtension_AllFormatsLoadedSuccessfully() {

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("pdf"))
          .isEqualTo(DefaultDocumentFormatRegistry.PDF);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("swf"))
          .isEqualTo(DefaultDocumentFormatRegistry.SWF);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("html"))
          .isEqualTo(DefaultDocumentFormatRegistry.HTML);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("xhtml"))
          .isEqualTo(DefaultDocumentFormatRegistry.XHTML);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("odt"))
          .isEqualTo(DefaultDocumentFormatRegistry.ODT);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("ott"))
          .isEqualTo(DefaultDocumentFormatRegistry.OTT);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("fodt"))
          .isEqualTo(DefaultDocumentFormatRegistry.FODT);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("sxw"))
          .isEqualTo(DefaultDocumentFormatRegistry.SXW);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("doc"))
          .isEqualTo(DefaultDocumentFormatRegistry.DOC);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("docx"))
          .isEqualTo(DefaultDocumentFormatRegistry.DOCX);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("rtf"))
          .isEqualTo(DefaultDocumentFormatRegistry.RTF);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("wpd"))
          .isEqualTo(DefaultDocumentFormatRegistry.WPD);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("txt"))
          .isEqualTo(DefaultDocumentFormatRegistry.TXT);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("ods"))
          .isEqualTo(DefaultDocumentFormatRegistry.ODS);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("ots"))
          .isEqualTo(DefaultDocumentFormatRegistry.OTS);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("fods"))
          .isEqualTo(DefaultDocumentFormatRegistry.FODS);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("sxc"))
          .isEqualTo(DefaultDocumentFormatRegistry.SXC);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("xls"))
          .isEqualTo(DefaultDocumentFormatRegistry.XLS);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("xlsx"))
          .isEqualTo(DefaultDocumentFormatRegistry.XLSX);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("csv"))
          .isEqualTo(DefaultDocumentFormatRegistry.CSV);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("tsv"))
          .isEqualTo(DefaultDocumentFormatRegistry.TSV);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("odp"))
          .isEqualTo(DefaultDocumentFormatRegistry.ODP);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("otp"))
          .isEqualTo(DefaultDocumentFormatRegistry.OTP);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("fodp"))
          .isEqualTo(DefaultDocumentFormatRegistry.FODP);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("sxi"))
          .isEqualTo(DefaultDocumentFormatRegistry.SXI);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("ppt"))
          .isEqualTo(DefaultDocumentFormatRegistry.PPT);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("pptx"))
          .isEqualTo(DefaultDocumentFormatRegistry.PPTX);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("odg"))
          .isEqualTo(DefaultDocumentFormatRegistry.ODG);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("otg"))
          .isEqualTo(DefaultDocumentFormatRegistry.OTG);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("fodg"))
          .isEqualTo(DefaultDocumentFormatRegistry.FODG);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("svg"))
          .isEqualTo(DefaultDocumentFormatRegistry.SVG);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("png"))
          .isEqualTo(DefaultDocumentFormatRegistry.PNG);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("jpg"))
          .isEqualTo(DefaultDocumentFormatRegistry.JPEG);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("jpeg"))
          .isEqualTo(DefaultDocumentFormatRegistry.JPEG);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("tif"))
          .isEqualTo(DefaultDocumentFormatRegistry.TIFF);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("tiff"))
          .isEqualTo(DefaultDocumentFormatRegistry.TIFF);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("gif"))
          .isEqualTo(DefaultDocumentFormatRegistry.GIF);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("bmp"))
          .isEqualTo(DefaultDocumentFormatRegistry.BMP);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("vsd"))
          .isEqualTo(DefaultDocumentFormatRegistry.VSD);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("vsdx"))
          .isEqualTo(DefaultDocumentFormatRegistry.VSDX);
    }
  }

  @Test
  public void getFormatByMediaType_AllFormatsLoadedSuccessfully() {

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/pdf"))
          .isEqualTo(DefaultDocumentFormatRegistry.PDF);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType("application/x-shockwave-flash"))
          .isEqualTo(DefaultDocumentFormatRegistry.SWF);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("text/html"))
          .isEqualTo(DefaultDocumentFormatRegistry.HTML);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/xhtml+xml"))
          .isEqualTo(DefaultDocumentFormatRegistry.XHTML);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.text"))
          .isEqualTo(DefaultDocumentFormatRegistry.ODT);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.text-template"))
          .isEqualTo(DefaultDocumentFormatRegistry.OTT);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.text-flat-xml"))
          .isEqualTo(DefaultDocumentFormatRegistry.FODT);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd.sun.xml.writer"))
          .isEqualTo(DefaultDocumentFormatRegistry.SXW);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/msword"))
          .isEqualTo(DefaultDocumentFormatRegistry.DOC);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
          .isEqualTo(DefaultDocumentFormatRegistry.DOCX);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("text/rtf"))
          .isEqualTo(DefaultDocumentFormatRegistry.RTF);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/wordperfect"))
          .isEqualTo(DefaultDocumentFormatRegistry.WPD);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("text/plain"))
          .isEqualTo(DefaultDocumentFormatRegistry.TXT);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.spreadsheet"))
          .isEqualTo(DefaultDocumentFormatRegistry.ODS);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.spreadsheet-template"))
          .isEqualTo(DefaultDocumentFormatRegistry.OTS);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.spreadsheet-flat-xml"))
          .isEqualTo(DefaultDocumentFormatRegistry.FODS);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd.sun.xml.calc"))
          .isEqualTo(DefaultDocumentFormatRegistry.SXC);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd.ms-excel"))
          .isEqualTo(DefaultDocumentFormatRegistry.XLS);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
          .isEqualTo(DefaultDocumentFormatRegistry.XLSX);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("text/csv"))
          .isEqualTo(DefaultDocumentFormatRegistry.CSV);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType("text/tab-separated-values"))
          .isEqualTo(DefaultDocumentFormatRegistry.TSV);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.presentation"))
          .isEqualTo(DefaultDocumentFormatRegistry.ODP);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.presentation-template"))
          .isEqualTo(DefaultDocumentFormatRegistry.OTP);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.presentation-flat-xml"))
          .isEqualTo(DefaultDocumentFormatRegistry.FODP);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd.sun.xml.impress"))
          .isEqualTo(DefaultDocumentFormatRegistry.SXI);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd.ms-powerpoint"))
          .isEqualTo(DefaultDocumentFormatRegistry.PPT);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.openxmlformats-officedocument.presentationml.presentation"))
          .isEqualTo(DefaultDocumentFormatRegistry.PPTX);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.graphics"))
          .isEqualTo(DefaultDocumentFormatRegistry.ODG);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.graphics-template"))
          .isEqualTo(DefaultDocumentFormatRegistry.OTG);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd.oasis.opendocument.graphics-flat-xml"))
          .isEqualTo(DefaultDocumentFormatRegistry.FODG);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("image/svg+xml"))
          .isEqualTo(DefaultDocumentFormatRegistry.SVG);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("image/png"))
          .isEqualTo(DefaultDocumentFormatRegistry.PNG);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("image/jpeg"))
          .isEqualTo(DefaultDocumentFormatRegistry.JPEG);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("image/gif"))
          .isEqualTo(DefaultDocumentFormatRegistry.GIF);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("image/tiff"))
          .isEqualTo(DefaultDocumentFormatRegistry.TIFF);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("image/bmp"))
          .isEqualTo(DefaultDocumentFormatRegistry.BMP);
      softly
          .assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd-visio"))
          .isEqualTo(DefaultDocumentFormatRegistry.VSD);
      softly
          .assertThat(
              DefaultDocumentFormatRegistry.getFormatByMediaType(
                  "application/vnd-ms-visio.drawing"))
          .isEqualTo(DefaultDocumentFormatRegistry.VSDX);
    }
  }

  @Test
  public void getFormatX_ReturnReadOnlyFormat() {

    final DocumentFormat format = DefaultDocumentFormatRegistry.CSV;

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
