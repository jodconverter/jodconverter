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

package org.jodconverter.boot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.jodconverter.boot.autoconfigure.JodConverterPdfAutoConfiguration;
import org.jodconverter.core.pdf.PdfOptions;

/** Contains tests for the {@link JodConverterPdfAutoConfiguration} class. No office is needed. */
class PdfOptionsAutoConfigurationITest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(JodConverterPdfAutoConfiguration.class));

  @Test
  void withoutPdfProperty_ShouldNotCreatePdfOptions() {
    contextRunner.run(context -> assertThat(context).doesNotHaveBean(PdfOptions.class));
  }

  @Test
  void withPreset_ShouldCreateThePresetOptions() {
    contextRunner
        .withPropertyValues("jodconverter.pdf.preset=archive")
        .run(
            context ->
                assertThat(context.getBean(PdfOptions.class).getFilterData())
                    .isEqualTo(PdfOptions.archive().getFilterData()));
  }

  @Test
  void withPresetAndProperties_ShouldApplyThePropertiesOnTopOfThePreset() {
    contextRunner
        .withPropertyValues(
            "jodconverter.pdf.preset=compact",
            "jodconverter.pdf.version=pdf-1-7",
            "jodconverter.pdf.images.jpeg-quality=60",
            "jodconverter.pdf.pages.range=1-3")
        .run(
            context ->
                assertThat(context.getBean(PdfOptions.class).getFilterData())
                    .containsEntry("UseTaggedPDF", true)
                    .containsEntry("MaxImageResolution", 150)
                    .containsEntry("SelectPdfVersion", 17)
                    .containsEntry("Quality", 60)
                    .containsEntry("PageRange", "1-3"));
  }

  @Test
  void withPropertiesOfEveryGroup_ShouldCreateTheExpectedOptions() {
    contextRunner
        .withPropertyValues(
            "jodconverter.pdf.version=PDF_A_2B",
            "jodconverter.pdf.pdf-ua=true",
            "jodconverter.pdf.tagged=true",
            "jodconverter.pdf.embed-source-document=true",
            "jodconverter.pdf.reference-xobjects=true",
            "jodconverter.pdf.images.lossless=true",
            "jodconverter.pdf.images.max-resolution=300",
            "jodconverter.pdf.pages.skip-empty-pages=true",
            "jodconverter.pdf.comments.in-margin=true",
            "jodconverter.pdf.bookmarks.open-levels=2",
            "jodconverter.pdf.forms.submit-format=xml",
            "jodconverter.pdf.links.cross-document-links=pdf-reader",
            "jodconverter.pdf.initial-view.magnification=fit-width",
            "jodconverter.pdf.initial-view.layout=continuous-facing",
            "jodconverter.pdf.viewer.hide-toolbar=true",
            "jodconverter.pdf.watermark.text=DRAFT",
            "jodconverter.pdf.watermark.color=#FF0000",
            "jodconverter.pdf.watermark.rotation=45",
            "jodconverter.pdf.signature.certificate-subject-name=CN=Me",
            "jodconverter.pdf.signature.reason=Approval",
            "jodconverter.pdf.presentation.hidden-slides=true",
            "jodconverter.pdf.spreadsheet.sheet-range=1-2",
            "jodconverter.pdf.filter-data.Custom=text",
            "jodconverter.pdf.filter-data.Count=3",
            "jodconverter.pdf.filter-data.Flag=true")
        .run(
            context ->
                assertThat(context.getBean(PdfOptions.class).getFilterData())
                    .containsOnly(
                        entry("SelectPdfVersion", 2),
                        entry("PDFUACompliance", true),
                        entry("UseTaggedPDF", true),
                        entry("IsAddStream", true),
                        entry("UseReferenceXObject", true),
                        entry("UseLosslessCompression", true),
                        entry("ReduceImageResolution", true),
                        entry("MaxImageResolution", 300),
                        entry("IsSkipEmptyPages", true),
                        entry("ExportNotesInMargin", true),
                        entry("OpenBookmarkLevels", 2),
                        entry("FormsType", 3),
                        entry("PDFViewSelection", 1),
                        entry("Magnification", 2),
                        entry("PageLayout", 3),
                        entry("HideViewerToolbar", true),
                        entry("Watermark", "DRAFT"),
                        entry("WatermarkColor", 0xFF0000),
                        entry("WatermarkRotateAngle", 450),
                        entry("SignPDF", true),
                        entry("SignCertificateSubjectName", "CN=Me"),
                        entry("SignatureReason", "Approval"),
                        entry("ExportHiddenSlides", true),
                        entry("SheetRange", "1-2"),
                        entry("Custom", "text"),
                        entry("Count", 3),
                        entry("Flag", true)));
  }

  @Test
  void withSecurityProperties_ShouldCreateTheExpectedOptions() {
    contextRunner
        .withPropertyValues(
            "jodconverter.pdf.security.open-password=open",
            "jodconverter.pdf.security.permission-password=owner",
            "jodconverter.pdf.security.printing=low-resolution",
            "jodconverter.pdf.security.changes=forms-and-comments",
            "jodconverter.pdf.security.copying=false")
        .run(
            context ->
                assertThat(context.getBean(PdfOptions.class).getFilterData())
                    .containsOnly(
                        entry("EncryptFile", true),
                        entry("DocumentOpenPassword", "open"),
                        entry("RestrictPermissions", true),
                        entry("PermissionPassword", "owner"),
                        entry("Printing", 1),
                        entry("Changes", 3),
                        entry("EnableCopyingOfContent", false)));
  }

  @Test
  void withCertificateResources_ShouldReadThem() {
    contextRunner
        .withPropertyValues(
            "jodconverter.pdf.signature.certificate=classpath:pdf/test-certificate.pem",
            "jodconverter.pdf.signature.private-key=classpath:pdf/test-key.pem",
            "jodconverter.pdf.signature.certificate-authorities=classpath:pdf/test-certificate.pem")
        .run(
            context -> {
              final PdfOptions options = context.getBean(PdfOptions.class);
              assertThat(options.getFilterData()).containsEntry("SignPDF", true);
              assertThat((String) options.getFilterData().get("SignCertificateCertPem"))
                  .contains("TESTCERT");
              assertThat((String) options.getFilterData().get("SignCertificateKeyPem"))
                  .contains("TESTKEY");
              assertThat((String) options.getFilterData().get("SignCertificateCaPem"))
                  .contains("TESTCERT");
            });
  }

  @Test
  void withCertificateWithoutPrivateKey_ShouldFailToStart() {
    contextRunner
        .withPropertyValues(
            "jodconverter.pdf.signature.certificate=classpath:pdf/test-certificate.pem")
        .run(
            context ->
                assertThat(context)
                    .hasFailed()
                    .getFailure()
                    .rootCause()
                    .hasMessageContaining("must be used together"));
  }

  @Test
  void withPropertiesThatCannotBeUsedTogether_ShouldFailToStart() {
    contextRunner
        .withPropertyValues(
            "jodconverter.pdf.preset=archive", "jodconverter.pdf.security.open-password=secret")
        .run(
            context ->
                assertThat(context)
                    .hasFailed()
                    .getFailure()
                    .rootCause()
                    .hasMessageContaining("PDF/A does not allow encryption"));
  }

  @Test
  void withInvalidValue_ShouldFailToStart() {
    contextRunner
        .withPropertyValues("jodconverter.pdf.security.printing=draft")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void withApplicationPdfOptions_ShouldNotCreateAnotherOne() {
    contextRunner
        .withUserConfiguration(ApplicationPdfOptions.class)
        .withPropertyValues("jodconverter.pdf.preset=archive")
        .run(
            context ->
                assertThat(context.getBean(PdfOptions.class).getFilterData())
                    .isEqualTo(PdfOptions.compact().getFilterData()));
  }

  @Configuration(proxyBeanMethods = false)
  /* default */ static class ApplicationPdfOptions {

    @Bean
    /* default */ PdfOptions applicationPdfOptions() {
      return PdfOptions.compact();
    }
  }
}
