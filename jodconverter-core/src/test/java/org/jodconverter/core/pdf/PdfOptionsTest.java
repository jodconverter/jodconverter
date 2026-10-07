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

package org.jodconverter.core.pdf;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.pdf.PdfFormOptions.SubmitFormat;
import org.jodconverter.core.pdf.PdfInitialViewOptions.Magnification;
import org.jodconverter.core.pdf.PdfInitialViewOptions.PageLayout;
import org.jodconverter.core.pdf.PdfInitialViewOptions.Pane;
import org.jodconverter.core.pdf.PdfLinkOptions.LinkTarget;
import org.jodconverter.core.pdf.PdfSecurityOptions.Changes;
import org.jodconverter.core.pdf.PdfSecurityOptions.Printing;

/** Contains tests for the {@link PdfOptions} class. */
class PdfOptionsTest {

  @Nested
  class Build {

    @Test
    void withoutOptions_ShouldHaveNoFilterData() {
      assertThat(PdfOptions.builder().build().getFilterData()).isEmpty();
    }

    @Test
    void withGeneralOptions_ShouldSetExpectedFilterData() {

      final var options =
          PdfOptions.builder()
              .version(PdfVersion.PDF_A_3B)
              .pdfUa(true)
              .tagged(true)
              .embedSourceDocument(true)
              .referenceXObjects(true)
              .build();

      assertThat(options.getFilterData())
          .containsOnly(
              entry("SelectPdfVersion", 3),
              entry("PDFUACompliance", true),
              entry("UseTaggedPDF", true),
              entry("IsAddStream", true),
              entry("UseReferenceXObject", true));
    }

    @Test
    void withEveryVersion_ShouldSetTheSelectPdfVersionValue() {

      final var expected = new HashMap<PdfVersion, Integer>();
      expected.put(PdfVersion.DEFAULT, 0);
      expected.put(PdfVersion.PDF_1_5, 15);
      expected.put(PdfVersion.PDF_1_6, 16);
      expected.put(PdfVersion.PDF_1_7, 17);
      expected.put(PdfVersion.PDF_2_0, 20);
      expected.put(PdfVersion.PDF_A_1B, 1);
      expected.put(PdfVersion.PDF_A_2B, 2);
      expected.put(PdfVersion.PDF_A_3B, 3);
      expected.put(PdfVersion.PDF_A_4, 4);

      assertThat(expected).containsOnlyKeys(PdfVersion.values());
      expected.forEach(
          (version, value) ->
              assertThat(PdfOptions.builder().version(version).build().getFilterData())
                  .containsOnly(entry("SelectPdfVersion", value)));
    }

    @Test
    void withImageOptions_ShouldSetExpectedFilterData() {

      final var options =
          PdfOptions.builder()
              .images(images -> images.lossless(false).jpegQuality(80).maxResolution(150))
              .build();

      assertThat(options.getFilterData())
          .containsOnly(
              entry("UseLosslessCompression", false),
              entry("Quality", 80),
              entry("ReduceImageResolution", true),
              entry("MaxImageResolution", 150));
      assertThat(
              PdfOptions.builder()
                  .images(images -> images.reduceResolution(false))
                  .build()
                  .getFilterData())
          .containsOnly(entry("ReduceImageResolution", false));
    }

    @Test
    void withPageAndCommentOptions_ShouldSetExpectedFilterData() {

      final var options =
          PdfOptions.builder()
              .pages(
                  pages ->
                      pages
                          .range("1-3;7")
                          .skipEmptyPages(true)
                          .placeholders(true)
                          .trackedChanges(false))
              .comments(comments -> comments.asPdfAnnotations(false).inMargin(true))
              .build();

      assertThat(options.getFilterData())
          .containsOnly(
              entry("PageRange", "1-3;7"),
              entry("IsSkipEmptyPages", true),
              entry("ExportPlaceholders", true),
              entry("ExportTrackedChanges", false),
              entry("ExportNotes", false),
              entry("ExportNotesInMargin", true));
    }

