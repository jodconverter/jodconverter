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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.jodconverter.core.pdf.PdfFormOptions.SubmitFormat;
import org.jodconverter.core.pdf.PdfInitialViewOptions.Magnification;
import org.jodconverter.core.pdf.PdfInitialViewOptions.PageLayout;
import org.jodconverter.core.pdf.PdfInitialViewOptions.Pane;
import org.jodconverter.core.pdf.PdfLinkOptions.LinkTarget;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.core.pdf.PdfSecurityOptions.Changes;
import org.jodconverter.core.pdf.PdfSecurityOptions.Printing;
import org.jodconverter.core.pdf.PdfVersion;

/**
 * Builds the {@link PdfOptions} of the command line, from the {@code --pdf-preset} and {@code
 * --pdf-option name=value} arguments.
 */
final class PdfOptionsParser {

  private static final String FILTER_DATA_PREFIX = "filter-data.";
  private static final String CERTIFICATE_FILE = "signature.certificate-file";
  private static final String PRIVATE_KEY_FILE = "signature.private-key-file";

  private static final Map<String, BiConsumer<PdfOptions.Builder, String>> OPTIONS = initOptions();

  // Suppresses default constructor, ensuring non-instantiability.
  private PdfOptionsParser() {
    throw new AssertionError("Utility class must not be instantiated");
  }

  /**
   * Gets the names accepted by the {@code --pdf-option} argument.
   *
   * @return The option names, in the order of their groups.
   */
  /* default */ static Set<String> getOptionNames() {
    return OPTIONS.keySet();
  }

  /**
   * Builds the PDF options described by the given arguments.
   *
   * @param preset The name of a preset to start from (archive, accessible or compact), or null.
   * @param options The options, each as "name=value", or null. They are applied in order, on top of
   *     the preset.
   * @return The PDF options, or null if there is neither a preset nor an option.
   * @throws IllegalArgumentException If a name or a value is not valid, or if the options cannot be
   *     used together.
   */
  /* default */ static PdfOptions parse(final String preset, final String... options) {

    if (preset == null && (options == null || options.length == 0)) {
      return null;
    }

    final PdfOptions.Builder builder = preset == null ? PdfOptions.builder() : preset(preset);
    String certificateFile = null;
    String privateKeyFile = null;
    if (options != null) {
      for (final String option : options) {
        final int separator = option.indexOf('=');
        if (separator <= 0) {
          throw new IllegalArgumentException(
              "Invalid PDF option '" + option + "'; expected name=value");
        }
        final String name = option.substring(0, separator).trim();
        final String value = option.substring(separator + 1);
        if (CERTIFICATE_FILE.equals(name)) {
          certificateFile = value;
        } else if (PRIVATE_KEY_FILE.equals(name)) {
          privateKeyFile = value;
        } else {
          apply(builder, name, value);
        }
      }
    }
    applyCertificate(builder, certificateFile, privateKeyFile);

    return builder.build();
  }

  private static PdfOptions.Builder preset(final String preset) {
    return switch (normalize(preset)) {
      case "ARCHIVE" -> PdfOptions.archive().toBuilder();
      case "ACCESSIBLE" -> PdfOptions.accessible().toBuilder();
      case "COMPACT" -> PdfOptions.compact().toBuilder();
      default ->
          throw new IllegalArgumentException(
              "Unknown PDF preset '" + preset + "'; expected archive, accessible or compact");
    };
  }

  private static void apply(
      final PdfOptions.Builder builder, final String name, final String value) {

    if (name.startsWith(FILTER_DATA_PREFIX) && name.length() > FILTER_DATA_PREFIX.length()) {
      builder.filterData(name.substring(FILTER_DATA_PREFIX.length()), toFilterDataValue(value));
      return;
    }

    final BiConsumer<PdfOptions.Builder, String> setter = OPTIONS.get(name);
    if (setter == null) {
      throw new IllegalArgumentException(
          "Unknown PDF option '"
              + name
              + "'; expected one of: "
              + String.join(", ", OPTIONS.keySet()));
    }
    try {
      setter.accept(builder, value);
    } catch (IllegalArgumentException | NullPointerException | UncheckedIOException ex) {
      throw new IllegalArgumentException(
          "Invalid value '" + value + "' for the PDF option '" + name + "': " + ex.getMessage(),
          ex);
    }
  }

