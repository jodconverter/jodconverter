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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.jodconverter.core.pdf.PdfFormOptions.SubmitFormat;
import org.jodconverter.core.pdf.PdfInitialViewOptions.Magnification;
import org.jodconverter.core.pdf.PdfInitialViewOptions.PageLayout;
import org.jodconverter.core.pdf.PdfInitialViewOptions.Pane;
import org.jodconverter.core.pdf.PdfLinkOptions.LinkTarget;
import org.jodconverter.core.pdf.PdfSecurityOptions.Changes;
import org.jodconverter.core.pdf.PdfSecurityOptions.Printing;

/**
 * The PDF options by name, as written on a command line or in a configuration file ({@code
 * images.jpeg-quality}, {@code security.printing}...), with the conversion of their text values.
 * Shared by the command line tool and the Spring Boot starter through {@link
 * PdfOptions.Builder#option(String, String)}.
 */
final class PdfOptionNames {

  /* default */ static final String CERTIFICATE = "signature.certificate";
  /* default */ static final String PRIVATE_KEY = "signature.private-key";

  private static final Map<String, BiConsumer<PdfOptions.Builder, String>> OPTIONS = initOptions();

  // Suppresses default constructor, ensuring non-instantiability.
  private PdfOptionNames() {
    throw new AssertionError("Utility class must not be instantiated");
  }

  /* default */ static Map<String, BiConsumer<PdfOptions.Builder, String>> options() {
    return OPTIONS;
  }

  /**
   * Applies a named option to a builder.
   *
   * @param builder The builder.
   * @param name The name of the option.
   * @param value The value of the option, as text.
   * @throws IllegalArgumentException If the name or the value is not valid.
   */
  /* default */ static void apply(
      final PdfOptions.Builder builder, final String name, final String value) {

    final var setter = OPTIONS.get(name);
    if (setter == null) {
      throw new IllegalArgumentException(
          "Unknown PDF option '"
              + name
              + "'; expected one of: "
              + String.join(", ", OPTIONS.keySet()));
    }
    try {
      setter.accept(builder, value);
    } catch (IllegalArgumentException | NullPointerException ex) {
      throw new IllegalArgumentException(
          "Invalid value '" + value + "' for the PDF option '" + name + "': " + ex.getMessage(),
          ex);
    }
  }

  private static String normalize(final String value) {
    return value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace('.', '_');
  }

  /* default */ static boolean bool(final String value) {
    return switch (value.toLowerCase(Locale.ROOT)) {
      case "true" -> true;
      case "false" -> false;
      default -> throw new IllegalArgumentException("expected true or false");
    };
  }

