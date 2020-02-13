/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

package org.jodconverter.core.document;

import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Default {@code DocumentFormat} registry. It contains the list of {@code DocumentFormat} that
 * should be enough to cover most of our needs.
 *
 * <p>See <a
 * href="https://wiki.openoffice.org/wiki/Framework/Article/Filter/FilterList_OOo_3_0">OpenOffice
 * Filters Documentation</a>
 *
 * <p>See <a
 * href="http://opengrok.libreoffice.org/xref/core/filter/source/config/fragments/filters">LibreOffice
 * Filters</a> and <a
 * href="https://svn.apache.org/repos/asf/openoffice/trunk/main/filter/source/config/fragments/filters">OpenOffice
 * Filters</a>.
 */
public final class DefaultDocumentFormatRegistry {

  // Another sources
  // https://wiki.openoffice.org/wiki/Framework/Article/Filter/FilterList_SO_8
  // https://ask.libreoffice.org/en/question/59204/what-are-the-5x-writer-importexport-filters/

  /**
   * Portable Document Format.
   *
   * <ul>
   *   <li>Extension: pdf
   *   <li>Media Type: application/pdf
   * </ul>
   */
  @NonNull public static final DocumentFormat PDF = byExtension("pdf");

  /**
   * Macromedia Flash.
   *
   * <ul>
   *   <li>Extension: swf
   *   <li>Media Type: application/x-shockwave-flash
   * </ul>
   */
  @NonNull public static final DocumentFormat SWF = byExtension("swf");

  /**
   * HTML.
   *
   * <ul>
   *   <li>Extension: html
   *   <li>Media Type: text/html
   * </ul>
   */
  @NonNull public static final DocumentFormat HTML = byExtension("html");

  /**
   * XHTML.
   *
   * <ul>
   *   <li>Extension: xhtml
   *   <li>Media Type: application/xhtml+xml
   * </ul>
   */
  @NonNull public static final DocumentFormat XHTML = byExtension("xhtml");

  /**
   * OpenDocument Text.
   *
   * <ul>
   *   <li>Extension: odt
   *   <li>Media Type: application/vnd.oasis.opendocument.text
   * </ul>
   */
  @NonNull public static final DocumentFormat ODT = byExtension("odt");

  /**
   * OpenDocument Text Template.
   *
   * <ul>
   *   <li>Extension: ott
   *   <li>Media Type: application/vnd.oasis.opendocument.text-template
   * </ul>
   */
  @NonNull public static final DocumentFormat OTT = byExtension("ott");

  /**
   * OpenDocument Text Flat XML.
   *
   * <ul>
   *   <li>Extension: fodt
   *   <li>Media Type: application/vnd.oasis.opendocument.text-flat-xml
   * </ul>
   */
  @NonNull public static final DocumentFormat FODT = byExtension("fodt");

  /**
   * OpenOffice.org 1.0 Text Document.
   *
   * <ul>
   *   <li>Extension: swx
   *   <li>Media Type: application/vnd.sun.xml.writer
   * </ul>
   */
  @NonNull public static final DocumentFormat SXW = byExtension("sxw");

  /**
   * Microsoft Word 97-2003.
   *
   * <ul>
   *   <li>Extension: doc
   *   <li>Media Type: application/msword
   * </ul>
   */
  @NonNull public static final DocumentFormat DOC = byExtension("doc");

  /**
   * Microsoft Word 2007-2013 XML.
   *
   * <ul>
   *   <li>Extension: docx
   *   <li>Media Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document
   * </ul>
   */
  @NonNull public static final DocumentFormat DOCX = byExtension("docx");

  /**
   * Rich Text Format.
   *
   * <ul>
   *   <li>Extension: rtf
   *   <li>Media Type: text/rtf"
   * </ul>
   */
  @NonNull public static final DocumentFormat RTF = byExtension("rtf");

  /**
   * WordPerfect.
   *
   * <ul>
   *   <li>Extension: wpd
   *   <li>Media Type: application/wordperfect
   * </ul>
   */
  @NonNull public static final DocumentFormat WPD = byExtension("wpd");

  /**
   * Plain Text.
   *
   * <ul>
   *   <li>Extension: txt
   *   <li>Media Type: text/plain
   * </ul>
   */
  @NonNull public static final DocumentFormat TXT = byExtension("txt");