    @Test
    void withBookmarkFormAndLinkOptions_ShouldSetExpectedFilterData() {

      final var options =
          PdfOptions.builder()
              .bookmarks(
                  bookmarks -> bookmarks.export(true).openLevels(2).asNamedDestinations(true))
              .forms(
                  forms ->
                      forms.export(true).submitFormat(SubmitFormat.XML).allowDuplicateNames(true))
              .links(
                  links ->
                      links
                          .relativeFileLinks(true)
                          .convertOdfTargetsToPdf(true)
                          .crossDocumentLinks(LinkTarget.BROWSER))
              .build();

      assertThat(options.getFilterData())
          .containsOnly(
              entry("ExportBookmarks", true),
              entry("OpenBookmarkLevels", 2),
              entry("ExportBookmarksToPDFDestination", true),
              entry("ExportFormFields", true),
              entry("FormsType", 3),
              entry("AllowDuplicateFieldNames", true),
              entry("ExportLinksRelativeFsys", true),
              entry("ConvertOOoTargetToPDFTarget", true),
              entry("PDFViewSelection", 2));
    }

    @Test
    void withInitialViewAndViewerOptions_ShouldSetExpectedFilterData() {

      final var options =
          PdfOptions.builder()
              .initialView(
                  view ->
                      view.pane(Pane.THUMBNAILS)
                          .page(3)
                          .magnification(Magnification.FIT_WIDTH)
                          .layout(PageLayout.CONTINUOUS_FACING))
              .viewer(
                  viewer ->
                      viewer
                          .resizeToInitialPage(true)
                          .centerWindow(true)
                          .fullScreen(true)
                          .displayDocumentTitle(false)
                          .hideMenubar(true)
                          .hideToolbar(true)
                          .hideWindowControls(true))
              .build();

      assertThat(options.getFilterData())
          .containsOnly(
              entry("InitialView", 2),
              entry("InitialPage", 3),
              entry("Magnification", 2),
              entry("PageLayout", 3),
              entry("ResizeWindowToInitialPage", true),
              entry("CenterWindow", true),
              entry("OpenInFullScreenMode", true),
              entry("DisplayPDFDocumentTitle", false),
              entry("HideViewerMenubar", true),
              entry("HideViewerToolbar", true),
              entry("HideViewerWindowControls", true));
    }

    @Test
    void withZoom_ShouldAlsoSetTheMagnificationToZoom() {
      assertThat(PdfOptions.builder().initialView(view -> view.zoom(75)).build().getFilterData())
          .containsOnly(entry("Zoom", 75), entry("Magnification", 4));
    }

    @Test
    void withSecurityOptions_ShouldSetExpectedFilterData() {

      final var options =
          PdfOptions.builder()
              .security(
                  security ->
                      security
                          .openPassword("open")
                          .permissionPassword("owner")
                          .printing(Printing.LOW_RESOLUTION)
                          .changes(Changes.FORMS)
                          .copying(false)
                          .accessibilityAccess(true))
              .build();

      assertThat(options.getFilterData())
          .containsOnly(
              entry("EncryptFile", true),
              entry("DocumentOpenPassword", "open"),
              entry("RestrictPermissions", true),
              entry("PermissionPassword", "owner"),
              entry("Printing", 1),
              entry("Changes", 2),
              entry("EnableCopyingOfContent", false),
              entry("EnableTextAccessForAccessibilityTools", true));
    }

    @Test
    void withNullPassword_ShouldRemoveTheEncryption() {

      final var options =
          PdfOptions.builder()
              .security(security -> security.openPassword("open").permissionPassword("owner"))
              .security(security -> security.openPassword(null).permissionPassword(null))
              .build();

      assertThat(options.getFilterData()).isEmpty();
    }

    @Test
    void withWatermarkOptions_ShouldSetExpectedFilterData() {

      final var options =
          PdfOptions.builder()
              .watermark(
                  watermark ->
                      watermark
                          .text("DRAFT")
                          .tiledText("COPY")
                          .color(0xCC0000)
                          .fontName("Liberation Sans")
                          .fontHeight(48)
                          .rotation(45))
              .build();

      assertThat(options.getFilterData())
          .containsOnly(
              entry("Watermark", "DRAFT"),
              entry("TiledWatermark", "COPY"),
              entry("WatermarkColor", 0xCC0000),
              entry("WatermarkFontName", "Liberation Sans"),
              entry("WatermarkFontHeight", 48),
              entry("WatermarkRotateAngle", 450));
    }

