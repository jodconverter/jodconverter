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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDDestinationOrAction;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionLaunch;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.pdf.PdfFormOptions.SubmitFormat;
import org.jodconverter.core.pdf.PdfInitialViewOptions.Magnification;
import org.jodconverter.core.pdf.PdfInitialViewOptions.PageLayout;
import org.jodconverter.core.pdf.PdfInitialViewOptions.Pane;
import org.jodconverter.core.pdf.PdfLinkOptions.LinkTarget;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.core.pdf.PdfSecurityOptions.Changes;
import org.jodconverter.core.pdf.PdfSecurityOptions.Printing;
import org.jodconverter.core.pdf.PdfVersion;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.utils.Info;

/**
 * Contains tests for conversions using {@link PdfOptions}, with a real office installation: each
 * option is converted, and the PDF document it produces is inspected to check that the option had
 * its effect.
 *
 * <p>The source documents of the {@code pdf-options} folder contain what the options act on:
 *
 * <ul>
 *   <li>{@code writer.fodt}: headings, a comment, a bookmark, a placeholder field, tracked changes,
 *       links, form fields with a submit button, a 600 x 600 pixels image shown in one inch, a PDF
 *       image, and pages that make Writer insert a blank page.
 *   <li>{@code impress.fodp}: three slides with notes and a transition, the second one hidden.
 *   <li>{@code calc.fods}: two sheets of several pages each.
 * </ul>
 *
 * <p>The tests of the options that the office installation is too old for are skipped. The digital
 * signature is not tested here: it needs a certificate, and an office process that has not yet
 * exported any PDF document without signature.
 */
@ExtendWith(LocalOfficeManagerExtension.class)
class PdfOptionsITest {

  private static final File WRITER_FILE = documentFile("/pdf-options/writer.fodt");
  private static final File IMPRESS_FILE = documentFile("/pdf-options/impress.fodp");
  private static final File CALC_FILE = documentFile("/pdf-options/calc.fods");