  /**
   * OpenDocument Spreadsheet.
   *
   * <ul>
   *   <li>Extension: ods
   *   <li>Media Type: application/vnd.oasis.opendocument.spreadsheet
   * </ul>
   */
  @NonNull public static final DocumentFormat ODS = byExtension("ods");

  /**
   * OpenDocument Spreadsheet Template.
   *
   * <ul>
   *   <li>Extension: ots
   *   <li>Media Type: application/vnd.oasis.opendocument.spreadsheet-template
   * </ul>
   */
  @NonNull public static final DocumentFormat OTS = byExtension("ots");

  /**
   * OpenDocument Spreadsheet Flat XML.
   *
   * <ul>
   *   <li>Extension: fods
   *   <li>Media Type: application/vnd.oasis.opendocument.spreadsheet-flat-xml
   * </ul>
   */
  @NonNull public static final DocumentFormat FODS = byExtension("fods");

  /**
   * OpenOffice.org 1.0 Spreadsheet.
   *
   * <ul>
   *   <li>Extension: sxc
   *   <li>Media Type: application/vnd.sun.xml.calc
   * </ul>
   */
  @NonNull public static final DocumentFormat SXC = byExtension("sxc");

  /**
   * Microsoft Excel 97-2003.
   *
   * <ul>
   *   <li>Extension: xls
   *   <li>Media Type: application/vnd.ms-excel
   * </ul>
   */
  @NonNull public static final DocumentFormat XLS = byExtension("xls");

  /**
   * Microsoft Excel 2007-2013 XML.
   *
   * <ul>
   *   <li>Extension: xlsx
   *   <li>Media Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
   * </ul>
   */
  @NonNull public static final DocumentFormat XLSX = byExtension("xlsx");

  /**
   * Comma Separated Values.
   *
   * <ul>
   *   <li>Extension: csv
   *   <li>Media Type: text/csv
   * </ul>
   */
  @NonNull public static final DocumentFormat CSV = byExtension("csv");

  /**
   * Tab Separated Values.
   *
   * <ul>
   *   <li>Extension: tsv
   *   <li>Media Type: text/tab-separated-values
   * </ul>
   */
  @NonNull public static final DocumentFormat TSV = byExtension("tsv");

  /**
   * OpenDocument Presentation.
   *
   * <ul>
   *   <li>Extension: odp
   *   <li>Media Type: application/vnd.oasis.opendocument.presentation
   * </ul>
   */
  @NonNull public static final DocumentFormat ODP = byExtension("odp");

  /**
   * OpenDocument Presentation Template.
   *
   * <ul>
   *   <li>Extension: otp
   *   <li>Media Type: application/vnd.oasis.opendocument.presentation-template
   * </ul>
   */
  @NonNull public static final DocumentFormat OTP = byExtension("otp");

  /**
   * OpenDocument Presentation Flat XML.
   *
   * <ul>
   *   <li>Extension: fodp
   *   <li>Media Type: application/vnd.oasis.opendocument.presentation-flat-xml
   * </ul>
   */
  @NonNull public static final DocumentFormat FODP = byExtension("fodp");

  /**
   * OpenOffice.org 1.0 Presentation.
   *
   * <ul>
   *   <li>Extension: sxi
   *   <li>Media Type: application/vnd.sun.xml.impress
   * </ul>
   */
  @NonNull public static final DocumentFormat SXI = byExtension("sxi");

  /**
   * Microsoft PowerPoint 97-2003.
   *
   * <ul>
   *   <li>Extension: ppt
   *   <li>Media Type: application/vnd.ms-powerpoint
   * </ul>
   */
  @NonNull public static final DocumentFormat PPT = byExtension("ppt");

  /**
   * Microsoft PowerPoint 2007-2013 XML.
   *
   * <ul>
   *   <li>Extension: pptx
   *   <li>Media Type: application/vnd.openxmlformats-officedocument.presentationml.presentation
   * </ul>
   */
  @NonNull public static final DocumentFormat PPTX = byExtension("pptx");

  /**
   * OpenDocument Drawing.
   *
   * <ul>
   *   <li>Extension: odg
   *   <li>Media Type: application/vnd.oasis.opendocument.graphics
   * </ul>
   */
  @NonNull public static final DocumentFormat ODG = byExtension("odg");