    @Test
    void withSignatureOptions_ShouldSetExpectedFilterData() {

      final var bySubjectName =
          PdfOptions.builder()
              .signature(
                  signature ->
                      signature
                          .certificateSubjectName("CN=JODConverter")
                          .password("pwd")
                          .location("Montreal")
                          .reason("Approval")
                          .contactInfo("me@example.org")
                          .timestampAuthority("https://tsa.example.org"))
              .build();
      final var byPem =
          PdfOptions.builder()
              .signature(signature -> signature.certificatePem("CERT", "KEY").caPem("CA"))
              .build();

      assertThat(bySubjectName.getFilterData())
          .containsOnly(
              entry("SignPDF", true),
              entry("SignCertificateSubjectName", "CN=JODConverter"),
              entry("SignaturePassword", "pwd"),
              entry("SignatureLocation", "Montreal"),
              entry("SignatureReason", "Approval"),
              entry("SignatureContactInfo", "me@example.org"),
              entry("SignatureTSA", "https://tsa.example.org"));
      assertThat(byPem.getFilterData())
          .containsOnly(
              entry("SignPDF", true),
              entry("SignCertificateCertPem", "CERT"),
              entry("SignCertificateKeyPem", "KEY"),
              entry("SignCertificateCaPem", "CA"));
    }

    @Test
    void withPresentationAndSpreadsheetOptions_ShouldSetExpectedFilterData() {

      final var options =
          PdfOptions.builder()
              .presentation(
                  presentation ->
                      presentation
                          .hiddenSlides(true)
                          .notesPages(true)
                          .onlyNotesPages(false)
                          .transitions(false))
              .spreadsheet(spreadsheet -> spreadsheet.singlePageSheets(true).sheetRange("1-2"))
              .build();

      assertThat(options.getFilterData())
          .containsOnly(
              entry("ExportHiddenSlides", true),
              entry("ExportNotesPages", true),
              entry("ExportOnlyNotesPages", false),
              entry("UseTransitionEffects", false),
              entry("SinglePageSheets", true),
              entry("SheetRange", "1-2"));
    }

    @Test
    void withFilterData_ShouldTakePrecedenceOverTheTypedOptions() {

      final var options =
          PdfOptions.builder()
              .filterData("UseTaggedPDF", false)
              .filterData("Unknown", "value")
              .filterData("Removed", 1)
              .filterData("Removed", null)
              .tagged(true)
              .build();

      assertThat(options.getFilterData())
          .containsOnly(entry("UseTaggedPDF", false), entry("Unknown", "value"));
    }

    @Test
    void shouldCoverAllThePdfOptions() {

      // Every PdfOption must be reachable through the builder.
      final var options =
          PdfOptions.builder()
              .version(PdfVersion.PDF_1_7)
              .pdfUa(true)
              .tagged(true)
              .embedSourceDocument(true)
              .referenceXObjects(true)
              .images(i -> i.lossless(true).jpegQuality(90).maxResolution(300))
              .pages(p -> p.range("1").skipEmptyPages(true).placeholders(true).trackedChanges(true))
              .comments(c -> c.asPdfAnnotations(true).inMargin(true))
              .bookmarks(b -> b.export(true).openLevels(-1).asNamedDestinations(true))
              .forms(f -> f.export(true).submitFormat(SubmitFormat.FDF).allowDuplicateNames(true))
              .links(
                  l ->
                      l.relativeFileLinks(true)
                          .convertOdfTargetsToPdf(true)
                          .crossDocumentLinks(LinkTarget.DEFAULT))
              .initialView(
                  v ->
                      v.pane(Pane.NONE)
                          .page(1)
                          .magnification(Magnification.DEFAULT)
                          .zoom(100)
                          .layout(PageLayout.DEFAULT))
              .viewer(
                  v ->
                      v.resizeToInitialPage(true)
                          .centerWindow(true)
                          .fullScreen(true)
                          .displayDocumentTitle(true)
                          .hideMenubar(true)
                          .hideToolbar(true)
                          .hideWindowControls(true))
              .security(
                  s ->
                      s.openPassword("a")
                          .permissionPassword("b")
                          .printing(Printing.NONE)
                          .changes(Changes.NONE)
                          .copying(true)
                          .accessibilityAccess(true))
              .watermark(
                  w -> w.text("a").tiledText("b").color(0).fontName("c").fontHeight(1).rotation(0))
              .signature(
                  s ->
                      s.certificateSubjectName("a")
                          .certificatePem("b", "c")
                          .caPem("d")
                          .password("e")
                          .location("f")
                          .reason("g")
                          .contactInfo("h")
                          .timestampAuthority("i"))
              .presentation(
                  p -> p.hiddenSlides(true).notesPages(true).onlyNotesPages(true).transitions(true))
              .spreadsheet(s -> s.singlePageSheets(true).sheetRange("1"))
              .build();

      final var allNames = new HashSet<String>();
      Arrays.stream(PdfOption.values()).forEach(o -> allNames.add(o.getFilterDataName()));
      assertThat(options.getFilterData().keySet()).containsExactlyInAnyOrderElementsOf(allNames);
    }
  }

