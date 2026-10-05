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

package org.jodconverter.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.core.pdf.PdfVersion;

/** Contains tests for conversions using {@link PdfOptions}, with a real office installation. */
@ExtendWith(LocalOfficeManagerExtension.class)
class PdfOptionsITest {

  private static final File SOURCE_FILE = documentFile("/test.doc");
  private static final File MULTI_PAGE_FILE = documentFile("/test_multi_page.doc");

  // Reads a PDF file as text. The parts checked by these tests (header, trailer, document
  // catalog, XMP metadata) are not compressed.
  private static String readPdf(final File file) throws IOException {
    return Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
  }

  private static int countPages(final String pdf) {
    return pdf.split("/Type\\s*/Page\\b(?!s)", -1).length - 1;
  }

  @Test
  void withVersion_ShouldProduceThatPdfVersion(
      final @TempDir File testFolder, final DocumentConverter converter)
      throws IOException, OfficeException {

    final File outputFile = new File(testFolder, "out.pdf");

    converter
        .convert(SOURCE_FILE)
        .to(outputFile)
        .with(PdfOptions.builder().version(PdfVersion.PDF_1_5).build())
        .execute();

    assertThat(readPdf(outputFile)).startsWith("%PDF-1.5");
  }

  @Test
  void withArchivePreset_ShouldProducePdfA2(
      final @TempDir File testFolder, final DocumentConverter converter)
      throws IOException, OfficeException {

    final File outputFile = new File(testFolder, "out.pdf");

    converter.convert(SOURCE_FILE).to(outputFile).with(PdfOptions.archive()).execute();

    final String pdf = readPdf(outputFile);
    assertThat(pdf).containsPattern("<pdfaid:part>\\s*2\\s*</pdfaid:part>");
    assertThat(pdf).contains("/StructTreeRoot");
  }

  @Test
  void withTagged_ShouldControlTheDocumentStructure(
      final @TempDir File testFolder, final DocumentConverter converter)
      throws IOException, OfficeException {

    final File taggedFile = new File(testFolder, "tagged.pdf");
    final File untaggedFile = new File(testFolder, "untagged.pdf");

    converter
        .convert(SOURCE_FILE)
        .to(taggedFile)
        .with(PdfOptions.builder().tagged(true).build())
        .execute();
    converter
        .convert(SOURCE_FILE)
        .to(untaggedFile)
        .with(PdfOptions.builder().tagged(false).build())
        .execute();

    assertThat(readPdf(taggedFile)).contains("/StructTreeRoot");
    assertThat(readPdf(untaggedFile)).doesNotContain("/StructTreeRoot");
  }

  @Test
  void withOpenPassword_ShouldEncryptTheDocument(
      final @TempDir File testFolder, final DocumentConverter converter)
      throws IOException, OfficeException {

    final File outputFile = new File(testFolder, "out.pdf");

    converter
        .convert(SOURCE_FILE)
        .to(outputFile)
        .with(PdfOptions.builder().security(security -> security.openPassword("secret")).build())
        .execute();

    assertThat(readPdf(outputFile)).contains("/Encrypt");
  }

  @Test
  void withPageRange_ShouldExportOnlyThosePages(
      final @TempDir File testFolder, final DocumentConverter converter)
      throws IOException, OfficeException {

    final File allPagesFile = new File(testFolder, "all.pdf");
    final File firstPageFile = new File(testFolder, "first.pdf");

    converter.convert(MULTI_PAGE_FILE).to(allPagesFile).execute();
    converter
        .convert(MULTI_PAGE_FILE)
        .to(firstPageFile)
        .with(PdfOptions.builder().pages(pages -> pages.range("1")).build())
        .execute();

    assertThat(countPages(readPdf(allPagesFile))).isGreaterThan(1);
    assertThat(countPages(readPdf(firstPageFile))).isEqualTo(1);
  }

  @Test
  void withConverterFilterData_ShouldMergeItAndTakePrecedence(
      final @TempDir File testFolder, final OfficeManager manager)
      throws IOException, OfficeException {

    // The converter asks for PDF 1.6 and only the first page, for all its conversions.
    final Map<String, Object> filterData = new HashMap<>();
    filterData.put("SelectPdfVersion", 16);
    filterData.put("PageRange", "1");
    final DocumentConverter converter =
        LocalConverter.builder()
            .officeManager(manager)
            .storeProperty("FilterData", filterData)
            .build();
    final File converterOnlyFile = new File(testFolder, "converter.pdf");
    final File withOptionsFile = new File(testFolder, "options.pdf");

    converter.convert(MULTI_PAGE_FILE).to(converterOnlyFile).execute();
    converter
        .convert(MULTI_PAGE_FILE)
        .to(withOptionsFile)
        .with(PdfOptions.builder().version(PdfVersion.PDF_1_5).build())
        .execute();

    final String converterOnly = readPdf(converterOnlyFile);
    assertThat(converterOnly).startsWith("%PDF-1.6");
    assertThat(countPages(converterOnly)).isEqualTo(1);

    // The version of the options wins, and the page range of the converter is kept.
    final String withOptions = readPdf(withOptionsFile);
    assertThat(withOptions).startsWith("%PDF-1.5");
    assertThat(countPages(withOptions)).isEqualTo(1);
  }

  @Test
  void toOutputStream_ShouldApplyTheOptions(final DocumentConverter converter)
      throws OfficeException {

    final ByteArrayOutputStream output = new ByteArrayOutputStream();

    converter
        .convert(SOURCE_FILE)
        .to(output)
        .as(DefaultDocumentFormatRegistry.PDF)
        .with(PdfOptions.builder().version(PdfVersion.PDF_1_5).build())
        .execute();

    assertThat(output.toString(StandardCharsets.ISO_8859_1)).startsWith("%PDF-1.5");
  }

  @Test
  void withTargetThatIsNotPdf_ShouldThrowIllegalArgumentException(
      final @TempDir File testFolder, final DocumentConverter converter) {

    final File outputFile = new File(testFolder, "out.odt");

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                converter.convert(SOURCE_FILE).to(outputFile).with(PdfOptions.archive()).execute())
        .withMessage("PdfOptions cannot be applied to a target document of format 'odt'");
  }
}
