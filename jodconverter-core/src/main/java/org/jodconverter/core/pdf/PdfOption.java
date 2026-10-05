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

/**
 * The properties of the FilterData of the PDF export filter that {@link PdfOptions} can set, with
 * the first LibreOffice version that reads each of them.
 *
 * <p>The versions come from the {@code filter/source/pdf/pdfexport.cxx} file of each LibreOffice
 * release branch, from 3.5 to 26.8, and of Apache OpenOffice 4.1.
 */
enum PdfOption {

  // General
  SELECT_PDF_VERSION("SelectPdfVersion"),
  PDF_UA_COMPLIANCE("PDFUACompliance", 7, 0),
  USE_TAGGED_PDF("UseTaggedPDF"),
  IS_ADD_STREAM("IsAddStream"),
  USE_REFERENCE_XOBJECT("UseReferenceXObject", 5, 4),

  // Images
  USE_LOSSLESS_COMPRESSION("UseLosslessCompression"),
  QUALITY("Quality"),
  REDUCE_IMAGE_RESOLUTION("ReduceImageResolution"),
  MAX_IMAGE_RESOLUTION("MaxImageResolution"),

  // Pages
  PAGE_RANGE("PageRange"),
  IS_SKIP_EMPTY_PAGES("IsSkipEmptyPages"),
  EXPORT_PLACEHOLDERS("ExportPlaceholders", 5, 1),
  EXPORT_TRACKED_CHANGES("ExportTrackedChanges", 26, 2),

  // Comments
  EXPORT_NOTES("ExportNotes"),
  EXPORT_NOTES_IN_MARGIN("ExportNotesInMargin", 7, 5),

  // Bookmarks
  EXPORT_BOOKMARKS("ExportBookmarks"),
  OPEN_BOOKMARK_LEVELS("OpenBookmarkLevels"),
  EXPORT_BOOKMARKS_TO_PDF_DESTINATION("ExportBookmarksToPDFDestination"),

  // Forms
  EXPORT_FORM_FIELDS("ExportFormFields"),
  FORMS_TYPE("FormsType"),
  ALLOW_DUPLICATE_FIELD_NAMES("AllowDuplicateFieldNames"),

  // Links
  EXPORT_LINKS_RELATIVE_FSYS("ExportLinksRelativeFsys"),
  CONVERT_OOO_TARGET_TO_PDF_TARGET("ConvertOOoTargetToPDFTarget"),
  PDF_VIEW_SELECTION("PDFViewSelection"),

  // Initial view
  INITIAL_VIEW("InitialView"),
  INITIAL_PAGE("InitialPage"),
  MAGNIFICATION("Magnification"),
  ZOOM("Zoom"),
  PAGE_LAYOUT("PageLayout"),

  // Viewer window
  RESIZE_WINDOW_TO_INITIAL_PAGE("ResizeWindowToInitialPage"),
  CENTER_WINDOW("CenterWindow"),
  OPEN_IN_FULL_SCREEN_MODE("OpenInFullScreenMode"),
  DISPLAY_PDF_DOCUMENT_TITLE("DisplayPDFDocumentTitle"),
  HIDE_VIEWER_MENUBAR("HideViewerMenubar"),
  HIDE_VIEWER_TOOLBAR("HideViewerToolbar"),
  HIDE_VIEWER_WINDOW_CONTROLS("HideViewerWindowControls"),

  // Security
  ENCRYPT_FILE("EncryptFile"),
  DOCUMENT_OPEN_PASSWORD("DocumentOpenPassword", true),
  RESTRICT_PERMISSIONS("RestrictPermissions"),
  PERMISSION_PASSWORD("PermissionPassword", true),
  PRINTING("Printing"),
  CHANGES("Changes"),
  ENABLE_COPYING_OF_CONTENT("EnableCopyingOfContent"),
  ENABLE_TEXT_ACCESS_FOR_ACCESSIBILITY_TOOLS("EnableTextAccessForAccessibilityTools"),

  // Watermark
  WATERMARK("Watermark"),
  TILED_WATERMARK("TiledWatermark", 6, 3),
  WATERMARK_COLOR("WatermarkColor", 7, 4),
  WATERMARK_FONT_NAME("WatermarkFontName", 7, 4),
  WATERMARK_FONT_HEIGHT("WatermarkFontHeight", 7, 4),
  WATERMARK_ROTATE_ANGLE("WatermarkRotateAngle", 7, 4),

  // Digital signature
  SIGN_PDF("SignPDF", 4, 0),
  SIGN_CERTIFICATE_SUBJECT_NAME("SignCertificateSubjectName", 7, 4),
  SIGN_CERTIFICATE_CERT_PEM("SignCertificateCertPem", 25, 2, true),
  SIGN_CERTIFICATE_KEY_PEM("SignCertificateKeyPem", 25, 2, true),
  SIGN_CERTIFICATE_CA_PEM("SignCertificateCaPem", 25, 2, true),
  SIGNATURE_PASSWORD("SignaturePassword", 4, 0, true),
  SIGNATURE_LOCATION("SignatureLocation", 4, 0),
  SIGNATURE_REASON("SignatureReason", 4, 0),
  SIGNATURE_CONTACT_INFO("SignatureContactInfo", 4, 0),
  SIGNATURE_TSA("SignatureTSA", 5, 0),

  // Presentation (Impress)
  EXPORT_HIDDEN_SLIDES("ExportHiddenSlides", 3, 5),
  EXPORT_NOTES_PAGES("ExportNotesPages"),
  EXPORT_ONLY_NOTES_PAGES("ExportOnlyNotesPages", 5, 2),
  USE_TRANSITION_EFFECTS("UseTransitionEffects"),

  // Spreadsheet (Calc)
  SINGLE_PAGE_SHEETS("SinglePageSheets", 6, 4),
  SHEET_RANGE("SheetRange", 24, 8);

  private final String filterDataName;
  private final OfficeSupport support;
  private final boolean secret;

  // An option read by all the versions of LibreOffice and by Apache OpenOffice.
  PdfOption(final String filterDataName) {
    this(filterDataName, false);
  }

  PdfOption(final String filterDataName, final boolean secret) {
    this.filterDataName = filterDataName;
    this.support = OfficeSupport.ALL;
    this.secret = secret;
  }

  // An option read by LibreOffice only, since the given version.
  PdfOption(final String filterDataName, final int major, final int minor) {
    this(filterDataName, major, minor, false);
  }

  PdfOption(final String filterDataName, final int major, final int minor, final boolean secret) {
    this.filterDataName = filterDataName;
    this.support = OfficeSupport.libreOffice(major, minor);
    this.secret = secret;
  }

  /**
   * Gets the name of the property in the FilterData of the PDF export filter.
   *
   * @return The property name.
   */
  /* default */ String getFilterDataName() {
    return filterDataName;
  }

  /**
   * Gets which office installations read this option.
   *
   * @return The office support.
   */
  /* default */ OfficeSupport getSupport() {
    return support;
  }

  /**
   * Gets whether the value of this option is a secret, such as a password, that must not be written
   * to a log.
   *
   * @return {@code true} if the value is a secret, {@code false} otherwise.
   */
  /* default */ boolean isSecret() {
    return secret;
  }
}
