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

package org.jodconverter.boot.autoconfigure;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

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
 * Configuration class for the PDF options that the auto-configured converters apply to all their
 * conversions to PDF.
 *
 * @see PdfOptions
 */
@ConfigurationProperties("jodconverter.pdf")
public class JodConverterPdfProperties {

  /** The PDF options to start from. */
  public enum Preset {
    /** PDF/A-2b, tagged, with bookmarks and images compressed without loss. */
    ARCHIVE,

    /** PDF/UA, tagged, with bookmarks. */
    ACCESSIBLE,

    /** Tagged, with images compressed as JPEG and reduced to 150 DPI. */
    COMPACT
  }

  /** PDF options to start from. The other properties are applied on top of them. */
  private Preset preset;

  /** Version of the PDF specification, or PDF/A conformance, of the documents. */
  private PdfVersion version;

  /**
   * Whether the documents comply with PDF/UA (universal accessibility). Requires LibreOffice 7.0 or
   * later.
   */
  private Boolean pdfUa;

  /**
   * Whether the documents are tagged PDF, which contain the structure of the document for screen
   * readers and text extraction.
   */
  private Boolean tagged;

  /** Whether the source document is embedded in the PDF document (hybrid PDF). */
  private Boolean embedSourceDocument;

  /**
   * Whether the PDF images are exported as reference XObjects. Requires LibreOffice 5.4 or later.
   */
  private Boolean referenceXObjects;

  /** Options for the images. */
  private final Images images = new Images();

  /** Options for the pages and the content to export. */
  private final Pages pages = new Pages();

  /** Options for the comments of the source documents. */
  private final Comments comments = new Comments();

  /** Options for the bookmarks (outline). */
  private final Bookmarks bookmarks = new Bookmarks();

  /** Options for the form fields. */
  private final Forms forms = new Forms();

  /** Options for the hyperlinks. */
  private final Links links = new Links();

  /** Options for the way a document is displayed when it is opened. */
  private final InitialView initialView = new InitialView();

  /** Options for the window of the PDF viewer. */
  private final Viewer viewer = new Viewer();

  /** Options for the encryption and the permissions. Not allowed with a PDF/A version. */
  private final Security security = new Security();

  /** Options for a text watermark printed on each page. */
  private final Watermark watermark = new Watermark();

  /** Options to sign the documents digitally. */
  private final Signature signature = new Signature();

  /** Options that only apply to presentations. */
  private final Presentation presentation = new Presentation();

  /** Options that only apply to spreadsheets. */
  private final Spreadsheet spreadsheet = new Spreadsheet();

  /**
   * Any other property of the FilterData of the PDF export filter, by name. A value true or false
   * is sent as a boolean, a whole number as an integer.
   */
  private Map<String, String> filterData = new LinkedHashMap<>();

  /**
   * Creates the PDF options described by these properties.
   *
   * @return The PDF options.
   * @throws IllegalArgumentException If a value is not valid, or if the options cannot be used
   *     together.
   */
  public @NonNull PdfOptions toPdfOptions() {

    final var builder = newBuilder();

    if (version != null) {
      builder.version(version);
    }
    if (pdfUa != null) {
      builder.pdfUa(pdfUa);
    }
    if (tagged != null) {
      builder.tagged(tagged);
    }
    if (embedSourceDocument != null) {
      builder.embedSourceDocument(embedSourceDocument);
    }
    if (referenceXObjects != null) {
      builder.referenceXObjects(referenceXObjects);
    }
    images.applyTo(builder);
    pages.applyTo(builder);
    comments.applyTo(builder);
    bookmarks.applyTo(builder);
    forms.applyTo(builder);
    links.applyTo(builder);
    initialView.applyTo(builder);
    viewer.applyTo(builder);
    security.applyTo(builder);
    watermark.applyTo(builder);
    signature.applyTo(builder);
    presentation.applyTo(builder);
    spreadsheet.applyTo(builder);
    filterData.forEach((name, value) -> builder.filterData(name, toFilterDataValue(value)));

    return builder.build();
  }

