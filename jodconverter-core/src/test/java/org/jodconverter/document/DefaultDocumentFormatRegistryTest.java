/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Set;

import org.junit.Test;

import org.jodconverter.test.util.AssertUtil;

public class DefaultDocumentFormatRegistryTest {

  @Test
  public void ctor_ClassWellDefined() throws Exception {

    AssertUtil.assertUtilityClassWellDefined(DefaultDocumentFormatRegistry.class);
  }

  private void assertExpectedExtensions(
      final Set<DocumentFormat> formats, final String... extensions) {

    assertThat(formats).hasSize(extensions.length);
    for (final DocumentFormat format : formats) {
      assertThat(format.getExtension()).isIn((Object[]) extensions);
    }
    //formats
    //.stream()
    //.forEach(
    //fmt ->
    //assertThat(fmt.getExtension())
    //.isIn(extensions));
  }

  /** Tests all the default output formats are load successfully. */
  @Test
  public void getInstance_AllOutputFormatsLoadedSuccessfully() {

    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();

    // TEXT output format
    Set<DocumentFormat> outputFormats = registry.getOutputFormats(DocumentFamily.TEXT);
    assertExpectedExtensions(
        outputFormats, "doc", "docx", "html", "odt", "ott", "pdf", "png", "rtf", "sxw", "txt");
    // SPREADSHEET output format
    outputFormats = registry.getOutputFormats(DocumentFamily.SPREADSHEET);
    assertExpectedExtensions(
        outputFormats, "csv", "html", "ods", "ots", "pdf", "png", "sxc", "tsv", "xls", "xlsx");
    // PRESENTATION output format
    outputFormats = registry.getOutputFormats(DocumentFamily.PRESENTATION);
    assertExpectedExtensions(
        outputFormats, "html", "odp", "otp", "pdf", "png", "ppt", "pptx", "swf", "sxi");
    // DRAWING output format
    outputFormats = registry.getOutputFormats(DocumentFamily.DRAWING);
    assertExpectedExtensions(outputFormats, "odg", "otg", "pdf", "png", "svg", "swf");
  }