  /**
   * OpenDocument Drawing Template.
   *
   * <ul>
   *   <li>Extension: otg
   *   <li>Media Type: application/vnd.oasis.opendocument.graphics
   * </ul>
   */
  @NonNull public static final DocumentFormat OTG = byExtension("otg");

  /**
   * OpenDocument Drawing Flat XML.
   *
   * <ul>
   *   <li>Extension: fodg
   *   <li>Media Type: application/vnd.oasis.opendocument.graphics-flat-xml
   * </ul>
   */
  @NonNull public static final DocumentFormat FODG = byExtension("fodg");

  /**
   * Scalable Vector Graphics.
   *
   * <ul>
   *   <li>Extension: svg
   *   <li>Media Type: image/svg+xml
   * </ul>
   */
  @NonNull public static final DocumentFormat SVG = byExtension("svg");

  /**
   * Visio format.
   *
   * <ul>
   *   <li>Extension: vsd
   *   <li>Media Type: application/x-visio
   * </ul>
   */
  @NonNull public static final DocumentFormat VSD = byExtension("vsd");

  /**
   * New Visio format.
   *
   * <ul>
   *   <li>Extension: vsdx
   *   <li>Media Type: application/vnd-ms-visio.drawing
   * </ul>
   */
  @NonNull public static final DocumentFormat VSDX = byExtension("vsdx");

  /**
   * Portable Network Graphics.
   *
   * <ul>
   *   <li>Extension: png
   *   <li>Media Type: image/png
   * </ul>
   */
  @NonNull public static final DocumentFormat PNG = byExtension("png");

  /**
   * Joint Photographic Experts Group.
   *
   * <ul>
   *   <li>Extensions: jpg, jpeg
   *   <li>Media Type: image/jpg
   * </ul>
   */
  @NonNull public static final DocumentFormat JPEG = byExtension("jpg");

  /**
   * Tagged Image File Format.
   *
   * <ul>
   *   <li>Extensions: tif, tiff
   *   <li>Media Type: image/tif
   * </ul>
   */
  @NonNull public static final DocumentFormat TIFF = byExtension("tif");

  /**
   * Graphic Interchange Format.
   *
   * <ul>
   *   <li>Extension: gif
   *   <li>Media Type: image/gif
   * </ul>
   */
  @NonNull public static final DocumentFormat GIF = byExtension("gif");

  /**
   * Windows Bitmap.
   *
   * <ul>
   *   <li>Extension: bmp
   *   <li>Media Type: image/bmp
   * </ul>
   */
  @NonNull public static final DocumentFormat BMP = byExtension("bmp");

  /**
   * Gets the default instance of the class.
   *
   * @return The default DocumentFormatRegistry.
   */
  @NonNull
  public static DocumentFormatRegistry getInstance() {
    return DefaultDocumentFormatRegistryInstanceHolder.getInstance();
  }

  private static DocumentFormat byExtension(final String extension) {
    final DocumentFormat fmt = getInstance().getFormatByExtension(extension);
    Validate.notNull(fmt, "byExtension(extension) must not be null");
    return fmt;
  }

  /**
   * Gets a document format for the specified extension.
   *
   * @param extension The extension whose document format will be returned.
   * @return The found document format, or {@code null} if no document format exists for the
   *     specified extension.
   */
  @Nullable
  public static DocumentFormat getFormatByExtension(@NonNull final String extension) {
    return getInstance().getFormatByExtension(extension);
  }

  /**
   * Gets a document format for the specified media type.
   *
   * @param mediaType The media type whose document format will be returned.
   * @return The found document format, or {@code null} if no document format exists for the
   *     specified media type.
   */
  @Nullable
  public static DocumentFormat getFormatByMediaType(@NonNull final String mediaType) {
    return getInstance().getFormatByMediaType(mediaType);
  }

  /**
   * Gets all the {@link DocumentFormat}s of a given family.
   *
   * @param family The family whose document formats will be returned.
   * @return A set with all the document formats for the specified family.
   */
  @NonNull
  public static Set<@NonNull DocumentFormat> getOutputFormats(
      @NonNull final DocumentFamily family) {
    return getInstance().getOutputFormats(family);
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private DefaultDocumentFormatRegistry() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
