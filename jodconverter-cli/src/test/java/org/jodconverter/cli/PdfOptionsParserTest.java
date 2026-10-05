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

package org.jodconverter.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.entry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.pdf.PdfOptions;

/** Contains tests for the {@link PdfOptionsParser} class. */
class PdfOptionsParserTest {

  @Nested
  class Parse {

    @Test
    void withoutPresetNorOption_ShouldReturnNull() {
      assertThat(PdfOptionsParser.parse(null)).isNull();
      assertThat(PdfOptionsParser.parse(null, (String[]) null)).isNull();
    }

    @Test
    void withPreset_ShouldReturnThePreset() {
      assertThat(PdfOptionsParser.parse("archive").getFilterData())
          .isEqualTo(PdfOptions.archive().getFilterData());
      assertThat(PdfOptionsParser.parse("Accessible").getFilterData())
          .isEqualTo(PdfOptions.accessible().getFilterData());
      assertThat(PdfOptionsParser.parse("COMPACT").getFilterData())
          .isEqualTo(PdfOptions.compact().getFilterData());
    }

    @Test
    void withUnknownPreset_ShouldThrowIllegalArgumentException() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse("tiny"))
          .withMessage("Unknown PDF preset 'tiny'; expected archive, accessible or compact");
    }

    @Test
    void withPresetAndOptions_ShouldApplyTheOptionsOnTopOfThePreset() {

      final PdfOptions options =
          PdfOptionsParser.parse("archive", "pages.range=1-3", "version=pdf-a-3b");

      assertThat(options.getFilterData())
          .containsEntry("SelectPdfVersion", 3)
          .containsEntry("PageRange", "1-3")
          .containsEntry("UseTaggedPDF", true)
          .containsEntry("UseLosslessCompression", true);
    }

    @Test
    void withVersion_ShouldAcceptTheShortAndTheFullNames() {

      final Map<String, Integer> expected = new HashMap<>();
      expected.put("1.5", 15);
      expected.put("1.7", 17);
      expected.put("2.0", 20);
      expected.put("a-1b", 1);
      expected.put("A-2B", 2);
      expected.put("pdf-a-3b", 3);
      expected.put("PDF_A_4", 4);
      expected.put("pdf-1.6", 16);
      expected.put("default", 0);

      expected.forEach(
          (value, selectPdfVersion) ->
              assertThat(PdfOptionsParser.parse(null, "version=" + value).getFilterData())
                  .as(value)
                  .containsOnly(entry("SelectPdfVersion", selectPdfVersion)));
    }

    @Test
    void withTypedValues_ShouldConvertThem() {

      final PdfOptions options =
          PdfOptionsParser.parse(
              null,
              "tagged=TRUE",
              "images.jpeg-quality=80",
              "initial-view.magnification=fit-width",
              "security.permission-password=a=b",
              "security.printing=low_resolution",
              "watermark.text=Top secret",
              "watermark.color=#FF0000",
              "watermark.rotation=45");

      assertThat(options.getFilterData())
          .containsEntry("UseTaggedPDF", true)
          .containsEntry("Quality", 80)
          .containsEntry("Magnification", 2)
          .containsEntry("PermissionPassword", "a=b")
          .containsEntry("Printing", 1)
          .containsEntry("Watermark", "Top secret")
          .containsEntry("WatermarkColor", 0xFF0000)
          .containsEntry("WatermarkRotateAngle", 450);
      assertThat(PdfOptionsParser.parse(null, "watermark.color=0x00ff00").getFilterData())
          .containsEntry("WatermarkColor", 0x00FF00);
      assertThat(PdfOptionsParser.parse(null, "watermark.color=0000FF").getFilterData())
          .containsEntry("WatermarkColor", 0x0000FF);
    }

    @Test
    void withFilterData_ShouldSetAnyProperty() {

      final PdfOptions options =
          PdfOptionsParser.parse(
              null, "filter-data.Unknown=text", "filter-data.Count=3", "filter-data.Flag=true");

      assertThat(options.getFilterData())
          .containsOnly(entry("Unknown", "text"), entry("Count", 3), entry("Flag", true));
    }

    @Test
    void withEveryOptionName_ShouldBuildOptions(final @TempDir File testFolder) throws IOException {

      final File pem = new File(testFolder, "file.pem");
      Files.writeString(pem.toPath(), "PEM");

      // A valid value for each option: by name for the ones that are not booleans.
      final Map<String, String> values = new HashMap<>();
      values.put("version", "1.7");
      values.put("images.jpeg-quality", "90");
      values.put("images.max-resolution", "300");
      values.put("pages.range", "1");
      values.put("bookmarks.open-levels", "2");
      values.put("forms.submit-format", "xml");
      values.put("links.cross-document-links", "browser");
      values.put("initial-view.pane", "thumbnails");
      values.put("initial-view.page", "2");
      values.put("initial-view.magnification", "fit-page");
      values.put("initial-view.zoom", "75");
      values.put("initial-view.layout", "continuous-facing");
      values.put("security.open-password", "open");
      values.put("security.permission-password", "owner");
      values.put("security.printing", "none");
      values.put("security.changes", "forms-and-comments");
      values.put("watermark.text", "a");
      values.put("watermark.tiled-text", "b");
      values.put("watermark.color", "FF0000");
      values.put("watermark.font-name", "c");
      values.put("watermark.font-height", "12");
      values.put("watermark.rotation", "45");
      values.put("signature.certificate-subject-name", "CN=Me");
      values.put("signature.certificate-file", pem.getPath());
      values.put("signature.private-key-file", pem.getPath());
      values.put("signature.ca-file", pem.getPath());
      values.put("signature.password", "d");
      values.put("signature.location", "e");
      values.put("signature.reason", "f");
      values.put("signature.contact-info", "g");
      values.put("signature.timestamp-authority", "h");
      values.put("spreadsheet.sheet-range", "1-2");

      final List<String> arguments = new ArrayList<>();
      for (final String name : PdfOptionsParser.getOptionNames()) {
        arguments.add(name + "=" + values.getOrDefault(name, "true"));
      }

      final PdfOptions options = PdfOptionsParser.parse(null, arguments.toArray(new String[0]));

      // The 66 properties of PdfOptions: every one of them can be set from the command line.
      assertThat(options.getFilterData()).hasSize(66);
      assertThat(options.getFilterData())
          .containsEntry("SignCertificateCertPem", "PEM")
          .containsEntry("SignCertificateKeyPem", "PEM")
          .containsEntry("SignCertificateCaPem", "PEM")
          .containsEntry("SignCertificateSubjectName", "CN=Me");
    }

    @Test
    void withInvalidOptions_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse(null, "tagged"))
          .withMessage("Invalid PDF option 'tagged'; expected name=value");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse(null, "=true"))
          .withMessageContaining("expected name=value");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse(null, "pages.rang=1"))
          .withMessageStartingWith("Unknown PDF option 'pages.rang'; expected one of: version, ")
          .withMessageContaining("pages.range");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse(null, "tagged=yes"))
          .withMessage("Invalid value 'yes' for the PDF option 'tagged': expected true or false");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse(null, "images.jpeg-quality=high"))
          .withMessage(
              "Invalid value 'high' for the PDF option 'images.jpeg-quality': expected a number");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse(null, "images.jpeg-quality=0"))
          .withMessageContaining("jpegQuality must be between 1 and 100");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse(null, "security.printing=draft"))
          .withMessage(
              "Invalid value 'draft' for the PDF option 'security.printing':"
                  + " expected one of: none, low-resolution, high-resolution");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse(null, "version=3.0"))
          .withMessageContaining("expected one of: default, pdf-1-5");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse(null, "watermark.color=red"))
          .withMessageContaining("expected a color such as FF0000");
    }

    @Test
    void withOptionsThatCannotBeUsedTogether_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse("archive", "security.open-password=secret"))
          .withMessageContaining("PDF/A does not allow encryption");
    }

    @Test
    void withCertificateFileWithoutPrivateKeyFile_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptionsParser.parse(null, "signature.certificate-file=cert.pem"))
          .withMessage(
              "The PDF options 'signature.certificate-file' and 'signature.private-key-file'"
                  + " must be used together");
    }

    @Test
    void withCertificateFileThatDoesNotExist_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  PdfOptionsParser.parse(
                      null,
                      "signature.certificate-file=does-not-exist.pem",
                      "signature.private-key-file=does-not-exist.pem"))
          .withMessage("Could not read the file 'does-not-exist.pem'");
      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  PdfOptionsParser.parse(
                      null,
                      "signature.certificate-subject-name=CN=Me",
                      "signature.ca-file=does-not-exist.pem"))
          .withMessageContaining("Could not read the file 'does-not-exist.pem'");
    }
  }
}