  private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)");
  private static final AtomicReference<String> OFFICE_VERSION = new AtomicReference<>();

  private int fileCount;

  // Converts a document with the given options and returns the PDF document produced.
  private File convert(
      final DocumentConverter converter,
      final File testFolder,
      final File source,
      final Consumer<PdfOptions.Builder> config)
      throws OfficeException {

    final PdfOptions.Builder builder = PdfOptions.builder();
    config.accept(builder);
    final File target = new File(testFolder, "out" + fileCount++ + ".pdf");
    converter.convert(source).to(target).with(builder.build()).execute();
    return target;
  }

  private File writer(
      final DocumentConverter converter,
      final File testFolder,
      final Consumer<PdfOptions.Builder> config)
      throws OfficeException {
    return convert(converter, testFolder, WRITER_FILE, config);
  }

  // Skips the test when the office installation is older than the given LibreOffice version.
  private static void assumeLibreOffice(
      final OfficeManager manager, final int major, final int minor) throws OfficeException {

    if (OFFICE_VERSION.get() == null) {
      manager.execute(
          context ->
              OFFICE_VERSION.set(
                  Info.getOfficeVersionLong(((LocalOfficeContext) context).getComponentContext())));
    }
    final Matcher matcher = VERSION_PATTERN.matcher(String.valueOf(OFFICE_VERSION.get()));
    assumeTrue(matcher.find(), "Unknown office version: " + OFFICE_VERSION.get());
    final int officeMajor = Integer.parseInt(matcher.group(1));
    final int officeMinor = Integer.parseInt(matcher.group(2));
    assumeTrue(
        officeMajor > major || officeMajor == major && officeMinor >= minor,
        "Requires LibreOffice " + major + "." + minor + ", found " + OFFICE_VERSION.get());
  }

  private static String text(final File file) throws IOException {
    try (PDDocument doc = Loader.loadPDF(file)) {
      return new PDFTextStripper().getText(doc);
    }
  }

  private static int pageCount(final File file) throws IOException {
    try (PDDocument doc = Loader.loadPDF(file)) {
      return doc.getNumberOfPages();
    }
  }

  private static String header(final File file) throws IOException {
    return Files.readString(file.toPath(), StandardCharsets.ISO_8859_1).substring(0, 8);
  }

  // Gets the XMP metadata of the document, which tells its PDF/A and PDF/UA conformance.
  private static String xmp(final File file) throws IOException {
    try (PDDocument doc = Loader.loadPDF(file)) {
      return new String(
          doc.getDocumentCatalog().getMetadata().toByteArray(), StandardCharsets.UTF_8);
    }
  }

  private static boolean isTagged(final File file) throws IOException {
    try (PDDocument doc = Loader.loadPDF(file)) {
      return doc.getDocumentCatalog().getStructureTreeRoot() != null;
    }
  }

  // Gets the value of a name entry of the document catalog, or null if it is absent.
  private static String catalogName(final File file, final String key) throws IOException {
    try (PDDocument doc = Loader.loadPDF(file)) {
      return doc.getDocumentCatalog().getCOSObject().getNameAsString(key);
    }
  }

  private static boolean catalogHas(final File file, final String key) throws IOException {
    try (PDDocument doc = Loader.loadPDF(file)) {
      return doc.getDocumentCatalog().getCOSObject().containsKey(key);
    }
  }

  private static <T extends PDAnnotation> List<T> annotations(
      final PDDocument doc, final Class<T> type) throws IOException {
    final List<T> found = new ArrayList<>();
    for (final PDPage page : doc.getPages()) {
      for (final PDAnnotation annotation : page.getAnnotations()) {
        if (type.isInstance(annotation)) {
          found.add(type.cast(annotation));
        }
      }
    }
    return found;
  }

  // Gets the image of the first page (the source document has only one).
  private static PDImageXObject image(final PDDocument doc) throws IOException {
    final PDResources resources = doc.getPage(0).getResources();
    for (final COSName name : resources.getXObjectNames()) {
      final PDXObject xobject = resources.getXObject(name);
      if (xobject instanceof PDImageXObject img && img.getWidth() > 50) {
        return img;
      }
    }
    throw new IllegalStateException("No image found");
  }

  private static List<PDFormXObject> forms(final PDDocument doc) throws IOException {
    final List<PDFormXObject> found = new ArrayList<>();
    final PDResources resources = doc.getPage(0).getResources();
    for (final COSName name : resources.getXObjectNames()) {
      if (resources.getXObject(name) instanceof PDFormXObject form) {
        found.add(form);
      }
    }
    return found;
  }

  // Gets the form XObject that draws the watermark of the first page.
  private static PDFormXObject watermark(final PDDocument doc) throws IOException {
    for (final PDFormXObject form : forms(doc)) {
      if (content(form).contains(" Tf")) {
        return form;
      }
    }
    throw new IllegalStateException("No watermark found");
  }

  private static String content(final PDFormXObject form) throws IOException {
    return new String(form.getStream().toByteArray(), StandardCharsets.ISO_8859_1);
  }

  private static String firstGroup(final String regex, final String input) {
    final Matcher matcher = Pattern.compile(regex).matcher(input);
    assertThat(matcher.find()).as("'%s' in '%s'", regex, input).isTrue();
    return matcher.group(1);
  }

  // Gets the action of the link to the "other" document of the source document.
  private static PDAction fileLinkAction(final File file) throws IOException {
    try (PDDocument doc = Loader.loadPDF(file)) {
      for (final PDAnnotationLink link : annotations(doc, PDAnnotationLink.class)) {
        final PDAction action = link.getAction();
        if (action instanceof PDActionLaunch
            || action instanceof PDActionURI uri && uri.getURI().contains("other")) {
          return action;
        }
      }
    }
    throw new IllegalStateException("No link to the other document found");
  }

  private static String fileLinkUri(final File file) throws IOException {
    return ((PDActionURI) fileLinkAction(file)).getURI();
  }

  private static PDPageDestination openDestination(final PDDocument doc) throws IOException {
    final PDDestinationOrAction openAction = doc.getDocumentCatalog().getOpenAction();
    if (openAction instanceof PDActionGoTo goTo) {
      return (PDPageDestination) goTo.getDestination();
    }
    return (PDPageDestination) openAction;
  }

  private static PDViewerPreferences viewerPreferences(final PDDocument doc) {
    final PDViewerPreferences preferences = doc.getDocumentCatalog().getViewerPreferences();
    return preferences == null ? new PDViewerPreferences(new COSDictionary()) : preferences;
  }

  private static List<String> fieldNames(final File file) throws IOException {
    final List<String> names = new ArrayList<>();
    try (PDDocument doc = Loader.loadPDF(file)) {
      for (final PDAnnotationWidget widget : annotations(doc, PDAnnotationWidget.class)) {
        names.add(widget.getCOSObject().getString(COSName.T));
      }
    }
    return names;
  }

  // Gets the permission bits of an encrypted document.
  private static int permissionBits(final File file) throws IOException {
    try (PDDocument doc = Loader.loadPDF(file)) {
      return doc.getEncryption().getPermissions();
    }
  }

  // Gets whether the bit at the given position (starting at 1, as in the PDF specification) is set.
  private static boolean bit(final int value, final int position) {
    return (value & 1 << position - 1) != 0;
  }

  @Nested
  class General {

    @Test
    void version_ShouldProduceThatPdfVersion(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final Map<PdfVersion, String> headers = new HashMap<>();
      headers.put(PdfVersion.PDF_1_5, "%PDF-1.5");
      headers.put(PdfVersion.PDF_1_6, "%PDF-1.6");
      headers.put(PdfVersion.PDF_1_7, "%PDF-1.7");
      for (final Map.Entry<PdfVersion, String> entry : headers.entrySet()) {
        final File pdf = writer(converter, testFolder, b -> b.version(entry.getKey()));
        assertThat(header(pdf)).as("%s", entry.getKey()).isEqualTo(entry.getValue());
        assertThat(xmp(pdf)).doesNotContain("pdfaid:part");
      }
    }

    @Test
    void versionPdfA_ShouldProduceThatConformance(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final Map<PdfVersion, String> parts = new HashMap<>();
      parts.put(PdfVersion.PDF_A_1B, "1");
      parts.put(PdfVersion.PDF_A_2B, "2");
      parts.put(PdfVersion.PDF_A_3B, "3");
      for (final Map.Entry<PdfVersion, String> entry : parts.entrySet()) {
        final File pdf = writer(converter, testFolder, b -> b.version(entry.getKey()));
        assertThat(xmp(pdf))
            .as("%s", entry.getKey())
            .containsPattern("<pdfaid:part>\\s*" + entry.getValue() + "\\s*</pdfaid:part>");
      }
    }

    @Test
    void versionPdf2AndPdfA4_ShouldProduceThem(
        final @TempDir File testFolder,
        final DocumentConverter converter,
        final OfficeManager manager)
        throws IOException, OfficeException {

      assumeLibreOffice(manager, 25, 2);

      final File pdf2 = writer(converter, testFolder, b -> b.version(PdfVersion.PDF_2_0));
      final File pdfA4 = writer(converter, testFolder, b -> b.version(PdfVersion.PDF_A_4));

      assertThat(header(pdf2)).isEqualTo("%PDF-2.0");
      assertThat(xmp(pdfA4)).containsPattern("<pdfaid:part>\\s*4\\s*</pdfaid:part>");
    }

    @Test
    void pdfUa_ShouldDeclarePdfUaConformance(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on = writer(converter, testFolder, b -> b.pdfUa(true));
      final File off = writer(converter, testFolder, b -> b.pdfUa(false).tagged(true));

      assertThat(xmp(on)).contains("pdfuaid:part");
      assertThat(isTagged(on)).isTrue();
      assertThat(xmp(off)).doesNotContain("pdfuaid:part");
    }

    @Test
    void tagged_ShouldControlTheDocumentStructure(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      assertThat(isTagged(writer(converter, testFolder, b -> b.tagged(true)))).isTrue();
      assertThat(isTagged(writer(converter, testFolder, b -> b.tagged(false)))).isFalse();
    }

    @Test
    void embedSourceDocument_ShouldEmbedTheSource(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on = writer(converter, testFolder, b -> b.embedSourceDocument(true));
      final File off = writer(converter, testFolder, b -> b.embedSourceDocument(false));

      final COSName additionalStreams = COSName.getPDFName("AdditionalStreams");
      try (PDDocument docOn = Loader.loadPDF(on);
          PDDocument docOff = Loader.loadPDF(off)) {
        assertThat(docOn.getDocument().getTrailer().containsKey(additionalStreams)).isTrue();
        assertThat(docOff.getDocument().getTrailer().containsKey(additionalStreams)).isFalse();
      }
      assertThat(on.length()).isGreaterThan(off.length());
    }

    @Test
    void referenceXObjects_ShouldExportThePdfImageAsReferenceXObject(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on = writer(converter, testFolder, b -> b.referenceXObjects(true));
      final File off = writer(converter, testFolder, b -> b.referenceXObjects(false));

      final COSName ref = COSName.getPDFName("Ref");
      try (PDDocument docOn = Loader.loadPDF(on);
          PDDocument docOff = Loader.loadPDF(off)) {
        assertThat(forms(docOn)).anyMatch(form -> form.getCOSObject().containsKey(ref));
        assertThat(forms(docOff)).noneMatch(form -> form.getCOSObject().containsKey(ref));
      }
    }
  }

  @Nested
  class Images {

    @Test
    void lossless_ShouldChooseTheImageCompression(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on =
          writer(
              converter, testFolder, b -> b.images(i -> i.lossless(true).reduceResolution(false)));
      final File off =
          writer(
              converter, testFolder, b -> b.images(i -> i.lossless(false).reduceResolution(false)));

      try (PDDocument docOn = Loader.loadPDF(on);
          PDDocument docOff = Loader.loadPDF(off)) {
        assertThat(image(docOn).getSuffix()).isEqualTo("png");
        assertThat(image(docOff).getSuffix()).isEqualTo("jpg");
      }
    }

    @Test
    void jpegQuality_ShouldChangeTheImageSize(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File low =
          writer(
              converter,
              testFolder,
              b -> b.images(i -> i.lossless(false).jpegQuality(10).reduceResolution(false)));
      final File high =
          writer(
              converter,
              testFolder,
              b -> b.images(i -> i.lossless(false).jpegQuality(95).reduceResolution(false)));

      try (PDDocument docLow = Loader.loadPDF(low);
          PDDocument docHigh = Loader.loadPDF(high)) {
        assertThat(image(docLow).getCOSObject().getLength() * 2)
            .isLessThan(image(docHigh).getCOSObject().getLength());
      }
    }

    @Test
    void resolution_ShouldReduceTheImage(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      // The image has 600 pixels shown in one inch.
      final File full =
          writer(
              converter, testFolder, b -> b.images(i -> i.lossless(true).reduceResolution(false)));
      final File dpi75 =
          writer(converter, testFolder, b -> b.images(i -> i.lossless(true).maxResolution(75)));
      final File dpi150 =
          writer(converter, testFolder, b -> b.images(i -> i.lossless(true).maxResolution(150)));

      try (PDDocument docFull = Loader.loadPDF(full);
          PDDocument doc75 = Loader.loadPDF(dpi75);
          PDDocument doc150 = Loader.loadPDF(dpi150)) {
        assertThat(image(docFull).getWidth()).isEqualTo(600);
        assertThat(image(doc75).getWidth()).isEqualTo(75);
        assertThat(image(doc150).getWidth()).isEqualTo(150);
      }
    }
  }

  @Nested
  class Pages {

    @Test
    void range_ShouldExportOnlyThosePages(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File second = writer(converter, testFolder, b -> b.pages(p -> p.range("2")));
      final File firstTwo = writer(converter, testFolder, b -> b.pages(p -> p.range("1-2")));

      assertThat(pageCount(second)).isEqualTo(1);
      assertThat(text(second)).contains("Chapter Two").doesNotContain("Chapter One");
      assertThat(pageCount(firstTwo)).isEqualTo(2);
    }

    @Test
    void skipEmptyPages_ShouldLeaveOutTheAutomaticBlankPages(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File skipped = writer(converter, testFolder, b -> b.pages(p -> p.skipEmptyPages(true)));
      final File kept = writer(converter, testFolder, b -> b.pages(p -> p.skipEmptyPages(false)));

      assertThat(pageCount(kept)).isEqualTo(pageCount(skipped) + 1);
    }

    @Test
    void placeholders_ShouldExportThePlaceholderFields(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on = writer(converter, testFolder, b -> b.pages(p -> p.placeholders(true)));
      final File off = writer(converter, testFolder, b -> b.pages(p -> p.placeholders(false)));

      assertThat(text(on)).contains("PLACEHOLDERTEXT");
      assertThat(text(off)).doesNotContain("PLACEHOLDERTEXT");
    }

    @Test
    void trackedChanges_ShouldShowTheDeletedText(
        final @TempDir File testFolder,
        final DocumentConverter converter,
        final OfficeManager manager)
        throws IOException, OfficeException {

      assumeLibreOffice(manager, 26, 2);

      final File on = writer(converter, testFolder, b -> b.pages(p -> p.trackedChanges(true)));
      final File off = writer(converter, testFolder, b -> b.pages(p -> p.trackedChanges(false)));

      assertThat(text(on)).contains("DELETEDWORD");
      assertThat(text(off)).doesNotContain("DELETEDWORD");
    }
  }

  @Nested
  class Comments {

    @Test
    void asPdfAnnotations_ShouldExportTheCommentAsAnnotation(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on = writer(converter, testFolder, b -> b.comments(c -> c.asPdfAnnotations(true)));
      final File off =
          writer(converter, testFolder, b -> b.comments(c -> c.asPdfAnnotations(false)));

      try (PDDocument docOn = Loader.loadPDF(on);
          PDDocument docOff = Loader.loadPDF(off)) {
        assertThat(annotations(docOn, PDAnnotationText.class)).hasSize(1);
        assertThat(annotations(docOff, PDAnnotationText.class)).isEmpty();
      }
    }

    @Test
    void inMargin_ShouldPrintTheCommentOnThePage(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on =
          writer(
              converter,
              testFolder,
              b -> b.comments(c -> c.asPdfAnnotations(false).inMargin(true)));
      final File off =
          writer(
              converter,
              testFolder,
              b -> b.comments(c -> c.asPdfAnnotations(false).inMargin(false)));

      assertThat(text(on)).contains("COMMENTBODY");
      assertThat(text(off)).doesNotContain("COMMENTBODY");
    }
  }

  @Nested
  class Bookmarks {

    private boolean hasNamedDestinations(final File file) throws IOException {
      try (PDDocument doc = Loader.loadPDF(file)) {
        final COSDictionary catalog = doc.getDocumentCatalog().getCOSObject();
        final COSBase names = catalog.getDictionaryObject(COSName.NAMES);
        return catalog.containsKey(COSName.DESTS)
            || names instanceof COSDictionary dictionary && dictionary.containsKey(COSName.DESTS);
      }
    }

    @Test
    void export_ShouldExportTheOutline(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on = writer(converter, testFolder, b -> b.bookmarks(m -> m.export(true)));
      final File off = writer(converter, testFolder, b -> b.bookmarks(m -> m.export(false)));

      assertThat(catalogHas(on, "Outlines")).isTrue();
      assertThat(catalogHas(off, "Outlines")).isFalse();
    }

    @Test
    void openLevels_ShouldOpenOrCloseTheOutlineItems(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File oneLevel =
          writer(converter, testFolder, b -> b.bookmarks(m -> m.export(true).openLevels(1)));
      final File allLevels =
          writer(converter, testFolder, b -> b.bookmarks(m -> m.export(true).openLevels(-1)));

      try (PDDocument docOne = Loader.loadPDF(oneLevel);
          PDDocument docAll = Loader.loadPDF(allLevels)) {
        assertThat(docOne.getDocumentCatalog().getDocumentOutline().getFirstChild().isNodeOpen())
            .isFalse();
        assertThat(docAll.getDocumentCatalog().getDocumentOutline().getFirstChild().isNodeOpen())
            .isTrue();
      }
    }

    @Test
    void asNamedDestinations_ShouldExportTheBookmarksAsDestinations(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on =
          writer(converter, testFolder, b -> b.bookmarks(m -> m.asNamedDestinations(true)));
      final File off =
          writer(converter, testFolder, b -> b.bookmarks(m -> m.asNamedDestinations(false)));

      assertThat(hasNamedDestinations(on)).isTrue();
      assertThat(hasNamedDestinations(off)).isFalse();
    }
  }

  @Nested
  class Forms {

    @Test
    void export_ShouldExportFillableFields(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on = writer(converter, testFolder, b -> b.forms(f -> f.export(true)));
      final File off = writer(converter, testFolder, b -> b.forms(f -> f.export(false)));

      assertThat(fieldNames(on)).contains("samename", "SubmitButton");
      assertThat(fieldNames(off)).isEmpty();
    }

    @Test
    void submitFormat_ShouldSetTheFlagsOfTheSubmitAction(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final Map<SubmitFormat, Integer> flags = new HashMap<>();
      for (final SubmitFormat format : SubmitFormat.values()) {
        final File pdf =
            writer(converter, testFolder, b -> b.forms(f -> f.export(true).submitFormat(format)));
        try (PDDocument doc = Loader.loadPDF(pdf)) {
          for (final PDAnnotationWidget widget : annotations(doc, PDAnnotationWidget.class)) {
            if ("SubmitButton".equals(widget.getCOSObject().getString(COSName.T))) {
              final COSDictionary action =
                  widget.getCOSObject().getCOSDictionary(COSName.AA).getCOSDictionary(COSName.D);
              assertThat(action.getNameAsString(COSName.S)).isEqualTo("SubmitForm");
              flags.put(format, action.getInt(COSName.getPDFName("Flags")));
            }
          }
        }
      }

      // Flags of a submit-form action, by bit position: 3 "HTML format", 4 "get method",
      // 6 "XFDF format", 9 "submit the PDF document".
      assertThat(flags)
          .containsEntry(SubmitFormat.FDF, 8)
          .containsEntry(SubmitFormat.PDF, 264)
          .containsEntry(SubmitFormat.HTML, 12)
          .containsEntry(SubmitFormat.XML, 40);
    }

    @Test
    void allowDuplicateNames_ShouldKeepOrRenameTheFieldsNamedAlike(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on =
          writer(
              converter, testFolder, b -> b.forms(f -> f.export(true).allowDuplicateNames(true)));
      final File off =
          writer(
              converter, testFolder, b -> b.forms(f -> f.export(true).allowDuplicateNames(false)));

      assertThat(fieldNames(on))
          .filteredOn(name -> name.startsWith("samename"))
          .containsExactly("samename", "samename");
      assertThat(fieldNames(off))
          .filteredOn(name -> name.startsWith("samename"))
          .containsExactlyInAnyOrder("samename", "samename_2");
    }
  }

  @Nested
  class Links {

    @Test
    void relativeFileLinks_ShouldExportRelativeOrAbsoluteFileLinks(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on = writer(converter, testFolder, b -> b.links(l -> l.relativeFileLinks(true)));
      final File off = writer(converter, testFolder, b -> b.links(l -> l.relativeFileLinks(false)));

      assertThat(fileLinkUri(on)).isEqualTo("other.odt");
      assertThat(fileLinkUri(off)).startsWith("file:///").endsWith("/other.odt");
    }

    @Test
    void convertOdfTargetsToPdf_ShouldChangeTheExtensionOfTheTarget(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on =
          writer(converter, testFolder, b -> b.links(l -> l.convertOdfTargetsToPdf(true)));
      final File off =
          writer(converter, testFolder, b -> b.links(l -> l.convertOdfTargetsToPdf(false)));

      assertThat(fileLinkUri(on)).endsWith("other.pdf");
      assertThat(fileLinkUri(off)).endsWith("other.odt");
    }

    @Test
    void crossDocumentLinks_ShouldChooseTheActionOfTheLink(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File reader =
          writer(
              converter,
              testFolder,
              b -> b.links(l -> l.crossDocumentLinks(LinkTarget.PDF_READER)));
      final File browser =
          writer(
              converter, testFolder, b -> b.links(l -> l.crossDocumentLinks(LinkTarget.BROWSER)));
      final File byDefault =
          writer(
              converter, testFolder, b -> b.links(l -> l.crossDocumentLinks(LinkTarget.DEFAULT)));

      assertThat(fileLinkAction(reader)).isInstanceOf(PDActionLaunch.class);
      assertThat(fileLinkAction(browser)).isInstanceOf(PDActionURI.class);
      assertThat(fileLinkAction(byDefault)).isInstanceOf(PDActionURI.class);
    }
  }

  @Nested
  class InitialView {

    @Test
    void pane_ShouldSetThePageMode(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File none = writer(converter, testFolder, b -> b.initialView(v -> v.pane(Pane.NONE)));
      final File bookmarks =
          writer(converter, testFolder, b -> b.initialView(v -> v.pane(Pane.BOOKMARKS)));
      final File thumbnails =
          writer(converter, testFolder, b -> b.initialView(v -> v.pane(Pane.THUMBNAILS)));

      // Without page mode, a viewer shows no pane.
      assertThat(catalogName(none, "PageMode")).isIn(null, "UseNone");
      assertThat(catalogName(bookmarks, "PageMode")).isEqualTo("UseOutlines");
      assertThat(catalogName(thumbnails, "PageMode")).isEqualTo("UseThumbs");
    }

    @Test
    void page_ShouldOpenTheDocumentOnThatPage(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File pdf = writer(converter, testFolder, b -> b.initialView(v -> v.page(2)));

      try (PDDocument doc = Loader.loadPDF(pdf)) {
        assertThat(openDestination(doc).retrievePageNumber()).isEqualTo(1);
      }
    }

    @Test
    void magnification_ShouldSetTheFitOfTheOpenAction(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File fitPage =
          writer(
              converter,
              testFolder,
              b -> b.initialView(v -> v.magnification(Magnification.FIT_PAGE)));
      final File fitWidth =
          writer(
              converter,
              testFolder,
              b -> b.initialView(v -> v.magnification(Magnification.FIT_WIDTH)));
      final File fitVisible =
          writer(
              converter,
              testFolder,
              b -> b.initialView(v -> v.magnification(Magnification.FIT_VISIBLE)));

      try (PDDocument docPage = Loader.loadPDF(fitPage);
          PDDocument docWidth = Loader.loadPDF(fitWidth);
          PDDocument docVisible = Loader.loadPDF(fitVisible)) {
        assertThat(openDestination(docPage)).isInstanceOf(PDPageFitDestination.class);
        assertThat(openDestination(docWidth))
            .isInstanceOfSatisfying(
                PDPageFitWidthDestination.class,
                destination -> assertThat(destination.fitBoundingBox()).isFalse());
        assertThat(openDestination(docVisible))
            .isInstanceOfSatisfying(
                PDPageFitWidthDestination.class,
                destination -> assertThat(destination.fitBoundingBox()).isTrue());
      }
    }

    @Test
    void zoom_ShouldSetTheZoomOfTheOpenAction(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File pdf = writer(converter, testFolder, b -> b.initialView(v -> v.zoom(75)));

      try (PDDocument doc = Loader.loadPDF(pdf)) {
        assertThat(openDestination(doc))
            .isInstanceOfSatisfying(
                PDPageXYZDestination.class,
                destination -> assertThat(destination.getZoom()).isEqualTo(0.75f));
      }
    }

    @Test
    void layout_ShouldSetThePageLayout(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File single =
          writer(converter, testFolder, b -> b.initialView(v -> v.layout(PageLayout.SINGLE_PAGE)));
      final File continuous =
          writer(converter, testFolder, b -> b.initialView(v -> v.layout(PageLayout.CONTINUOUS)));
      final File facing =
          writer(
              converter,
              testFolder,
              b -> b.initialView(v -> v.layout(PageLayout.CONTINUOUS_FACING)));
      final File byDefault =
          writer(converter, testFolder, b -> b.initialView(v -> v.layout(PageLayout.DEFAULT)));

      assertThat(catalogName(single, "PageLayout")).isEqualTo("SinglePage");
      assertThat(catalogName(continuous, "PageLayout")).isEqualTo("OneColumn");
      assertThat(catalogName(facing, "PageLayout")).isEqualTo("TwoColumnRight");
      assertThat(catalogName(byDefault, "PageLayout")).isNull();
    }
  }

  @Nested
  class Viewer {

    @Test
    void windowOptions_ShouldSetTheViewerPreferences(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on =
          writer(
              converter,
              testFolder,
              b ->
                  b.viewer(
                      v ->
                          v.resizeToInitialPage(true)
                              .centerWindow(true)
                              .displayDocumentTitle(true)
                              .hideMenubar(true)
                              .hideToolbar(true)
                              .hideWindowControls(true)));
      final File off =
          writer(
              converter,
              testFolder,
              b ->
                  b.viewer(
                      v ->
                          v.resizeToInitialPage(false)
                              .centerWindow(false)
                              .displayDocumentTitle(false)
                              .hideMenubar(false)
                              .hideToolbar(false)
                              .hideWindowControls(false)));

      try (PDDocument docOn = Loader.loadPDF(on);
          PDDocument docOff = Loader.loadPDF(off)) {
        final PDViewerPreferences prefsOn = viewerPreferences(docOn);
        assertThat(prefsOn.fitWindow()).isTrue();
        assertThat(prefsOn.centerWindow()).isTrue();
        assertThat(prefsOn.displayDocTitle()).isTrue();
        assertThat(prefsOn.hideMenubar()).isTrue();
        assertThat(prefsOn.hideToolbar()).isTrue();
        assertThat(prefsOn.hideWindowUI()).isTrue();
        final PDViewerPreferences prefsOff = viewerPreferences(docOff);
        assertThat(prefsOff.fitWindow()).isFalse();
        assertThat(prefsOff.centerWindow()).isFalse();
        assertThat(prefsOff.displayDocTitle()).isFalse();
        assertThat(prefsOff.hideMenubar()).isFalse();
        assertThat(prefsOff.hideToolbar()).isFalse();
        assertThat(prefsOff.hideWindowUI()).isFalse();
      }
    }

    @Test
    void eachWindowOption_ShouldOnlySetItsOwnPreference(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File center = writer(converter, testFolder, b -> b.viewer(v -> v.centerWindow(true)));
      final File toolbar = writer(converter, testFolder, b -> b.viewer(v -> v.hideToolbar(true)));

      try (PDDocument docCenter = Loader.loadPDF(center);
          PDDocument docToolbar = Loader.loadPDF(toolbar)) {
        assertThat(viewerPreferences(docCenter).centerWindow()).isTrue();
        assertThat(viewerPreferences(docCenter).hideToolbar()).isFalse();
        assertThat(viewerPreferences(docToolbar).hideToolbar()).isTrue();
        assertThat(viewerPreferences(docToolbar).centerWindow()).isFalse();
      }
    }

    @Test
    void fullScreen_ShouldSetThePageMode(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on = writer(converter, testFolder, b -> b.viewer(v -> v.fullScreen(true)));
      final File off = writer(converter, testFolder, b -> b.viewer(v -> v.fullScreen(false)));

      assertThat(catalogName(on, "PageMode")).isEqualTo("FullScreen");
      assertThat(catalogName(off, "PageMode")).isNotEqualTo("FullScreen");
    }
  }

  @Nested
  class Security {

    private static final String OWNER_PASSWORD = "ownerpw";

    @Test
    void openPassword_ShouldRequireThePasswordToOpen(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File pdf =
          writer(converter, testFolder, b -> b.security(s -> s.openPassword("openpw")));

      assertThatExceptionOfType(InvalidPasswordException.class)
          .isThrownBy(() -> Loader.loadPDF(pdf, "wrong").close());
      try (PDDocument doc = Loader.loadPDF(pdf, "openpw")) {
        assertThat(doc.isEncrypted()).isTrue();
        assertThat(doc.getNumberOfPages()).isPositive();
      }
    }

    @Test
    void permissionPassword_ShouldEncryptWithoutPasswordToOpen(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File pdf =
          writer(converter, testFolder, b -> b.security(s -> s.permissionPassword(OWNER_PASSWORD)));

      try (PDDocument asUser = Loader.loadPDF(pdf);
          PDDocument asOwner = Loader.loadPDF(pdf, OWNER_PASSWORD)) {
        assertThat(asUser.isEncrypted()).isTrue();
        assertThat(asUser.getCurrentAccessPermission().isOwnerPermission()).isFalse();
        assertThat(asOwner.getCurrentAccessPermission().isOwnerPermission()).isTrue();
      }
    }

    @Test
    void printing_ShouldSetThePrintPermissions(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final Map<Printing, Integer> bits = new HashMap<>();
      for (final Printing printing : Printing.values()) {
        bits.put(
            printing,
            permissionBits(
                writer(
                    converter,
                    testFolder,
                    b ->
                        b.security(s -> s.permissionPassword(OWNER_PASSWORD).printing(printing)))));
      }

      // Bit 3: print. Bit 12: print in high quality.
      assertThat(bit(bits.get(Printing.NONE), 3)).isFalse();
      assertThat(bit(bits.get(Printing.LOW_RESOLUTION), 3)).isTrue();
      assertThat(bit(bits.get(Printing.LOW_RESOLUTION), 12)).isFalse();
      assertThat(bit(bits.get(Printing.HIGH_RESOLUTION), 3)).isTrue();
      assertThat(bit(bits.get(Printing.HIGH_RESOLUTION), 12)).isTrue();
    }

    @Test
    void changes_ShouldSetTheChangePermissions(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      // The bits that are set, among: 4 modify, 6 annotate, 9 fill in forms, 11 assemble.
      final Map<Changes, String> bits = new HashMap<>();
      for (final Changes changes : Changes.values()) {
        final int value =
            permissionBits(
                writer(
                    converter,
                    testFolder,
                    b -> b.security(s -> s.permissionPassword(OWNER_PASSWORD).changes(changes))));
        final List<String> set = new ArrayList<>();
        for (final int position : new int[] {4, 6, 9, 11}) {
          if (bit(value, position)) {
            set.add(String.valueOf(position));
          }
        }
        bits.put(changes, String.join(" ", set));
      }

      assertThat(bits).containsEntry(Changes.NONE, "");
      assertThat(bits.values()).doesNotHaveDuplicates();
      assertThat(bits.get(Changes.PAGES)).contains("11");
      assertThat(bits.get(Changes.FORMS)).contains("9");
      assertThat(bits.get(Changes.FORMS_AND_COMMENTS)).contains("6");
      assertThat(bits.get(Changes.ALL_EXCEPT_EXTRACTION)).contains("4");
    }

    @Test
    void copyingAndAccessibility_ShouldSetTheExtractPermissions(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final int allowed =
          permissionBits(
              writer(
                  converter,
                  testFolder,
                  b -> b.security(s -> s.permissionPassword(OWNER_PASSWORD).copying(true))));
      final int accessibilityOnly =
          permissionBits(
              writer(
                  converter,
                  testFolder,
                  b ->
                      b.security(
                          s ->
                              s.permissionPassword(OWNER_PASSWORD)
                                  .copying(false)
                                  .accessibilityAccess(true))));
      final int denied =
          permissionBits(
              writer(
                  converter,
                  testFolder,
                  b ->
                      b.security(
                          s ->
                              s.permissionPassword(OWNER_PASSWORD)
                                  .copying(false)
                                  .accessibilityAccess(false))));

      // Bit 5: copy. Bit 10: extract for accessibility.
      assertThat(bit(allowed, 5)).isTrue();
      assertThat(bit(accessibilityOnly, 5)).isFalse();
      assertThat(bit(accessibilityOnly, 10)).isTrue();
      assertThat(bit(denied, 5)).isFalse();
      assertThat(bit(denied, 10)).isFalse();
    }
  }

  @Nested
  class Watermark {

    private static final String WATERMARK = "WMARKTEXT";

    // The names of the fonts of a form, without the prefix of the embedded subsets (ABCDEF+).
    private TreeSet<String> fontNames(final PDFormXObject form) throws IOException {
      final TreeSet<String> names = new TreeSet<>();
      for (final COSName name : form.getResources().getFontNames()) {
        names.add(form.getResources().getFont(name).getName().replaceFirst("^[A-Z]{6}\\+", ""));
      }
      return names;
    }

    @Test
    void text_ShouldPrintTheWatermark(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File with =
          writer(converter, testFolder, b -> b.tagged(false).watermark(w -> w.text(WATERMARK)));
      final File without = writer(converter, testFolder, b -> b.tagged(false));

      // The letters of the watermark are drawn one by one.
      assertThat(text(with).replaceAll("\\s+", "")).contains(WATERMARK);
      assertThat(text(without).replaceAll("\\s+", "")).doesNotContain(WATERMARK);
    }

    @Test
    void tiledText_ShouldRepeatTheWatermark(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File single =
          writer(converter, testFolder, b -> b.tagged(false).watermark(w -> w.text(WATERMARK)));
      final File tiled =
          writer(
              converter, testFolder, b -> b.tagged(false).watermark(w -> w.tiledText(WATERMARK)));

      // Each watermark is drawn by a form: the tiled one needs many more of them.
      try (PDDocument docSingle = Loader.loadPDF(single);
          PDDocument docTiled = Loader.loadPDF(tiled)) {
        assertThat(textFormCount(docTiled)).isGreaterThan(textFormCount(docSingle) * 5);
      }
    }

    private long textFormCount(final PDDocument doc) throws IOException {
      long count = 0;
      for (final PDFormXObject form : forms(doc)) {
        if (content(form).contains(" Tf")) {
          count++;
        }
      }
      return count;
    }

    @Test
    void colorFontHeightAndRotation_ShouldStyleTheWatermark(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File plain =
          writer(converter, testFolder, b -> b.tagged(false).watermark(w -> w.text(WATERMARK)));
      final File styled =
          writer(
              converter,
              testFolder,
              b ->
                  b.tagged(false)
                      .watermark(
                          w -> w.text(WATERMARK).color(0xFF0000).fontHeight(20).rotation(90)));

      final String color = "([\\d.]+ [\\d.]+ [\\d.]+) rg";
      final String size = "/F\\d+ ([\\d.]+) Tf";
      final String matrix = "([-\\d.]+ [-\\d.]+ [-\\d.]+ [-\\d.]+) [-\\d.]+ [-\\d.]+ Tm";
      try (PDDocument docPlain = Loader.loadPDF(plain);
          PDDocument docStyled = Loader.loadPDF(styled)) {
        final String plainContent = content(watermark(docPlain));
        final String styledContent = content(watermark(docStyled));
        assertThat(firstGroup(color, styledContent)).isEqualTo("1 0 0");
        assertThat(firstGroup(color, plainContent)).isNotEqualTo("1 0 0");
        assertThat(firstGroup(size, styledContent)).isEqualTo("20");
        assertThat(firstGroup(size, plainContent)).isNotEqualTo("20");
        assertThat(firstGroup(matrix, styledContent))
            .isNotEqualTo(firstGroup(matrix, plainContent));
      }
    }

    @Test
    void fontName_ShouldChangeTheFontOfTheWatermark(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File plain =
          writer(converter, testFolder, b -> b.tagged(false).watermark(w -> w.text(WATERMARK)));
      final File mono =
          writer(
              converter,
              testFolder,
              b -> b.tagged(false).watermark(w -> w.text(WATERMARK).fontName("Liberation Mono")));

      try (PDDocument docPlain = Loader.loadPDF(plain);
          PDDocument docMono = Loader.loadPDF(mono)) {
        assertThat(fontNames(watermark(docMono))).isNotEqualTo(fontNames(watermark(docPlain)));
        assertThat(fontNames(watermark(docMono))).anyMatch(name -> name.contains("Mono"));
      }
    }
  }

  @Nested
  class Presentation {

    private File impress(
        final DocumentConverter converter,
        final File testFolder,
        final Consumer<PdfOptions.Builder> config)
        throws OfficeException {
      return convert(converter, testFolder, IMPRESS_FILE, config);
    }

    @Test
    void hiddenSlides_ShouldExportTheHiddenSlide(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on =
          impress(converter, testFolder, b -> b.presentation(p -> p.hiddenSlides(true)));
      final File off =
          impress(converter, testFolder, b -> b.presentation(p -> p.hiddenSlides(false)));

      assertThat(pageCount(on)).isEqualTo(3);
      assertThat(text(on)).contains("SLIDEHIDDEN");
      assertThat(pageCount(off)).isEqualTo(2);
      assertThat(text(off)).doesNotContain("SLIDEHIDDEN");
    }

    @Test
    void notesPages_ShouldExportTheNotesAfterTheSlides(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on = impress(converter, testFolder, b -> b.presentation(p -> p.notesPages(true)));
      final File off =
          impress(converter, testFolder, b -> b.presentation(p -> p.notesPages(false)));
      final File only =
          impress(
              converter,
              testFolder,
              b -> b.presentation(p -> p.notesPages(true).onlyNotesPages(true)));

      // Two visible slides.
      assertThat(pageCount(off)).isEqualTo(2);
      assertThat(text(off)).doesNotContain("NOTESONE");
      assertThat(pageCount(on)).isEqualTo(4);
      assertThat(text(on)).contains("NOTESONE");
      assertThat(pageCount(only)).isEqualTo(2);
      assertThat(text(only)).contains("NOTESONE");
    }

    @Test
    void transitions_ShouldExportTheSlideTransitions(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on = impress(converter, testFolder, b -> b.presentation(p -> p.transitions(true)));
      final File off =
          impress(converter, testFolder, b -> b.presentation(p -> p.transitions(false)));

      try (PDDocument docOn = Loader.loadPDF(on);
          PDDocument docOff = Loader.loadPDF(off)) {
        assertThat(docOn.getPage(0).getCOSObject().containsKey("Trans")).isTrue();
        assertThat(docOff.getPage(0).getCOSObject().containsKey("Trans")).isFalse();
      }
    }
  }

  @Nested
  class Spreadsheet {

    private File calc(
        final DocumentConverter converter,
        final File testFolder,
        final Consumer<PdfOptions.Builder> config)
        throws OfficeException {
      return convert(converter, testFolder, CALC_FILE, config);
    }

    @Test
    void singlePageSheets_ShouldExportOnePagePerSheet(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File on =
          calc(converter, testFolder, b -> b.spreadsheet(s -> s.singlePageSheets(true)));
      final File off =
          calc(converter, testFolder, b -> b.spreadsheet(s -> s.singlePageSheets(false)));

      // Two sheets.
      assertThat(pageCount(on)).isEqualTo(2);
      assertThat(pageCount(off)).isGreaterThan(2);
    }

    @Test
    void sheetRange_ShouldExportOnlyThoseSheets(
        final @TempDir File testFolder,
        final DocumentConverter converter,
        final OfficeManager manager)
        throws IOException, OfficeException {

      assumeLibreOffice(manager, 24, 8);

      final File first = calc(converter, testFolder, b -> b.spreadsheet(s -> s.sheetRange("1")));
      final File second = calc(converter, testFolder, b -> b.spreadsheet(s -> s.sheetRange("2")));

      assertThat(text(first)).contains("AlphaR1").doesNotContain("BetaR1");
      assertThat(text(second)).contains("BetaR1").doesNotContain("AlphaR1");
    }
  }

  @Nested
  class Presets {

    @Test
    void archive_ShouldProduceTaggedPdfA2WithBookmarksAndLosslessImages(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File pdf = new File(testFolder, "archive.pdf");
      converter.convert(WRITER_FILE).to(pdf).with(PdfOptions.archive()).execute();

      assertThat(xmp(pdf)).containsPattern("<pdfaid:part>\\s*2\\s*</pdfaid:part>");
      assertThat(isTagged(pdf)).isTrue();
      assertThat(catalogHas(pdf, "Outlines")).isTrue();
      try (PDDocument doc = Loader.loadPDF(pdf)) {
        assertThat(image(doc).getSuffix()).isEqualTo("png");
      }
    }

    @Test
    void accessible_ShouldProduceTaggedPdfUaWithBookmarks(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File pdf = new File(testFolder, "accessible.pdf");
      converter.convert(WRITER_FILE).to(pdf).with(PdfOptions.accessible()).execute();

      assertThat(xmp(pdf)).contains("pdfuaid:part");
      assertThat(isTagged(pdf)).isTrue();
      assertThat(catalogHas(pdf, "Outlines")).isTrue();
    }

    @Test
    void compact_ShouldProduceTaggedPdfWithReducedJpegImages(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      final File pdf = new File(testFolder, "compact.pdf");
      converter.convert(WRITER_FILE).to(pdf).with(PdfOptions.compact()).execute();

      assertThat(isTagged(pdf)).isTrue();
      try (PDDocument doc = Loader.loadPDF(pdf)) {
        assertThat(image(doc).getSuffix()).isEqualTo("jpg");
        assertThat(image(doc).getWidth()).isEqualTo(150);
      }
    }
  }

  @Nested
  class Conversion {

    @Test
    void withoutOptionSet_ShouldConvertLikeWithoutOptions(
        final @TempDir File testFolder, final DocumentConverter converter)
        throws IOException, OfficeException {

      // Without FilterData, the office uses the settings of its configuration. Empty options
      // must not change that.
      final File plain = new File(testFolder, "plain.pdf");
      converter.convert(WRITER_FILE).to(plain).execute();
      final File empty = writer(converter, testFolder, b -> {});

      assertThat(isTagged(empty)).isEqualTo(isTagged(plain));
      assertThat(catalogName(empty, "PageMode")).isEqualTo(catalogName(plain, "PageMode"));
      assertThat(pageCount(empty)).isEqualTo(pageCount(plain));
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
      final File converterOnly = new File(testFolder, "converter.pdf");
      final File withOptions = new File(testFolder, "options.pdf");

      converter.convert(WRITER_FILE).to(converterOnly).execute();
      converter
          .convert(WRITER_FILE)
          .to(withOptions)
          .with(PdfOptions.builder().version(PdfVersion.PDF_1_5).build())
          .execute();

      assertThat(header(converterOnly)).isEqualTo("%PDF-1.6");
      assertThat(pageCount(converterOnly)).isEqualTo(1);

      // The version of the options wins, and the page range of the converter is kept.
      assertThat(header(withOptions)).isEqualTo("%PDF-1.5");
      assertThat(pageCount(withOptions)).isEqualTo(1);
    }

    @Test
    void toOutputStream_ShouldApplyTheOptions(final DocumentConverter converter)
        throws OfficeException {

      final ByteArrayOutputStream output = new ByteArrayOutputStream();

      converter
          .convert(WRITER_FILE)
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
                  converter
                      .convert(WRITER_FILE)
                      .to(outputFile)
                      .with(PdfOptions.archive())
                      .execute())
          .withMessage("PdfOptions cannot be applied to a target document of format 'odt'");
    }
  }
}