  // Creates a builder that starts from the preset, if there is one.
  private PdfOptions.Builder newBuilder() {
    if (preset == null) {
      return PdfOptions.builder();
    }
    return switch (preset) {
      case ARCHIVE -> PdfOptions.archive().toBuilder();
      case ACCESSIBLE -> PdfOptions.accessible().toBuilder();
      case COMPACT -> PdfOptions.compact().toBuilder();
    };
  }

  // Same conversion as the command line tool: boolean, integer, or text.
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

  // An RGB color, written as RRGGBB, #RRGGBB or 0xRRGGBB.
  private static int parseColor(final String value) {
    try {
      return Integer.parseInt(value.trim().replaceFirst("^(#|0[xX])", ""), 16);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(
          "Invalid watermark color '" + value + "'; expected an RGB value such as FF0000", ex);
    }
  }

  private static String read(final Resource resource) {
    try (var in = resource.getInputStream()) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new UncheckedIOException("Could not read " + resource.getDescription(), ex);
    }
  }

  public @Nullable Preset getPreset() {
    return preset;
  }

  public void setPreset(final @Nullable Preset preset) {
    this.preset = preset;
  }

  public @Nullable PdfVersion getVersion() {
    return version;
  }

  public void setVersion(final @Nullable PdfVersion version) {
    this.version = version;
  }

  public @Nullable Boolean getPdfUa() {
    return pdfUa;
  }

  public void setPdfUa(final @Nullable Boolean pdfUa) {
    this.pdfUa = pdfUa;
  }

  public @Nullable Boolean getTagged() {
    return tagged;
  }

  public void setTagged(final @Nullable Boolean tagged) {
    this.tagged = tagged;
  }

  public @Nullable Boolean getEmbedSourceDocument() {
    return embedSourceDocument;
  }

  public void setEmbedSourceDocument(final @Nullable Boolean embedSourceDocument) {
    this.embedSourceDocument = embedSourceDocument;
  }

  public @Nullable Boolean getReferenceXObjects() {
    return referenceXObjects;
  }

  public void setReferenceXObjects(final @Nullable Boolean referenceXObjects) {
    this.referenceXObjects = referenceXObjects;
  }

  public @NonNull Images getImages() {
    return images;
  }

  public @NonNull Pages getPages() {
    return pages;
  }

  public @NonNull Comments getComments() {
    return comments;
  }

  public @NonNull Bookmarks getBookmarks() {
    return bookmarks;
  }

  public @NonNull Forms getForms() {
    return forms;
  }

  public @NonNull Links getLinks() {
    return links;
  }

  public @NonNull InitialView getInitialView() {
    return initialView;
  }

  public @NonNull Viewer getViewer() {
    return viewer;
  }

  public @NonNull Security getSecurity() {
    return security;
  }

  public @NonNull Watermark getWatermark() {
    return watermark;
  }

  public @NonNull Signature getSignature() {
    return signature;
  }

  public @NonNull Presentation getPresentation() {
    return presentation;
  }

  public @NonNull Spreadsheet getSpreadsheet() {
    return spreadsheet;
  }

  public @NonNull Map<@NonNull String, @NonNull String> getFilterData() {
    return filterData;
  }

  public void setFilterData(final @NonNull Map<@NonNull String, @NonNull String> filterData) {
    this.filterData = filterData;
  }

  /** Options for the images. */
  public static class Images {

    /** Whether the images are compressed without loss instead of as JPEG. */
    private Boolean lossless;

    /** Quality of the JPEG compression, from 1 to 100. */
    private Integer jpegQuality;

    /** Whether the resolution of the images is reduced to the maximum resolution. */
    private Boolean reduceResolution;

    /** Maximum resolution of the images, in DPI. Also reduces the resolution of the images. */
    private Integer maxResolution;

    private void applyTo(final PdfOptions.Builder builder) {
      if (lossless != null) {
        builder.images(o -> o.lossless(lossless));
      }
      if (jpegQuality != null) {
        builder.images(o -> o.jpegQuality(jpegQuality));
      }
      if (reduceResolution != null) {
        builder.images(o -> o.reduceResolution(reduceResolution));
      }
      if (maxResolution != null) {
        builder.images(o -> o.maxResolution(maxResolution));
      }
    }

    public @Nullable Boolean getLossless() {
      return lossless;
    }

    public void setLossless(final @Nullable Boolean lossless) {
      this.lossless = lossless;
    }

    public @Nullable Integer getJpegQuality() {
      return jpegQuality;
    }

    public void setJpegQuality(final @Nullable Integer jpegQuality) {
      this.jpegQuality = jpegQuality;
    }

    public @Nullable Boolean getReduceResolution() {
      return reduceResolution;
    }

    public void setReduceResolution(final @Nullable Boolean reduceResolution) {
      this.reduceResolution = reduceResolution;
    }

    public @Nullable Integer getMaxResolution() {
      return maxResolution;
    }

    public void setMaxResolution(final @Nullable Integer maxResolution) {
      this.maxResolution = maxResolution;
    }
  }

  /** Options for the pages and the content to export. */
  public static class Pages {

    /** Pages to export, such as 1-3;7. All the pages when not set. */
    private String range;

    /** Whether the blank pages that Writer inserts automatically are left out. */
    private Boolean skipEmptyPages;

    /** Whether the placeholder fields are exported. Requires LibreOffice 5.1 or later. */
    private Boolean placeholders;

    /** Whether the tracked changes are shown. Requires LibreOffice 26.2 or later. */
    private Boolean trackedChanges;

    private void applyTo(final PdfOptions.Builder builder) {
      if (range != null) {
        builder.pages(o -> o.range(range));
      }
      if (skipEmptyPages != null) {
        builder.pages(o -> o.skipEmptyPages(skipEmptyPages));
      }
      if (placeholders != null) {
        builder.pages(o -> o.placeholders(placeholders));
      }
      if (trackedChanges != null) {
        builder.pages(o -> o.trackedChanges(trackedChanges));
      }
    }

    public @Nullable String getRange() {
      return range;
    }

    public void setRange(final @Nullable String range) {
      this.range = range;
    }

    public @Nullable Boolean getSkipEmptyPages() {
      return skipEmptyPages;
    }

    public void setSkipEmptyPages(final @Nullable Boolean skipEmptyPages) {
      this.skipEmptyPages = skipEmptyPages;
    }

    public @Nullable Boolean getPlaceholders() {
      return placeholders;
    }

    public void setPlaceholders(final @Nullable Boolean placeholders) {
      this.placeholders = placeholders;
    }

    public @Nullable Boolean getTrackedChanges() {
      return trackedChanges;
    }

    public void setTrackedChanges(final @Nullable Boolean trackedChanges) {
      this.trackedChanges = trackedChanges;
    }
  }

  /** Options for the comments of the source documents. */
  public static class Comments {

    /** Whether the comments are exported as PDF annotations. */
    private Boolean asPdfAnnotations;

    /** Whether the comments are exported in the page margin. Requires LibreOffice 7.5 or later. */
    private Boolean inMargin;

    private void applyTo(final PdfOptions.Builder builder) {
      if (asPdfAnnotations != null) {
        builder.comments(o -> o.asPdfAnnotations(asPdfAnnotations));
      }
      if (inMargin != null) {
        builder.comments(o -> o.inMargin(inMargin));
      }
    }

    public @Nullable Boolean getAsPdfAnnotations() {
      return asPdfAnnotations;
    }

    public void setAsPdfAnnotations(final @Nullable Boolean asPdfAnnotations) {
      this.asPdfAnnotations = asPdfAnnotations;
    }

    public @Nullable Boolean getInMargin() {
      return inMargin;
    }

    public void setInMargin(final @Nullable Boolean inMargin) {
      this.inMargin = inMargin;
    }
  }

  /** Options for the bookmarks (outline). */
  public static class Bookmarks {

    /** Whether the headings are exported as PDF bookmarks. */
    private Boolean export;

    /** Number of bookmark levels that are open when the document is opened, -1 for all. */
    private Integer openLevels;

    /** Whether the bookmarks of the source document are exported as PDF named destinations. */
    private Boolean asNamedDestinations;

    private void applyTo(final PdfOptions.Builder builder) {
      if (export != null) {
        builder.bookmarks(o -> o.export(export));
      }
      if (openLevels != null) {
        builder.bookmarks(o -> o.openLevels(openLevels));
      }
      if (asNamedDestinations != null) {
        builder.bookmarks(o -> o.asNamedDestinations(asNamedDestinations));
      }
    }

    public @Nullable Boolean getExport() {
      return export;
    }

    public void setExport(final @Nullable Boolean export) {
      this.export = export;
    }

    public @Nullable Integer getOpenLevels() {
      return openLevels;
    }

    public void setOpenLevels(final @Nullable Integer openLevels) {
      this.openLevels = openLevels;
    }

    public @Nullable Boolean getAsNamedDestinations() {
      return asNamedDestinations;
    }

    public void setAsNamedDestinations(final @Nullable Boolean asNamedDestinations) {
      this.asNamedDestinations = asNamedDestinations;
    }
  }

  /** Options for the form fields. */
  public static class Forms {

    /** Whether the form fields are exported as fillable PDF fields. */
    private Boolean export;

    /** Format in which the PDF form is submitted. */
    private SubmitFormat submitFormat;

    /** Whether several form fields may have the same name. */
    private Boolean allowDuplicateNames;

    private void applyTo(final PdfOptions.Builder builder) {
      if (export != null) {
        builder.forms(o -> o.export(export));
      }
      if (submitFormat != null) {
        builder.forms(o -> o.submitFormat(submitFormat));
      }
      if (allowDuplicateNames != null) {
        builder.forms(o -> o.allowDuplicateNames(allowDuplicateNames));
      }
    }

    public @Nullable Boolean getExport() {
      return export;
    }

    public void setExport(final @Nullable Boolean export) {
      this.export = export;
    }

    public @Nullable SubmitFormat getSubmitFormat() {
      return submitFormat;
    }

    public void setSubmitFormat(final @Nullable SubmitFormat submitFormat) {
      this.submitFormat = submitFormat;
    }

    public @Nullable Boolean getAllowDuplicateNames() {
      return allowDuplicateNames;
    }

    public void setAllowDuplicateNames(final @Nullable Boolean allowDuplicateNames) {
      this.allowDuplicateNames = allowDuplicateNames;
    }
  }

  /** Options for the hyperlinks. */
  public static class Links {

    /** Whether the links to files are exported relative to the location of the document. */
    private Boolean relativeFileLinks;

    /**
     * Whether the links to OpenDocument files are changed to target a file with the .pdf extension.
     */
    private Boolean convertOdfTargetsToPdf;

    /** How the links to other documents are opened. */
    private LinkTarget crossDocumentLinks;

    private void applyTo(final PdfOptions.Builder builder) {
      if (relativeFileLinks != null) {
        builder.links(o -> o.relativeFileLinks(relativeFileLinks));
      }
      if (convertOdfTargetsToPdf != null) {
        builder.links(o -> o.convertOdfTargetsToPdf(convertOdfTargetsToPdf));
      }
      if (crossDocumentLinks != null) {
        builder.links(o -> o.crossDocumentLinks(crossDocumentLinks));
      }
    }

    public @Nullable Boolean getRelativeFileLinks() {
      return relativeFileLinks;
    }

    public void setRelativeFileLinks(final @Nullable Boolean relativeFileLinks) {
      this.relativeFileLinks = relativeFileLinks;
    }

    public @Nullable Boolean getConvertOdfTargetsToPdf() {
      return convertOdfTargetsToPdf;
    }

    public void setConvertOdfTargetsToPdf(final @Nullable Boolean convertOdfTargetsToPdf) {
      this.convertOdfTargetsToPdf = convertOdfTargetsToPdf;
    }

    public @Nullable LinkTarget getCrossDocumentLinks() {
      return crossDocumentLinks;
    }

    public void setCrossDocumentLinks(final @Nullable LinkTarget crossDocumentLinks) {
      this.crossDocumentLinks = crossDocumentLinks;
    }
  }

  /** Options for the way a document is displayed when it is opened. */
  public static class InitialView {

    /** Pane shown beside the page. */
    private Pane pane;

    /** Page shown when the document is opened, starting at 1. */
    private Integer page;

    /** Magnification of the page. */
    private Magnification magnification;

    /** Zoom level of the page, in percent. Replaces the magnification. */
    private Integer zoom;

    /** Layout of the pages. */
    private PageLayout layout;

    private void applyTo(final PdfOptions.Builder builder) {
      if (pane != null) {
        builder.initialView(o -> o.pane(pane));
      }
      if (page != null) {
        builder.initialView(o -> o.page(page));
      }
      if (magnification != null) {
        builder.initialView(o -> o.magnification(magnification));
      }
      if (zoom != null) {
        builder.initialView(o -> o.zoom(zoom));
      }
      if (layout != null) {
        builder.initialView(o -> o.layout(layout));
      }
    }

    public @Nullable Pane getPane() {
      return pane;
    }

    public void setPane(final @Nullable Pane pane) {
      this.pane = pane;
    }

    public @Nullable Integer getPage() {
      return page;
    }

    public void setPage(final @Nullable Integer page) {
      this.page = page;
    }

    public @Nullable Magnification getMagnification() {
      return magnification;
    }

    public void setMagnification(final @Nullable Magnification magnification) {
      this.magnification = magnification;
    }

    public @Nullable Integer getZoom() {
      return zoom;
    }

    public void setZoom(final @Nullable Integer zoom) {
      this.zoom = zoom;
    }

    public @Nullable PageLayout getLayout() {
      return layout;
    }

    public void setLayout(final @Nullable PageLayout layout) {
      this.layout = layout;
    }
  }

  /** Options for the window of the PDF viewer. */
  public static class Viewer {

    /** Whether the viewer window is resized to the size of the first page. */
    private Boolean resizeToInitialPage;

    /** Whether the viewer window is centered on the screen. */
    private Boolean centerWindow;

    /** Whether the document is opened in full screen mode. */
    private Boolean fullScreen;

    /** Whether the title bar of the viewer shows the title of the document. */
    private Boolean displayDocumentTitle;

    /** Whether the menu bar of the viewer is hidden. */
    private Boolean hideMenubar;

    /** Whether the toolbar of the viewer is hidden. */
    private Boolean hideToolbar;

    /** Whether the controls of the viewer window are hidden. */
    private Boolean hideWindowControls;

    private void applyTo(final PdfOptions.Builder builder) {
      if (resizeToInitialPage != null) {
        builder.viewer(o -> o.resizeToInitialPage(resizeToInitialPage));
      }
      if (centerWindow != null) {
        builder.viewer(o -> o.centerWindow(centerWindow));
      }
      if (fullScreen != null) {
        builder.viewer(o -> o.fullScreen(fullScreen));
      }
      if (displayDocumentTitle != null) {
        builder.viewer(o -> o.displayDocumentTitle(displayDocumentTitle));
      }
      if (hideMenubar != null) {
        builder.viewer(o -> o.hideMenubar(hideMenubar));
      }
      if (hideToolbar != null) {
        builder.viewer(o -> o.hideToolbar(hideToolbar));
      }
      if (hideWindowControls != null) {
        builder.viewer(o -> o.hideWindowControls(hideWindowControls));
      }
    }

    public @Nullable Boolean getResizeToInitialPage() {
      return resizeToInitialPage;
    }

    public void setResizeToInitialPage(final @Nullable Boolean resizeToInitialPage) {
      this.resizeToInitialPage = resizeToInitialPage;
    }

    public @Nullable Boolean getCenterWindow() {
      return centerWindow;
    }

    public void setCenterWindow(final @Nullable Boolean centerWindow) {
      this.centerWindow = centerWindow;
    }

    public @Nullable Boolean getFullScreen() {
      return fullScreen;
    }

    public void setFullScreen(final @Nullable Boolean fullScreen) {
      this.fullScreen = fullScreen;
    }

    public @Nullable Boolean getDisplayDocumentTitle() {
      return displayDocumentTitle;
    }

    public void setDisplayDocumentTitle(final @Nullable Boolean displayDocumentTitle) {
      this.displayDocumentTitle = displayDocumentTitle;
    }

    public @Nullable Boolean getHideMenubar() {
      return hideMenubar;
    }

    public void setHideMenubar(final @Nullable Boolean hideMenubar) {
      this.hideMenubar = hideMenubar;
    }

    public @Nullable Boolean getHideToolbar() {
      return hideToolbar;
    }

    public void setHideToolbar(final @Nullable Boolean hideToolbar) {
      this.hideToolbar = hideToolbar;
    }

    public @Nullable Boolean getHideWindowControls() {
      return hideWindowControls;
    }

    public void setHideWindowControls(final @Nullable Boolean hideWindowControls) {
      this.hideWindowControls = hideWindowControls;
    }
  }

  /** Options for the encryption and the permissions. Not allowed with a PDF/A version. */
  public static class Security {

    /** Password required to open the documents. */
    private String openPassword;

    /** Password required to change the permissions. Required by the permissions below. */
    private String permissionPassword;

    /** Printing that is allowed. */
    private Printing printing;

    /** Changes that are allowed. */
    private Changes changes;

    /** Whether the content can be copied. */
    private Boolean copying;

    /** Whether the accessibility tools can read the text. */
    private Boolean accessibilityAccess;

    private void applyTo(final PdfOptions.Builder builder) {
      if (openPassword != null) {
        builder.security(o -> o.openPassword(openPassword));
      }
      if (permissionPassword != null) {
        builder.security(o -> o.permissionPassword(permissionPassword));
      }
      if (printing != null) {
        builder.security(o -> o.printing(printing));
      }
      if (changes != null) {
        builder.security(o -> o.changes(changes));
      }
      if (copying != null) {
        builder.security(o -> o.copying(copying));
      }
      if (accessibilityAccess != null) {
        builder.security(o -> o.accessibilityAccess(accessibilityAccess));
      }
    }

    public @Nullable String getOpenPassword() {
      return openPassword;
    }

    public void setOpenPassword(final @Nullable String openPassword) {
      this.openPassword = openPassword;
    }

    public @Nullable String getPermissionPassword() {
      return permissionPassword;
    }

    public void setPermissionPassword(final @Nullable String permissionPassword) {
      this.permissionPassword = permissionPassword;
    }

    public @Nullable Printing getPrinting() {
      return printing;
    }

    public void setPrinting(final @Nullable Printing printing) {
      this.printing = printing;
    }

    public @Nullable Changes getChanges() {
      return changes;
    }

    public void setChanges(final @Nullable Changes changes) {
      this.changes = changes;
    }

    public @Nullable Boolean getCopying() {
      return copying;
    }

    public void setCopying(final @Nullable Boolean copying) {
      this.copying = copying;
    }

    public @Nullable Boolean getAccessibilityAccess() {
      return accessibilityAccess;
    }

    public void setAccessibilityAccess(final @Nullable Boolean accessibilityAccess) {
      this.accessibilityAccess = accessibilityAccess;
    }
  }

  /** Options for a text watermark printed on each page. */
  public static class Watermark {

    /** Text of a watermark printed once across each page. */
    private String text;

    /** Text of a watermark repeated all over each page. Requires LibreOffice 6.3 or later. */
    private String tiledText;

    /**
     * Color of the watermark, as an RGB value such as FF0000. Requires LibreOffice 7.4 or later.
     */
    private String color;

    /** Font of the watermark. Requires LibreOffice 7.4 or later. */
    private String fontName;

    /** Height of the font of the watermark, in points. Requires LibreOffice 7.4 or later. */
    private Integer fontHeight;

    /** Rotation of the watermark, in degrees. Requires LibreOffice 7.4 or later. */
    private Integer rotation;

    private void applyTo(final PdfOptions.Builder builder) {
      if (text != null) {
        builder.watermark(o -> o.text(text));
      }
      if (tiledText != null) {
        builder.watermark(o -> o.tiledText(tiledText));
      }
      if (color != null) {
        builder.watermark(o -> o.color(parseColor(color)));
      }
      if (fontName != null) {
        builder.watermark(o -> o.fontName(fontName));
      }
      if (fontHeight != null) {
        builder.watermark(o -> o.fontHeight(fontHeight));
      }
      if (rotation != null) {
        builder.watermark(o -> o.rotation(rotation));
      }
    }

    public @Nullable String getText() {
      return text;
    }

    public void setText(final @Nullable String text) {
      this.text = text;
    }

    public @Nullable String getTiledText() {
      return tiledText;
    }

    public void setTiledText(final @Nullable String tiledText) {
      this.tiledText = tiledText;
    }

    public @Nullable String getColor() {
      return color;
    }

    public void setColor(final @Nullable String color) {
      this.color = color;
    }

    public @Nullable String getFontName() {
      return fontName;
    }

    public void setFontName(final @Nullable String fontName) {
      this.fontName = fontName;
    }

    public @Nullable Integer getFontHeight() {
      return fontHeight;
    }

    public void setFontHeight(final @Nullable Integer fontHeight) {
      this.fontHeight = fontHeight;
    }

    public @Nullable Integer getRotation() {
      return rotation;
    }

    public void setRotation(final @Nullable Integer rotation) {
      this.rotation = rotation;
    }
  }

  /** Options to sign the documents digitally. */
  public static class Signature {

    /**
     * Subject name of the certificate to sign with, taken from the certificate store used by the
     * office installation. Requires LibreOffice 7.4 or later.
     */
    private String certificateSubjectName;

    /**
     * Certificate to sign with, in the PEM format. Used with the private key. Requires LibreOffice
     * 25.2 or later; not supported on Windows.
     */
    private Resource certificate;

    /** Private key of the certificate, in the PEM format. */
    private Resource privateKey;

    /**
     * Certificates of the certificate authorities to trust, in the PEM format. Requires LibreOffice
     * 25.2 or later.
     */
    private Resource certificateAuthorities;

    /** Password of the private key of the certificate. */
    private String password;

    /** Location written in the signature. */
    private String location;

    /** Reason written in the signature. */
    private String reason;

    /** Contact information written in the signature. */
    private String contactInfo;

    /**
     * URL of the time stamping authority used to timestamp the signature. Requires LibreOffice 5.0
     * or later.
     */
    private String timestampAuthority;

    private void applyTo(final PdfOptions.Builder builder) {
      if (certificateSubjectName != null) {
        builder.signature(o -> o.certificateSubjectName(certificateSubjectName));
      }
      if (certificateAuthorities != null) {
        builder.signature(o -> o.caPem(read(certificateAuthorities)));
      }
      if (password != null) {
        builder.signature(o -> o.password(password));
      }
      if (location != null) {
        builder.signature(o -> o.location(location));
      }
      if (reason != null) {
        builder.signature(o -> o.reason(reason));
      }
      if (contactInfo != null) {
        builder.signature(o -> o.contactInfo(contactInfo));
      }
      if (timestampAuthority != null) {
        builder.signature(o -> o.timestampAuthority(timestampAuthority));
      }
      if (certificate != null || privateKey != null) {
        if (certificate == null || privateKey == null) {
          throw new IllegalArgumentException(
              "jodconverter.pdf.signature.certificate and jodconverter.pdf.signature.private-key"
                  + " must be used together");
        }
        builder.signature(o -> o.certificatePem(read(certificate), read(privateKey)));
      }
    }

    public @Nullable String getCertificateSubjectName() {
      return certificateSubjectName;
    }

    public void setCertificateSubjectName(final @Nullable String certificateSubjectName) {
      this.certificateSubjectName = certificateSubjectName;
    }

    public @Nullable Resource getCertificate() {
      return certificate;
    }

    public void setCertificate(final @Nullable Resource certificate) {
      this.certificate = certificate;
    }

    public @Nullable Resource getPrivateKey() {
      return privateKey;
    }

    public void setPrivateKey(final @Nullable Resource privateKey) {
      this.privateKey = privateKey;
    }

    public @Nullable Resource getCertificateAuthorities() {
      return certificateAuthorities;
    }

    public void setCertificateAuthorities(final @Nullable Resource certificateAuthorities) {
      this.certificateAuthorities = certificateAuthorities;
    }

    public @Nullable String getPassword() {
      return password;
    }

    public void setPassword(final @Nullable String password) {
      this.password = password;
    }

    public @Nullable String getLocation() {
      return location;
    }

    public void setLocation(final @Nullable String location) {
      this.location = location;
    }

    public @Nullable String getReason() {
      return reason;
    }

    public void setReason(final @Nullable String reason) {
      this.reason = reason;
    }

    public @Nullable String getContactInfo() {
      return contactInfo;
    }

    public void setContactInfo(final @Nullable String contactInfo) {
      this.contactInfo = contactInfo;
    }

    public @Nullable String getTimestampAuthority() {
      return timestampAuthority;
    }

    public void setTimestampAuthority(final @Nullable String timestampAuthority) {
      this.timestampAuthority = timestampAuthority;
    }
  }

  /** Options that only apply to presentations. */
  public static class Presentation {

    /** Whether the hidden slides are exported. */
    private Boolean hiddenSlides;

    /** Whether the notes pages are exported after the slides. */
    private Boolean notesPages;

    /** Whether only the notes pages are exported. Requires LibreOffice 5.2 or later. */
    private Boolean onlyNotesPages;

    /** Whether the slide transitions are exported. */
    private Boolean transitions;

    private void applyTo(final PdfOptions.Builder builder) {
      if (hiddenSlides != null) {
        builder.presentation(o -> o.hiddenSlides(hiddenSlides));
      }
      if (notesPages != null) {
        builder.presentation(o -> o.notesPages(notesPages));
      }
      if (onlyNotesPages != null) {
        builder.presentation(o -> o.onlyNotesPages(onlyNotesPages));
      }
      if (transitions != null) {
        builder.presentation(o -> o.transitions(transitions));
      }
    }

    public @Nullable Boolean getHiddenSlides() {
      return hiddenSlides;
    }

    public void setHiddenSlides(final @Nullable Boolean hiddenSlides) {
      this.hiddenSlides = hiddenSlides;
    }

    public @Nullable Boolean getNotesPages() {
      return notesPages;
    }

    public void setNotesPages(final @Nullable Boolean notesPages) {
      this.notesPages = notesPages;
    }

    public @Nullable Boolean getOnlyNotesPages() {
      return onlyNotesPages;
    }

    public void setOnlyNotesPages(final @Nullable Boolean onlyNotesPages) {
      this.onlyNotesPages = onlyNotesPages;
    }

    public @Nullable Boolean getTransitions() {
      return transitions;
    }

    public void setTransitions(final @Nullable Boolean transitions) {
      this.transitions = transitions;
    }
  }

  /** Options that only apply to spreadsheets. */
  public static class Spreadsheet {

    /** Whether each sheet is exported as a single page. Requires LibreOffice 6.4 or later. */
    private Boolean singlePageSheets;

    /** Sheets to export, by their position, such as 1-3. Requires LibreOffice 24.8 or later. */
    private String sheetRange;

    private void applyTo(final PdfOptions.Builder builder) {
      if (singlePageSheets != null) {
        builder.spreadsheet(o -> o.singlePageSheets(singlePageSheets));
      }
      if (sheetRange != null) {
        builder.spreadsheet(o -> o.sheetRange(sheetRange));
      }
    }

    public @Nullable Boolean getSinglePageSheets() {
      return singlePageSheets;
    }

    public void setSinglePageSheets(final @Nullable Boolean singlePageSheets) {
      this.singlePageSheets = singlePageSheets;
    }

    public @Nullable String getSheetRange() {
      return sheetRange;
    }

    public void setSheetRange(final @Nullable String sheetRange) {
      this.sheetRange = sheetRange;
    }
  }
}