  @Nested
  class Validation {

    @Test
    void withPdfAAndPassword_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  PdfOptions.builder()
                      .version(PdfVersion.PDF_A_2B)
                      .security(security -> security.openPassword("secret"))
                      .build())
          .withMessage("PDF/A does not allow encryption: a password cannot be used with PDF_A_2B");
      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  PdfOptions.archive().toBuilder()
                      .security(security -> security.permissionPassword("secret"))
                      .build());
    }

    @Test
    void withPermissionWithoutPermissionPassword_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  PdfOptions.builder()
                      .security(security -> security.printing(Printing.NONE))
                      .build())
          .withMessage("Printing requires a permission password");
      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  PdfOptions.builder()
                      .security(security -> security.openPassword("open").copying(false))
                      .build())
          .withMessage("EnableCopyingOfContent requires a permission password");
    }

    @Test
    void withPdfUaAndNotTagged_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().pdfUa(true).tagged(false).build())
          .withMessageContaining("PDF/UA requires a tagged PDF");
    }

    @Test
    void withSignatureDetailsWithoutCertificate_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(
              () -> PdfOptions.builder().signature(signature -> signature.reason("x")).build())
          .withMessage("SignatureReason requires a certificate to sign with");

      // A certificate object given as raw FilterData is accepted.
      assertThat(
              PdfOptions.builder()
                  .signature(signature -> signature.reason("x"))
                  .filterData("SignatureCertificate", new Object())
                  .build()
                  .getFilterData())
          .containsKeys("SignatureReason", "SignatureCertificate");
    }

    @Test
    void withInvalidValues_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().images(images -> images.jpegQuality(0)));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().images(images -> images.jpegQuality(101)));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().images(images -> images.maxResolution(0)));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().bookmarks(bookmarks -> bookmarks.openLevels(0)));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().initialView(view -> view.page(0)));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().initialView(view -> view.zoom(0)));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().watermark(watermark -> watermark.color(-1)));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().watermark(watermark -> watermark.rotation(360)));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().pages(pages -> pages.range(" ")));
      assertThatNullPointerException().isThrownBy(() -> PdfOptions.builder().version(null));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().filterData(" ", 1));
    }
  }

  @Nested
  class NamedOptions {

    @Test
    void optionNames_ShouldListEveryOptionInTheOrderOfTheGroups() {

      final var names = PdfOptions.optionNames();

      assertThat(names)
          .startsWith("version", "pdf-ua", "tagged")
          .contains("images.jpeg-quality", "signature.certificate", "signature.private-key")
          .endsWith("spreadsheet.single-page-sheets", "spreadsheet.sheet-range");
      assertThatThrownBy(() -> names.add("other"))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void withEveryOptionName_ShouldBuildOptions() {

      // A valid value for each option: by name for the ones that are not booleans.
      final var values = new HashMap<String, String>();
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
      values.put("signature.certificate", "CERT");
      values.put("signature.private-key", "KEY");
      values.put("signature.ca", "CA");
      values.put("signature.password", "d");
      values.put("signature.location", "e");
      values.put("signature.reason", "f");
      values.put("signature.contact-info", "g");
      values.put("signature.timestamp-authority", "h");
      values.put("spreadsheet.sheet-range", "1-2");

      final var builder = PdfOptions.builder();
      for (final var name : PdfOptions.optionNames()) {
        builder.option(name, values.getOrDefault(name, "true"));
      }
      final var options = builder.build();

      // Every PdfOption can be set by name.
      final var allNames = new HashSet<String>();
      Arrays.stream(PdfOption.values()).forEach(o -> allNames.add(o.getFilterDataName()));
      assertThat(options.getFilterData().keySet()).containsExactlyInAnyOrderElementsOf(allNames);
      assertThat(options.getFilterData())
          .containsEntry("SignCertificateCertPem", "CERT")
          .containsEntry("SignCertificateKeyPem", "KEY")
          .containsEntry("SignCertificateCaPem", "CA")
          .containsEntry("SignCertificateSubjectName", "CN=Me");
    }

    @Test
    void withTypedValues_ShouldConvertThem() {

      final var options =
          PdfOptions.builder()
              .option("tagged", "TRUE")
              .option("images.lossless", "False")
              .option("images.jpeg-quality", " 80 ")
              .option("initial-view.magnification", "fit-width")
              .option("security.permission-password", "a=b")
              .option("security.printing", "low_resolution")
              .option("watermark.text", "Top secret")
              .option("watermark.color", "#FF0000")
              .option("watermark.rotation", "45")
              .build();

      assertThat(options.getFilterData())
          .containsEntry("UseTaggedPDF", true)
          .containsEntry("UseLosslessCompression", false)
          .containsEntry("Quality", 80)
          .containsEntry("Magnification", 2)
          .containsEntry("PermissionPassword", "a=b")
          .containsEntry("Printing", 1)
          .containsEntry("Watermark", "Top secret")
          .containsEntry("WatermarkColor", 0xFF0000)
          .containsEntry("WatermarkRotateAngle", 450);
    }

    @Test
    void withVersion_ShouldAcceptTheShortAndTheFullNames() {

      final var expected = new HashMap<String, PdfVersion>();
      expected.put("1.7", PdfVersion.PDF_1_7);
      expected.put("pdf-1-7", PdfVersion.PDF_1_7);
      expected.put("PDF_1_7", PdfVersion.PDF_1_7);
      expected.put("2.0", PdfVersion.PDF_2_0);
      expected.put("a-2b", PdfVersion.PDF_A_2B);
      expected.put("A_3B", PdfVersion.PDF_A_3B);
      expected.put("pdf-a-1b", PdfVersion.PDF_A_1B);
      expected.put("default", PdfVersion.DEFAULT);
      expected.forEach(
          (text, version) ->
              assertThat(PdfOptions.builder().option("version", text).build().getFilterData())
                  .as(text)
                  .containsEntry("SelectPdfVersion", version.getValue()));
    }

    @Test
    void withColorText_ShouldAcceptTheUsualForms() {

      for (final var text :
          new String[] {"00ff00", "#00FF00", "0x00ff00", "0X00FF00", " 00FF00 "}) {
        assertThat(PdfOptions.builder().option("watermark.color", text).build().getFilterData())
            .as(text)
            .containsEntry("WatermarkColor", 0x00FF00);
      }
    }

    @Test
    void withInvalidNameOrValue_ShouldThrowIllegalArgumentException() {

      assertThatNullPointerException()
          .isThrownBy(() -> PdfOptions.builder().option(null, "1"))
          .withMessage("name must not be null");
      assertThatNullPointerException()
          .isThrownBy(() -> PdfOptions.builder().option("tagged", null))
          .withMessage("value must not be null");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().option("pages.rang", "1"))
          .withMessageStartingWith("Unknown PDF option 'pages.rang'; expected one of: version, ")
          .withMessageContaining("pages.range");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().option("tagged", "yes"))
          .withMessage("Invalid value 'yes' for the PDF option 'tagged': expected true or false");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().option("images.jpeg-quality", "high"))
          .withMessage(
              "Invalid value 'high' for the PDF option 'images.jpeg-quality': expected a number");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().option("images.jpeg-quality", "0"))
          .withMessageContaining("jpegQuality must be between 1 and 100");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().option("security.printing", "draft"))
          .withMessage(
              "Invalid value 'draft' for the PDF option 'security.printing':"
                  + " expected one of: none, low-resolution, high-resolution");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().option("version", "3.0"))
          .withMessageContaining("expected one of: default, pdf-1-5");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().option("watermark.color", "red"))
          .withMessage(
              "Invalid value 'red' for the PDF option 'watermark.color':"
                  + " expected a color such as FF0000");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().option("watermark.color", "1000000"))
          .withMessageContaining("color must be an RGB value");
    }

    @Test
    void withCertificateWithoutPrivateKey_ShouldThrowIllegalArgumentException() {

      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().option("signature.certificate", "CERT").build())
          .withMessage(
              "The PDF options 'signature.certificate' and 'signature.private-key'"
                  + " must be used together");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> PdfOptions.builder().option("signature.private-key", "KEY").build())
          .withMessageContaining("must be used together");
    }

    @Test
    void withCertificateAndPrivateKey_ShouldSignWithThem() {

      final var options =
          PdfOptions.builder()
              .option("signature.private-key", "KEY")
              .option("signature.reason", "because")
              .option("signature.certificate", "CERT")
              .build();

      assertThat(options.getFilterData())
          .containsEntry("SignPDF", true)
          .containsEntry("SignCertificateCertPem", "CERT")
          .containsEntry("SignCertificateKeyPem", "KEY")
          .containsEntry("SignatureReason", "because");
      // Building again from the options keeps the pair.
      assertThat(options.toBuilder().build().getFilterData())
          .containsEntry("SignCertificateCertPem", "CERT");
    }
  }

  @Nested
  class Presets {

    @Test
    void archive_ShouldBePdfA2TaggedLosslessWithBookmarks() {
      assertThat(PdfOptions.archive().getFilterData())
          .containsOnly(
              entry("SelectPdfVersion", 2),
              entry("UseTaggedPDF", true),
              entry("ExportBookmarks", true),
              entry("UseLosslessCompression", true));
    }

    @Test
    void accessible_ShouldBePdfUaTaggedWithBookmarks() {
      assertThat(PdfOptions.accessible().getFilterData())
          .containsOnly(
              entry("PDFUACompliance", true),
              entry("UseTaggedPDF", true),
              entry("ExportBookmarks", true));
    }

    @Test
    void compact_ShouldReduceTheImages() {
      assertThat(PdfOptions.compact().getFilterData())
          .containsOnly(
              entry("UseTaggedPDF", true),
              entry("UseLosslessCompression", false),
              entry("Quality", 75),
              entry("ReduceImageResolution", true),
              entry("MaxImageResolution", 150));
    }

    @Test
    void toBuilder_ShouldDeriveNewOptionsWithoutChangingTheOriginal() {

      final var archive = PdfOptions.archive();
      final var derived =
          archive.toBuilder().pages(pages -> pages.range("1")).filterData("Extra", 1).build();

      assertThat(derived.getFilterData())
          .containsAllEntriesOf(archive.getFilterData())
          .containsEntry("PageRange", "1")
          .containsEntry("Extra", 1);
      assertThat(archive.getFilterData()).doesNotContainKeys("PageRange", "Extra");
    }
  }

  @Nested
  class Supports {

    @Test
    void shouldOnlySupportThePdfFormat() {

      final var options = PdfOptions.archive();

      assertThat(options.supports(DefaultDocumentFormatRegistry.PDF)).isTrue();
      assertThat(options.supports(DefaultDocumentFormatRegistry.ODT)).isFalse();
      assertThat(options.supports(DefaultDocumentFormatRegistry.PNG)).isFalse();
    }
  }

  @Nested
  class ApplyTo {

    @Test
    void withoutOptions_ShouldNotAddFilterData() {

      final var storeProperties = new HashMap<String, Object>();
      storeProperties.put("FilterName", "writer_pdf_Export");

      PdfOptions.builder().build().applyTo(storeProperties);

      assertThat(storeProperties).containsOnly(entry("FilterName", "writer_pdf_Export"));
    }

    @Test
    void withExistingFilterData_ShouldMergeAndTakePrecedence() {

      final var existing = new HashMap<String, Object>();
      existing.put("SelectPdfVersion", 16);
      existing.put("Quality", 50);
      final var storeProperties = new HashMap<String, Object>();
      storeProperties.put("FilterName", "writer_pdf_Export");
      storeProperties.put("FilterData", existing);

      PdfOptions.builder()
          .version(PdfVersion.PDF_1_5)
          .tagged(true)
          .build()
          .applyTo(storeProperties);

      assertThat(storeProperties).containsOnlyKeys("FilterName", "FilterData");
      assertThat(storeProperties.get("FilterData"))
          .isEqualTo(Map.of("SelectPdfVersion", 15, "Quality", 50, "UseTaggedPDF", true));
      // The map of the format or of the converter must not be modified.
      assertThat(existing).containsOnly(entry("SelectPdfVersion", 16), entry("Quality", 50));
    }

    @Test
    void withFilterDataThatIsNotAMap_ShouldThrowIllegalStateException() {

      final var storeProperties = new HashMap<String, Object>();
      storeProperties.put("FilterData", "foo");

      assertThatIllegalStateException()
          .isThrownBy(() -> PdfOptions.archive().applyTo(storeProperties))
          .withMessageContaining("java.lang.String");
    }
  }

  @Nested
  class GetUnsupportedOptions {

    private final PdfOptions options =
        PdfOptions.builder()
            .version(PdfVersion.PDF_A_4)
            .pdfUa(true)
            .tagged(true)
            .watermark(watermark -> watermark.text("DRAFT").color(0xFF0000))
            .presentation(presentation -> presentation.hiddenSlides(true))
            .build();

    @Test
    void withRecentLibreOffice_ShouldReturnNothing() {
      assertThat(options.getUnsupportedOptions(true, "25.2")).isEmpty();
      assertThat(options.getUnsupportedOptions(true, "26.2.6.2")).isEmpty();
    }

    @Test
    void withOlderLibreOffice_ShouldReturnTheOptionsItIgnores() {
      assertThat(options.getUnsupportedOptions(true, "7.3"))
          .containsExactlyInAnyOrder(
              "SelectPdfVersion=4 (PDF_A_4) requires LibreOffice 25.2 or later",
              "WatermarkColor requires LibreOffice 7.4 or later");
      assertThat(options.getUnsupportedOptions(true, "6.4.7"))
          .containsExactlyInAnyOrder(
              "SelectPdfVersion=4 (PDF_A_4) requires LibreOffice 25.2 or later",
              "PDFUACompliance requires LibreOffice 7.0 or later",
              "WatermarkColor requires LibreOffice 7.4 or later");
    }

    @Test
    void withOpenOffice_ShouldReturnTheLibreOfficeOnlyOptions() {
      assertThat(options.getUnsupportedOptions(false, "4.1.15"))
          .containsExactlyInAnyOrder(
              "SelectPdfVersion=4 (PDF_A_4) is not supported by Apache OpenOffice",
              "PDFUACompliance is not supported by Apache OpenOffice",
              "WatermarkColor is not supported by Apache OpenOffice",
              "ExportHiddenSlides is not supported by Apache OpenOffice");
    }

    @Test
    void withUnreadableVersion_ShouldReturnNothing() {
      assertThat(options.getUnsupportedOptions(true, "unknown")).isEmpty();
    }
  }

  @Nested
  class ToString {

    @Test
    void shouldHideTheSecrets() {

      final var str =
          PdfOptions.builder()
              .tagged(true)
              .security(security -> security.openPassword("open-secret"))
              .signature(signature -> signature.certificatePem("cert-secret", "key-secret"))
              .filterData("SomePassword", "extra-secret")
              .filterData("Other", 1)
              .build()
              .toString();

      assertThat(str)
          .startsWith("PdfOptions{")
          .contains("UseTaggedPDF=true", "DocumentOpenPassword=***", "SomePassword=***", "Other=1")
          .doesNotContain("open-secret", "cert-secret", "key-secret", "extra-secret");
    }
  }
}
