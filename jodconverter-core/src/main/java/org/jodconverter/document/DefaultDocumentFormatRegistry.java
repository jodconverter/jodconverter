/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Holds the default {@code DocumentFormat} registry. It contains the list of {@code DocumentFormat}
 * that should be enough to cover most of our needs.
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

  /**
   * Portable Document Format.
   *
   * <ul>
   *   <li>Extension: pdf
   *   <li>Media Type: application/pdf
   * </ul>
   */
  public static final DocumentFormat PDF = getInstance().getFormatByExtension("pdf");

  /**
   * Macromedia Flash.
   *
   * <ul>
   *   <li>Extension: swf
   *   <li>Media Type: application/x-shockwave-flash
   * </ul>
   */
  public static final DocumentFormat SWF = getInstance().getFormatByExtension("swf");

  /**
   * HTML.
   *
   * <ul>
   *   <li>Extension: html
   *   <li>Media Type: text/html
   * </ul>
   */
  public static final DocumentFormat HTML = getInstance().getFormatByExtension("html");

  /**
   * OpenDocument Text.
   *
   * <ul>
   *   <li>Extension: odt
   *   <li>Media Type: application/vnd.oasis.opendocument.text
   * </ul>
   */
  public static final DocumentFormat ODT = getInstance().getFormatByExtension("odt");

  /**
   * OpenDocument Text Template.
   *
   * <ul>
   *   <li>Extension: ott
   *   <li>Media Type: application/vnd.oasis.opendocument.text-template
   * </ul>
   */
  public static final DocumentFormat OTT = getInstance().getFormatByExtension("ott");

  /**
   * OpenOffice.org 1.0 Text Document.
   *
   * <ul>
   *   <li>Extension: swx
   *   <li>Media Type: application/vnd.sun.xml.writer
   * </ul>
   */
  public static final DocumentFormat SXW = getInstance().getFormatByExtension("sxw");

  /**
   * Microsoft Word 97-2003.
   *
   * <ul>
   *   <li>Extension: doc
   *   <li>Media Type: application/msword
   * </ul>
   */
  public static final DocumentFormat DOC = getInstance().getFormatByExtension("doc");

  /**
   * Microsoft Word 2007-2013 XML.
   *
   * <ul>
   *   <li>Extension: docx
   *   <li>Media Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document
   * </ul>
   */
  public static final DocumentFormat DOCX = getInstance().getFormatByExtension("docx");

  /**
   * Rich Text Format.
   *
   * <ul>
   *   <li>Extension: rtf
   *   <li>Media Type: text/rtf"
   * </ul>
   */
  public static final DocumentFormat RTF = getInstance().getFormatByExtension("rtf");

  /**
   * WordPerfect.
   *
   * <ul>
   *   <li>Extension: wpd
   *   <li>Media Type: application/wordperfect
   * </ul>
   */
  public static final DocumentFormat WPD = getInstance().getFormatByExtension("wpd");

  /**
   * Plain Text.
   *
   * <ul>
   *   <li>Extension: txt
   *   <li>Media Type: text/plain
   * </ul>
   */
  public static final DocumentFormat TXT = getInstance().getFormatByExtension("txt");

  /**
   * OpenDocument Spreadsheet.
   *
   * <ul>
   *   <li>Extension: ods
   *   <li>Media Type: application/vnd.oasis.opendocument.spreadsheet
   * </ul>
   */
  public static final DocumentFormat ODS = getInstance().getFormatByExtension("ods");

  /**
   * OpenDocument Spreadsheet Template.
   *
   * <ul>
   *   <li>Extension: ots
   *   <li>Media Type: application/vnd.oasis.opendocument.spreadsheet-template
   * </ul>
   */
  public static final DocumentFormat OTS = getInstance().getFormatByExtension("ots");

  /**
   * OpenOffice.org 1.0 Spreadsheet.
   *
   * <ul>
   *   <li>Extension: sxc
   *   <li>Media Type: application/vnd.sun.xml.calc
   * </ul>
   */
  public static final DocumentFormat SXC = getInstance().getFormatByExtension("sxc");

  /**
   * Microsoft Excel 97-2003.
   *
   * <ul>
   *   <li>Extension: xls
   *   <li>Media Type: application/vnd.ms-excel
   * </ul>
   */
  public static final DocumentFormat XLS = getInstance().getFormatByExtension("xls");

  /**
   * Microsoft Excel 2007-2013 XML.
   *
   * <ul>
   *   <li>Extension: xlsx
   *   <li>Media Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
   * </ul>
   */
  public static final DocumentFormat XLSX = getInstance().getFormatByExtension("xlsx");

  /**
   * Comma Separated Values.
   *
   * <ul>
   *   <li>Extension: csv
   *   <li>Media Type: text/csv
   * </ul>
   */
  public static final DocumentFormat CSV = getInstance().getFormatByExtension("csv");

  /**
   * Tab Separated Values.
   *
   * <ul>
   *   <li>Extension: tsv
   *   <li>Media Type: text/tab-separated-values
   * </ul>
   */
  public static final DocumentFormat TSV = getInstance().getFormatByExtension("tsv");

  /**
   * OpenDocument Presentation.
   *
   * <ul>
   *   <li>Extension: odp
   *   <li>Media Type: application/vnd.oasis.opendocument.presentation
   * </ul>
   */
  public static final DocumentFormat ODP = getInstance().getFormatByExtension("odp");

  /**
   * OpenDocument Presentation Template.
   *
   * <ul>
   *   <li>Extension: otp
   *   <li>Media Type: application/vnd.oasis.opendocument.presentation-template
   * </ul>
   */
  public static final DocumentFormat OTP = getInstance().getFormatByExtension("otp");

  /**
   * OpenOffice.org 1.0 Presentation.
   *
   * <ul>
   *   <li>Extension: sxi
   *   <li>Media Type: application/vnd.sun.xml.impress
   * </ul>
   */
  public static final DocumentFormat SXI = getInstance().getFormatByExtension("sxi");

  /**
   * Microsoft PowerPoint 97-2003.
   *
   * <ul>
   *   <li>Extension: ppt
   *   <li>Media Type: application/vnd.ms-powerpoint
   * </ul>
   */
  public static final DocumentFormat PPT = getInstance().getFormatByExtension("ppt");

  /**
   * Microsoft PowerPoint 2007-2013 XML.
   *
   * <ul>
   *   <li>Extension: pptx
   *   <li>Media Type: application/vnd.openxmlformats-officedocument.presentationml.presentation
   * </ul>
   */
  public static final DocumentFormat PPTX = getInstance().getFormatByExtension("pptx");

  /**
   * OpenDocument Drawing.
   *
   * <ul>
   *   <li>Extension: odg
   *   <li>Media Type: application/vnd.oasis.opendocument.graphics
   * </ul>
   */
  public static final DocumentFormat ODG = getInstance().getFormatByExtension("odg");

  /**
   * OpenDocument Drawing Template.
   *
   * <ul>
   *   <li>Extension: otg
   *   <li>Media Type: application/vnd.oasis.opendocument.graphics
   * </ul>
   */
  public static final DocumentFormat OTG = getInstance().getFormatByExtension("otg");

  /**
   * Scalable Vector Graphics.
   *
   * <ul>
   *   <li>Extension: svg
   *   <li>Media Type: image/svg+xml
   * </ul>
   */
  public static final DocumentFormat SVG = getInstance().getFormatByExtension("svg");

  /**
   * Portable Network Graphics.
   *
   * <ul>
   *   <li>Extension: png
   *   <li>Media Type: image/png
   * </ul>
   */
  public static final DocumentFormat PNG = getInstance().getFormatByExtension("png");

  /**
   * The InstanceHolder inner class is used to initialize the static INSTANCE on demand (the
   * instance will be initialized the first time it is used). Working this way allow us to create
   * (and thus load default document format) the static INSTANCE only if needed.
   */
  private static class InstanceHolder { // NOSONAR
    static final DocumentFormatRegistry INSTANCE = create(); // NOSONAR

    private static DocumentFormatRegistry create() {

      try (final InputStream input =
          DefaultDocumentFormatRegistry.class.getResourceAsStream("/document-formats.json")) {
        return JsonDocumentFormatRegistry.create(input);
      } catch (IOException ex) {
        throw new DocumentFormatRegistryException(
            "Unable to load the default document-formats.json configuration file", ex);
      }
    }
  }

  /**
   * Gets the default instance of the class.
   *
   * @return The ResourceManager used at this class hierarchy level.
   */
  public static DocumentFormatRegistry getInstance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Gets a document format for the specified extension.
   *
   * @param extension The extension whose document format will be returned.
   * @return The found document format, or {@code null} if no document format exists for the
   *     specified extension.
   */
  public static DocumentFormat getFormatByExtension(final String extension) {

    return getInstance().getFormatByExtension(extension);
  }

  /**
   * Gets a document format for the specified media type.
   *
   * @param mediaType The media type whose document format will be returned.
   * @return The found document format, or {@code null} if no document format exists for the
   *     specified media type.
   */
  public static DocumentFormat getFormatByMediaType(final String mediaType) {

    return getInstance().getFormatByMediaType(mediaType);
  }

  /**
   * Gets all the {@link DocumentFormat}s of a given family.
   *
   * @param family The family whose document formats will be returned.
   * @return A set with all the document formats for the specified family.
   */
  public static Set<DocumentFormat> getOutputFormats(final DocumentFamily family) {

    return getInstance().getOutputFormats(family);
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private DefaultDocumentFormatRegistry() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