  @Test
  public void getFormatByExtension_AllFormatsLoadedSuccessfully() {

    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("pdf"))
        .isEqualTo(DefaultDocumentFormatRegistry.PDF);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("swf"))
        .isEqualTo(DefaultDocumentFormatRegistry.SWF);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("html"))
        .isEqualTo(DefaultDocumentFormatRegistry.HTML);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("odt"))
        .isEqualTo(DefaultDocumentFormatRegistry.ODT);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("ott"))
        .isEqualTo(DefaultDocumentFormatRegistry.OTT);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("sxw"))
        .isEqualTo(DefaultDocumentFormatRegistry.SXW);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("doc"))
        .isEqualTo(DefaultDocumentFormatRegistry.DOC);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("docx"))
        .isEqualTo(DefaultDocumentFormatRegistry.DOCX);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("rtf"))
        .isEqualTo(DefaultDocumentFormatRegistry.RTF);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("wpd"))
        .isEqualTo(DefaultDocumentFormatRegistry.WPD);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("txt"))
        .isEqualTo(DefaultDocumentFormatRegistry.TXT);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("ods"))
        .isEqualTo(DefaultDocumentFormatRegistry.ODS);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("ots"))
        .isEqualTo(DefaultDocumentFormatRegistry.OTS);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("sxc"))
        .isEqualTo(DefaultDocumentFormatRegistry.SXC);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("xls"))
        .isEqualTo(DefaultDocumentFormatRegistry.XLS);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("xlsx"))
        .isEqualTo(DefaultDocumentFormatRegistry.XLSX);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("csv"))
        .isEqualTo(DefaultDocumentFormatRegistry.CSV);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("tsv"))
        .isEqualTo(DefaultDocumentFormatRegistry.TSV);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("odp"))
        .isEqualTo(DefaultDocumentFormatRegistry.ODP);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("otp"))
        .isEqualTo(DefaultDocumentFormatRegistry.OTP);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("sxi"))
        .isEqualTo(DefaultDocumentFormatRegistry.SXI);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("ppt"))
        .isEqualTo(DefaultDocumentFormatRegistry.PPT);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("pptx"))
        .isEqualTo(DefaultDocumentFormatRegistry.PPTX);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("odg"))
        .isEqualTo(DefaultDocumentFormatRegistry.ODG);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("otg"))
        .isEqualTo(DefaultDocumentFormatRegistry.OTG);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("svg"))
        .isEqualTo(DefaultDocumentFormatRegistry.SVG);
    assertThat(DefaultDocumentFormatRegistry.getFormatByExtension("png"))
        .isEqualTo(DefaultDocumentFormatRegistry.PNG);
  }

  @Test
  public void getFormatByMediaType_AllFormatsLoadedSuccessfully() {

    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/pdf"))
        .isEqualTo(DefaultDocumentFormatRegistry.PDF);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/x-shockwave-flash"))
        .isEqualTo(DefaultDocumentFormatRegistry.SWF);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("text/html"))
        .isEqualTo(DefaultDocumentFormatRegistry.HTML);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType(
                "application/vnd.oasis.opendocument.text"))
        .isEqualTo(DefaultDocumentFormatRegistry.ODT);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType(
                "application/vnd.oasis.opendocument.text-template"))
        .isEqualTo(DefaultDocumentFormatRegistry.OTT);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd.sun.xml.writer"))
        .isEqualTo(DefaultDocumentFormatRegistry.SXW);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/msword"))
        .isEqualTo(DefaultDocumentFormatRegistry.DOC);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .isEqualTo(DefaultDocumentFormatRegistry.DOCX);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("text/rtf"))
        .isEqualTo(DefaultDocumentFormatRegistry.RTF);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/wordperfect"))
        .isEqualTo(DefaultDocumentFormatRegistry.WPD);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("text/plain"))
        .isEqualTo(DefaultDocumentFormatRegistry.TXT);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType(
                "application/vnd.oasis.opendocument.spreadsheet"))
        .isEqualTo(DefaultDocumentFormatRegistry.ODS);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType(
                "application/vnd.oasis.opendocument.spreadsheet-template"))
        .isEqualTo(DefaultDocumentFormatRegistry.OTS);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd.sun.xml.calc"))
        .isEqualTo(DefaultDocumentFormatRegistry.SXC);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd.ms-excel"))
        .isEqualTo(DefaultDocumentFormatRegistry.XLS);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .isEqualTo(DefaultDocumentFormatRegistry.XLSX);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("text/csv"))
        .isEqualTo(DefaultDocumentFormatRegistry.CSV);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("text/tab-separated-values"))
        .isEqualTo(DefaultDocumentFormatRegistry.TSV);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType(
                "application/vnd.oasis.opendocument.presentation"))
        .isEqualTo(DefaultDocumentFormatRegistry.ODP);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType(
                "application/vnd.oasis.opendocument.presentation-template"))
        .isEqualTo(DefaultDocumentFormatRegistry.OTP);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd.sun.xml.impress"))
        .isEqualTo(DefaultDocumentFormatRegistry.SXI);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd.ms-powerpoint"))
        .isEqualTo(DefaultDocumentFormatRegistry.PPT);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"))
        .isEqualTo(DefaultDocumentFormatRegistry.PPTX);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType(
                "application/vnd.oasis.opendocument.graphics"))
        .isEqualTo(DefaultDocumentFormatRegistry.ODG);
    assertThat(
            DefaultDocumentFormatRegistry.getFormatByMediaType(
                "application/vnd.oasis.opendocument.graphics-template"))
        .isEqualTo(DefaultDocumentFormatRegistry.OTG);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("image/svg+xml"))
        .isEqualTo(DefaultDocumentFormatRegistry.SVG);
    assertThat(DefaultDocumentFormatRegistry.getFormatByMediaType("image/png"))
        .isEqualTo(DefaultDocumentFormatRegistry.PNG);
  }

  @Test
  public void getFormatX_ReturnReadOnlyFormat() {

    final DocumentFormat format = DefaultDocumentFormatRegistry.CSV;

    try {
      format.getLoadProperties().put("newKey", "newValue");
    } catch (Exception ex) {
      assertThat(ex).isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    try {
      format.getStoreProperties().put(DocumentFamily.DRAWING, new HashMap<String, Object>());
    } catch (Exception ex) {
      assertThat(ex).isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    try {
      format.getStoreProperties(DocumentFamily.SPREADSHEET).put("newKey", "newValue");
    } catch (Exception ex) {
      assertThat(ex).isExactlyInstanceOf(UnsupportedOperationException.class);
    }
  }
}