  /* default */ static int integer(final String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("expected a number", ex);
    }
  }

  // An enum constant, written in any case, with hyphens or underscores (pdf-a-2b, PDF_A_2B).
  /* default */ static <E extends Enum<E>> E enumValue(final Class<E> type, final String value) {
    final var normalized = normalize(value);
    for (final var constant : type.getEnumConstants()) {
      if (constant.name().equals(normalized)) {
        return constant;
      }
    }
    throw new IllegalArgumentException(
        "expected one of: "
            + Arrays.stream(type.getEnumConstants())
                .map(constant -> constant.name().toLowerCase(Locale.ROOT).replace('_', '-'))
                .collect(Collectors.joining(", ")));
  }

  // The PDF version, also accepted as "1.7", "2.0", "a-1b"...
  /* default */ static PdfVersion version(final String value) {
    final var normalized = normalize(value);
    return enumValue(
        PdfVersion.class,
        normalized.startsWith("PDF") || "DEFAULT".equals(normalized)
            ? normalized
            : "PDF_" + normalized);
  }

  private static Map<String, BiConsumer<PdfOptions.Builder, String>> initOptions() {

    final var map = new LinkedHashMap<String, BiConsumer<PdfOptions.Builder, String>>();

    // General
    map.put("version", (b, v) -> b.version(version(v)));
    map.put("pdf-ua", (b, v) -> b.pdfUa(bool(v)));
    map.put("tagged", (b, v) -> b.tagged(bool(v)));
    map.put("embed-source-document", (b, v) -> b.embedSourceDocument(bool(v)));
    map.put("reference-xobjects", (b, v) -> b.referenceXObjects(bool(v)));

    // Images
    map.put("images.lossless", (b, v) -> b.images(o -> o.lossless(bool(v))));
    map.put("images.jpeg-quality", (b, v) -> b.images(o -> o.jpegQuality(integer(v))));
    map.put("images.reduce-resolution", (b, v) -> b.images(o -> o.reduceResolution(bool(v))));
    map.put("images.max-resolution", (b, v) -> b.images(o -> o.maxResolution(integer(v))));

    // Pages
    map.put("pages.range", (b, v) -> b.pages(o -> o.range(v)));
    map.put("pages.skip-empty-pages", (b, v) -> b.pages(o -> o.skipEmptyPages(bool(v))));
    map.put("pages.placeholders", (b, v) -> b.pages(o -> o.placeholders(bool(v))));
    map.put("pages.tracked-changes", (b, v) -> b.pages(o -> o.trackedChanges(bool(v))));

    // Comments
    map.put("comments.as-pdf-annotations", (b, v) -> b.comments(o -> o.asPdfAnnotations(bool(v))));
    map.put("comments.in-margin", (b, v) -> b.comments(o -> o.inMargin(bool(v))));

    // Bookmarks
    map.put("bookmarks.export", (b, v) -> b.bookmarks(o -> o.export(bool(v))));
    map.put("bookmarks.open-levels", (b, v) -> b.bookmarks(o -> o.openLevels(integer(v))));
    map.put(
        "bookmarks.as-named-destinations",
        (b, v) -> b.bookmarks(o -> o.asNamedDestinations(bool(v))));

    // Forms
    map.put("forms.export", (b, v) -> b.forms(o -> o.export(bool(v))));
    map.put(
        "forms.submit-format",
        (b, v) -> b.forms(o -> o.submitFormat(enumValue(SubmitFormat.class, v))));
    map.put("forms.allow-duplicate-names", (b, v) -> b.forms(o -> o.allowDuplicateNames(bool(v))));

    // Links
    map.put("links.relative-file-links", (b, v) -> b.links(o -> o.relativeFileLinks(bool(v))));
    map.put(
        "links.convert-odf-targets-to-pdf",
        (b, v) -> b.links(o -> o.convertOdfTargetsToPdf(bool(v))));
    map.put(
        "links.cross-document-links",
        (b, v) -> b.links(o -> o.crossDocumentLinks(enumValue(LinkTarget.class, v))));

    // Initial view
    map.put("initial-view.pane", (b, v) -> b.initialView(o -> o.pane(enumValue(Pane.class, v))));
    map.put("initial-view.page", (b, v) -> b.initialView(o -> o.page(integer(v))));
    map.put(
        "initial-view.magnification",
        (b, v) -> b.initialView(o -> o.magnification(enumValue(Magnification.class, v))));
    map.put("initial-view.zoom", (b, v) -> b.initialView(o -> o.zoom(integer(v))));
    map.put(
        "initial-view.layout",
        (b, v) -> b.initialView(o -> o.layout(enumValue(PageLayout.class, v))));

    // Viewer window
    map.put(
        "viewer.resize-to-initial-page", (b, v) -> b.viewer(o -> o.resizeToInitialPage(bool(v))));
    map.put("viewer.center-window", (b, v) -> b.viewer(o -> o.centerWindow(bool(v))));
    map.put("viewer.full-screen", (b, v) -> b.viewer(o -> o.fullScreen(bool(v))));
    map.put(
        "viewer.display-document-title", (b, v) -> b.viewer(o -> o.displayDocumentTitle(bool(v))));
    map.put("viewer.hide-menubar", (b, v) -> b.viewer(o -> o.hideMenubar(bool(v))));
    map.put("viewer.hide-toolbar", (b, v) -> b.viewer(o -> o.hideToolbar(bool(v))));
    map.put("viewer.hide-window-controls", (b, v) -> b.viewer(o -> o.hideWindowControls(bool(v))));

    // Security
    map.put("security.open-password", (b, v) -> b.security(o -> o.openPassword(v)));
    map.put("security.permission-password", (b, v) -> b.security(o -> o.permissionPassword(v)));
    map.put(
        "security.printing", (b, v) -> b.security(o -> o.printing(enumValue(Printing.class, v))));
    map.put("security.changes", (b, v) -> b.security(o -> o.changes(enumValue(Changes.class, v))));
    map.put("security.copying", (b, v) -> b.security(o -> o.copying(bool(v))));
    map.put(
        "security.accessibility-access", (b, v) -> b.security(o -> o.accessibilityAccess(bool(v))));

    // Watermark
    map.put("watermark.text", (b, v) -> b.watermark(o -> o.text(v)));
    map.put("watermark.tiled-text", (b, v) -> b.watermark(o -> o.tiledText(v)));
    map.put("watermark.color", (b, v) -> b.watermark(o -> o.color(v)));
    map.put("watermark.font-name", (b, v) -> b.watermark(o -> o.fontName(v)));
    map.put("watermark.font-height", (b, v) -> b.watermark(o -> o.fontHeight(integer(v))));
    map.put("watermark.rotation", (b, v) -> b.watermark(o -> o.rotation(integer(v))));

    // Digital signature. The certificate and its private key, as PEM text, are kept by the builder
    // until it is built, since they must be set together.
    map.put(
        "signature.certificate-subject-name",
        (b, v) -> b.signature(o -> o.certificateSubjectName(v)));
    map.put(CERTIFICATE, PdfOptions.Builder::pendingCertificatePem);
    map.put(PRIVATE_KEY, PdfOptions.Builder::pendingPrivateKeyPem);
    map.put("signature.ca", (b, v) -> b.signature(o -> o.caPem(v)));
    map.put("signature.password", (b, v) -> b.signature(o -> o.password(v)));
    map.put("signature.location", (b, v) -> b.signature(o -> o.location(v)));
    map.put("signature.reason", (b, v) -> b.signature(o -> o.reason(v)));
    map.put("signature.contact-info", (b, v) -> b.signature(o -> o.contactInfo(v)));
    map.put("signature.timestamp-authority", (b, v) -> b.signature(o -> o.timestampAuthority(v)));

    // Presentation
    map.put("presentation.hidden-slides", (b, v) -> b.presentation(o -> o.hiddenSlides(bool(v))));
    map.put("presentation.notes-pages", (b, v) -> b.presentation(o -> o.notesPages(bool(v))));
    map.put(
        "presentation.only-notes-pages", (b, v) -> b.presentation(o -> o.onlyNotesPages(bool(v))));
    map.put("presentation.transitions", (b, v) -> b.presentation(o -> o.transitions(bool(v))));

    // Spreadsheet
    map.put(
        "spreadsheet.single-page-sheets",
        (b, v) -> b.spreadsheet(o -> o.singlePageSheets(bool(v))));
    map.put("spreadsheet.sheet-range", (b, v) -> b.spreadsheet(o -> o.sheetRange(v)));

    return map;
  }
}
