/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.jodconverter.document;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default {@code DocumentFormat} registry. It contains the {@code DocumentFormat} that should be
 * enough to cover most of our needs.
 *
 * <p>LibreOffice filters:
 * http://opengrok.libreoffice.org/xref/core/filter/source/config/fragments/filters/ OpenOffice
 * filters:
 * https://svn.apache.org/repos/asf/openoffice/trunk/main/filter/source/config/fragments/filters/
 */
public class DefaultDocumentFormatRegistry extends SimpleDocumentFormatRegistry {

  private static final String KEY_FILTER_NAME = "FilterName";
  private static final String KEY_FILTER_OPTIONS = "FilterOptions";

  /**
   * The InstanceHolder inner class is used to initialize the static INSTANCE on demand (the
   * instance will be initialized the first time it is used). Working this way allow us to create
   * (and thus load default document format) the static INSTANCE only if needed.
   */
  private static class InstanceHolder { // NOSONAR
    public static DefaultDocumentFormatRegistry INSTANCE; // NOSONAR

    static {
      INSTANCE = new DefaultDocumentFormatRegistry();
      INSTANCE.loadDefaults();
    }
  }

  /**
   * Creates a DefaultDocumentFormatRegistry.
   *
   * @return the created DefaultDocumentFormatRegistry.
   */
  public static DefaultDocumentFormatRegistry create() {

    final DefaultDocumentFormatRegistry registry = new DefaultDocumentFormatRegistry();
    registry.loadDefaults();
    return registry;
  }

  /**
   * Gets the default instance of the class.
   *
   * @return the ResourceManager used at this class hierarchy level.
   */
  public static DefaultDocumentFormatRegistry getInstance() {
    return InstanceHolder.INSTANCE;
  }

  // Force static function call
  private DefaultDocumentFormatRegistry() { // NOSONAR
    super();
  }

  private void loadCsv() {

    final DocumentFormat csv = new DocumentFormat("Comma Separated Values", "csv", "text/csv");
    csv.setInputFamily(DocumentFamily.SPREADSHEET);
    final Map<String, Object> csvLoadAndStoreProperties = new LinkedHashMap<>();
    csvLoadAndStoreProperties.put(KEY_FILTER_NAME, "Text - txt - csv (StarCalc)");
    csvLoadAndStoreProperties.put(
        KEY_FILTER_OPTIONS, "44,34,0"); // Field Separator: ','; Text Delimiter: '"'
    csv.setLoadProperties(csvLoadAndStoreProperties);
    csv.setStoreProperties(DocumentFamily.SPREADSHEET, csvLoadAndStoreProperties);
    addFormat(csv);
  }

  // Load all the default supported DocumentFormat.
  private void loadDefaults() {

    loadPdf();
    loadSwf();
    // disabled because it's not always available
    //loadXhtml();
    loadHtml();
    loadOdt();
    loadSxw();
    loadDoc();
    loadDocx();
    loadRtf();
    loadWpd();
    loadTxt();
    //loadWikitext();
    loadOds();
    loadSxc();
    loadXls();
    loadXlsx();
    loadCsv();
    loadTsv();
    loadOdp();
    loadSxi();
    loadPpt();
    loadPptx();
    loadOdg();
    loadSvg();
    loadPng();
  }

  private void loadDoc() {

    final DocumentFormat doc =
        new DocumentFormat("Microsoft Word 97-2003", "doc", "application/msword");
    doc.setInputFamily(DocumentFamily.TEXT);
    doc.setStoreProperties(
        DocumentFamily.TEXT, Collections.singletonMap(KEY_FILTER_NAME, "MS Word 97"));
    addFormat(doc);
  }