  // The certificate and its private key are two options, but must be set together.
  private static void applyCertificate(
      final PdfOptions.Builder builder, final String certificateFile, final String privateKeyFile) {

    if (certificateFile == null && privateKeyFile == null) {
      return;
    }
    if (certificateFile == null || privateKeyFile == null) {
      throw new IllegalArgumentException(
          "The PDF options '"
              + CERTIFICATE_FILE
              + "' and '"
              + PRIVATE_KEY_FILE
              + "' must be used together");
    }
    try {
      final String certificate = readFile(certificateFile);
      final String privateKey = readFile(privateKeyFile);
      builder.signature(signature -> signature.certificatePem(certificate, privateKey));
    } catch (UncheckedIOException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    }
  }

  private static String readFile(final String path) {
    try {
      return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new UncheckedIOException("Could not read the file '" + path + "'", ex);
    }
  }

  // Same conversion as the store properties: boolean, integer, or text.
  private static Object toFilterDataValue(final String value) {
    if ("true".equalsIgnoreCase(value)) {
      return Boolean.TRUE;
    }
    if ("false".equalsIgnoreCase(value)) {
      return Boolean.FALSE;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      return value;
    }
  }

  private static String normalize(final String value) {
    return value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace('.', '_');
  }

  private static boolean bool(final String value) {
    if ("true".equalsIgnoreCase(value)) {
      return true;
    }
    if ("false".equalsIgnoreCase(value)) {
      return false;
    }
    throw new IllegalArgumentException("expected true or false");
  }

  private static int integer(final String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("expected a number", ex);
    }
  }

  // An RGB color, written as RRGGBB, #RRGGBB or 0xRRGGBB.
  private static int color(final String value) {
    final String hex = value.trim().replaceFirst("^(#|0[xX])", "");
    try {
      return Integer.parseInt(hex, 16);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("expected a color such as FF0000", ex);
    }
  }

  // An enum constant, written in any case, with hyphens or underscores (pdf-a-2b, PDF_A_2B).
  private static <E extends Enum<E>> E enumValue(final Class<E> type, final String value) {
    final String normalized = normalize(value);
    for (final E constant : type.getEnumConstants()) {
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
  private static PdfVersion version(final String value) {
    final String normalized = normalize(value);
    return enumValue(
        PdfVersion.class,
        normalized.startsWith("PDF") || "DEFAULT".equals(normalized)
            ? normalized
            : "PDF_" + normalized);
  }

  @SuppressWarnings("PMD.NcssCount")
  private static Map<String, BiConsumer<PdfOptions.Builder, String>> initOptions() {

    final Map<String, BiConsumer<PdfOptions.Builder, String>> map = new LinkedHashMap<>();

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
    map.put("watermark.color", (b, v) -> b.watermark(o -> o.color(color(v))));
    map.put("watermark.font-name", (b, v) -> b.watermark(o -> o.fontName(v)));
    map.put("watermark.font-height", (b, v) -> b.watermark(o -> o.fontHeight(integer(v))));
    map.put("watermark.rotation", (b, v) -> b.watermark(o -> o.rotation(integer(v))));

    // Digital signature. The certificate file and its private key file are handled by parse().
    map.put(
        "signature.certificate-subject-name",
        (b, v) -> b.signature(o -> o.certificateSubjectName(v)));
    map.put(CERTIFICATE_FILE, null);
    map.put(PRIVATE_KEY_FILE, null);
    map.put("signature.ca-file", (b, v) -> b.signature(o -> o.caPem(readFile(v))));
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