  private void loadDocx() {

    final DocumentFormat docx =
        new DocumentFormat(
            "Microsoft Word 2007-2013 XML",
            "docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    docx.setInputFamily(DocumentFamily.TEXT);
    docx.setStoreProperties(
        DocumentFamily.TEXT, Collections.singletonMap(KEY_FILTER_NAME, "MS Word 2007 XML"));
    addFormat(docx);
  }

  private void loadHtml() {

    final DocumentFormat html = new DocumentFormat("HTML", "html", "text/html");
    // HTML is treated as Text when supplied as input, but as an output it is also
    // available for exporting Spreadsheet and Presentation formats.
    html.setInputFamily(DocumentFamily.TEXT);
    html.setStoreProperties(
        DocumentFamily.TEXT, Collections.singletonMap(KEY_FILTER_NAME, "HTML (StarWriter)"));
    html.setStoreProperties(
        DocumentFamily.SPREADSHEET, Collections.singletonMap(KEY_FILTER_NAME, "HTML (StarCalc)"));
    html.setStoreProperties(
        DocumentFamily.PRESENTATION,
        Collections.singletonMap(KEY_FILTER_NAME, "impress_html_Export"));
    addFormat(html);
  }

  private void loadOdg() {

    final DocumentFormat odg =
        new DocumentFormat(
            "OpenDocument Drawing", "odg", "application/vnd.oasis.opendocument.graphics");
    odg.setInputFamily(DocumentFamily.DRAWING);
    odg.setStoreProperties(
        DocumentFamily.DRAWING, Collections.singletonMap(KEY_FILTER_NAME, "draw8"));
    addFormat(odg);
  }

  private void loadOdp() {

    final DocumentFormat odp =
        new DocumentFormat(
            "OpenDocument Presentation", "odp", "application/vnd.oasis.opendocument.presentation");
    odp.setInputFamily(DocumentFamily.PRESENTATION);
    odp.setStoreProperties(
        DocumentFamily.PRESENTATION, Collections.singletonMap(KEY_FILTER_NAME, "impress8"));
    addFormat(odp);
  }

  private void loadOds() {

    final DocumentFormat ods =
        new DocumentFormat(
            "OpenDocument Spreadsheet", "ods", "application/vnd.oasis.opendocument.spreadsheet");
    ods.setInputFamily(DocumentFamily.SPREADSHEET);
    ods.setStoreProperties(
        DocumentFamily.SPREADSHEET, Collections.singletonMap(KEY_FILTER_NAME, "calc8"));
    addFormat(ods);
  }

  private void loadOdt() {

    final DocumentFormat odt =
        new DocumentFormat("OpenDocument Text", "odt", "application/vnd.oasis.opendocument.text");
    odt.setInputFamily(DocumentFamily.TEXT);
    odt.setStoreProperties(
        DocumentFamily.TEXT, Collections.singletonMap(KEY_FILTER_NAME, "writer8"));
    addFormat(odt);
  }

  // Load all the default supported DocumentFormat.
  private void loadPdf() {

    final DocumentFormat pdf =
        new DocumentFormat("Portable Document Format", "pdf", "application/pdf");
    pdf.setStoreProperties(
        DocumentFamily.TEXT, Collections.singletonMap(KEY_FILTER_NAME, "writer_pdf_Export"));
    pdf.setStoreProperties(
        DocumentFamily.SPREADSHEET, Collections.singletonMap(KEY_FILTER_NAME, "calc_pdf_Export"));
    pdf.setStoreProperties(
        DocumentFamily.PRESENTATION,
        Collections.singletonMap(KEY_FILTER_NAME, "impress_pdf_Export"));
    pdf.setStoreProperties(
        DocumentFamily.DRAWING, Collections.singletonMap(KEY_FILTER_NAME, "draw_pdf_Export"));
    addFormat(pdf);
  }

  private void loadPng() {

    final DocumentFormat png = new DocumentFormat("Portable Network Graphics", "png", "image/png");
    png.setStoreProperties(
        DocumentFamily.TEXT, Collections.singletonMap(KEY_FILTER_NAME, "writer_png_Export"));
    png.setStoreProperties(
        DocumentFamily.SPREADSHEET, Collections.singletonMap(KEY_FILTER_NAME, "calc_png_Export"));
    png.setStoreProperties(
        DocumentFamily.PRESENTATION,
        Collections.singletonMap(KEY_FILTER_NAME, "impress_png_Export"));
    png.setStoreProperties(
        DocumentFamily.DRAWING, Collections.singletonMap(KEY_FILTER_NAME, "draw_png_Export"));
    addFormat(png);
  }

  private void loadPpt() {

    final DocumentFormat ppt =
        new DocumentFormat("Microsoft PowerPoint 97-2003", "ppt", "application/vnd.ms-powerpoint");
    ppt.setInputFamily(DocumentFamily.PRESENTATION);
    ppt.setStoreProperties(
        DocumentFamily.PRESENTATION, Collections.singletonMap(KEY_FILTER_NAME, "MS PowerPoint 97"));
    addFormat(ppt);
  }

  private void loadPptx() {

    final DocumentFormat pptx =
        new DocumentFormat(
            "Microsoft PowerPoint 2007-2013 XML",
            "pptx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    pptx.setInputFamily(DocumentFamily.PRESENTATION);
    pptx.setStoreProperties(
        DocumentFamily.PRESENTATION,
        Collections.singletonMap(KEY_FILTER_NAME, "Impress MS PowerPoint 2007 XML"));
    addFormat(pptx);
  }

  private void loadRtf() {

    final DocumentFormat rtf = new DocumentFormat("Rich Text Format", "rtf", "text/rtf");
    rtf.setInputFamily(DocumentFamily.TEXT);
    rtf.setStoreProperties(
        DocumentFamily.TEXT, Collections.singletonMap(KEY_FILTER_NAME, "Rich Text Format"));
    addFormat(rtf);
  }

  private void loadSvg() {

    final DocumentFormat svg =
        new DocumentFormat("Scalable Vector Graphics", "svg", "image/svg+xml");
    svg.setStoreProperties(
        DocumentFamily.DRAWING, Collections.singletonMap(KEY_FILTER_NAME, "draw_svg_Export"));
    addFormat(svg);
  }

  private void loadSwf() {

    final DocumentFormat swf =
        new DocumentFormat("Macromedia Flash", "swf", "application/x-shockwave-flash");
    swf.setStoreProperties(
        DocumentFamily.PRESENTATION,
        Collections.singletonMap(KEY_FILTER_NAME, "impress_flash_Export"));
    swf.setStoreProperties(
        DocumentFamily.DRAWING, Collections.singletonMap(KEY_FILTER_NAME, "draw_flash_Export"));
    addFormat(swf);
  }

  private void loadSxc() {

    final DocumentFormat sxc =
        new DocumentFormat("OpenOffice.org 1.0 Spreadsheet", "sxc", "application/vnd.sun.xml.calc");
    sxc.setInputFamily(DocumentFamily.SPREADSHEET);
    sxc.setStoreProperties(
        DocumentFamily.SPREADSHEET,
        Collections.singletonMap(KEY_FILTER_NAME, "StarOffice XML (Calc)"));
    addFormat(sxc);
  }

  private void loadSxi() {

    final DocumentFormat sxi =
        new DocumentFormat(
            "OpenOffice.org 1.0 Presentation", "sxi", "application/vnd.sun.xml.impress");
    sxi.setInputFamily(DocumentFamily.PRESENTATION);
    sxi.setStoreProperties(
        DocumentFamily.PRESENTATION,
        Collections.singletonMap(KEY_FILTER_NAME, "StarOffice XML (Impress)"));
    addFormat(sxi);
  }

  private void loadSxw() {

    final DocumentFormat sxw =
        new DocumentFormat(
            "OpenOffice.org 1.0 Text Document", "sxw", "application/vnd.sun.xml.writer");
    sxw.setInputFamily(DocumentFamily.TEXT);
    sxw.setStoreProperties(
        DocumentFamily.TEXT, Collections.singletonMap(KEY_FILTER_NAME, "StarOffice XML (Writer)"));
    addFormat(sxw);
  }

  private void loadTsv() {

    final DocumentFormat tsv =
        new DocumentFormat("Tab Separated Values", "tsv", "text/tab-separated-values");
    tsv.setInputFamily(DocumentFamily.SPREADSHEET);
    final Map<String, Object> tsvLoadAndStoreProperties = new LinkedHashMap<>();
    tsvLoadAndStoreProperties.put(KEY_FILTER_NAME, "Text - txt - csv (StarCalc)");
    tsvLoadAndStoreProperties.put(
        KEY_FILTER_OPTIONS, "9,34,0"); // Field Separator: '\t'; Text Delimiter: '"'
    tsv.setLoadProperties(tsvLoadAndStoreProperties);
    tsv.setStoreProperties(DocumentFamily.SPREADSHEET, tsvLoadAndStoreProperties);
    addFormat(tsv);
  }

  private void loadTxt() {

    final DocumentFormat txt = new DocumentFormat("Plain Text", "txt", "text/plain");
    txt.setInputFamily(DocumentFamily.TEXT);
    final Map<String, Object> txtLoadAndStoreProperties = new LinkedHashMap<>();
    txtLoadAndStoreProperties.put(KEY_FILTER_NAME, "Text (encoded)");
    txtLoadAndStoreProperties.put(KEY_FILTER_OPTIONS, "utf8");
    txt.setLoadProperties(txtLoadAndStoreProperties);
    txt.setStoreProperties(DocumentFamily.TEXT, txtLoadAndStoreProperties);
    addFormat(txt);
  }

  //private void loadWikitext() {
  //
  //final DocumentFormat wikitext =
  //new DocumentFormat("MediaWiki wikitext", "wiki", "text/x-wiki");
  ///wikitext.setStoreProperties(
  //DocumentFamily.TEXT, Collections.singletonMap(KEY_FILTER_NAME, "MediaWiki"));
  //addFormat(wikitext);
  //}

  private void loadWpd() {

    final DocumentFormat wpd = new DocumentFormat("WordPerfect", "wpd", "application/wordperfect");
    wpd.setInputFamily(DocumentFamily.TEXT);
    addFormat(wpd);
  }

  // private void loadXhtml() {
  //
  //final DocumentFormat xhtml = new DocumentFormat("XHTML", "xhtml", "application/xhtml+xml");
  //xhtml.setStoreProperties(
  //DocumentFamily.TEXT, Collections.singletonMap(KEY_FILTER_NAME, "XHTML Writer File"));
  //xhtml.setStoreProperties(
  //DocumentFamily.SPREADSHEET, Collections.singletonMap(KEY_FILTER_NAME, "XHTML Calc File"));
  //xhtml.setStoreProperties(
  //DocumentFamily.PRESENTATION, Collections.singletonMap(KEY_FILTER_NAME, "XHTML Impress File"));
  //addFormat(xhtml);
  //}

  private void loadXls() {

    final DocumentFormat xls =
        new DocumentFormat("Microsoft Excel 97-2003", "xls", "application/vnd.ms-excel");
    xls.setInputFamily(DocumentFamily.SPREADSHEET);
    xls.setStoreProperties(
        DocumentFamily.SPREADSHEET, Collections.singletonMap(KEY_FILTER_NAME, "MS Excel 97"));
    addFormat(xls);
  }

  private void loadXlsx() {

    final DocumentFormat xlsx =
        new DocumentFormat(
            "Microsoft Excel 2007-2013 XML",
            "xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    xlsx.setInputFamily(DocumentFamily.SPREADSHEET);
    xlsx.setStoreProperties(
        DocumentFamily.SPREADSHEET,
        Collections.singletonMap(KEY_FILTER_NAME, "Calc MS Excel 2007 XML"));
    addFormat(xlsx);
  }
}
