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

package org.jodconverter.local.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNamed;
import com.sun.star.datatransfer.XTransferable;
import com.sun.star.datatransfer.XTransferableSupplier;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.frame.XController;
import com.sun.star.lang.XComponent;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.text.XPageCursor;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.view.XSelectionSupplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.local.office.LocalOfficeUtils;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Props;

/**
 * This filter is used to select specific pages from a document in order to convert only the
 * selected pages.
 */
public class PagesSelectorFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(PagesSelectorFilter.class);

  // This class has been inspired by these examples:
  // https://wiki.openoffice.org/wiki/API/Tutorials/PDF_export
  // https://blog.oio.de/2010/10/27/copy-and-paste-without-clipboard-using-openoffice-org-api

  private final List<Integer> pages;

  /**
   * Creates a new filter that will select the specified pages while converting a document (only the
   * given pages will be converted).
   *
   * @param pages The page numbers of the page to convert. First page is index 1.
   */
  public PagesSelectorFilter(final @NonNull Integer... pages) {
    this(Stream.of(pages).collect(Collectors.toSet()));
  }

  /**
   * Creates a new filter that will select the specified pages while converting a document (only the
   * given pages will be converted).
   *
   * @param pages The page numbers of the page to convert. First page is index 1.
   */
  public PagesSelectorFilter(final @NonNull Set<@NonNull Integer> pages) {
    super();

    AssertUtils.notEmpty(pages, "pages must not be null nor empty");

    this.pages = new ArrayList<>(pages);
  }

  @Override
  public void doFilter(
      final @NonNull OfficeContext context,
      final @NonNull XComponent document,
      final @NonNull FilterChain chain)
      throws Exception {

    final DocumentFamily family = LocalOfficeUtils.getDocumentFamilySilently(document);
    if (family != null) {

      switch (family) {
        case TEXT:
          LOGGER.debug("Applying the PagesSelectorFilter for a Text document");

          // We must process from the start to the end.
          Collections.sort(pages);
          selectTextPages(Lo.qi(XTextDocument.class, document));
          break;
        case SPREADSHEET:
          LOGGER.debug("Applying the PagesSelectorFilter for a Calc document");

          // We must process from the end to the start.
          selectSheets(Lo.qi(XSpreadsheetDocument.class, document));
          break;
        case PRESENTATION:
        case DRAWING:
          LOGGER.debug(
              "Applying the PagesSelectorFilter for a {} document",
              family == DocumentFamily.DRAWING ? "Draw" : "Impress");

          // We must process from the end to the start.
          pages.sort(Collections.reverseOrder());
          selectDrawPages(Lo.qi(XDrawPagesSupplier.class, document));
          break;
      }
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private void copyPage(final XTextDocument docText, final int source, final int target)
      throws Exception {

    if (source == target) {
      return;
    }

    final XController ctrl = docText.getCurrentController();

    // Get the text cursor for the document.
    final XTextCursor tc = docText.getText().createTextCursor();

    // Get the view cursor for the document. We also need a page cursor
    // on the view cursor to navigate through the document pages.
    final XTextViewCursor vc = Lo.qi(XTextViewCursorSupplier.class, ctrl).getViewCursor();
    final XPageCursor pc = Lo.qi(XPageCursor.class, vc);

    // Reset both cursors to the beginning of the document
    tc.gotoStart(false);
    vc.gotoStart(false);

    // Jump to the source page to select (first page is 1) and move the
    // text cursor to the beginning of this page.
    pc.jumpToPage((short) source);
    tc.gotoRange(vc.getStart(), false);

    // Jump to the end of the source page and expand the text cursor
    // to the end of this page, while selecting text in between.
    pc.jumpToEndOfPage();
    tc.gotoRange(vc.getStart(), true);

    // Select the source page.
    final XSelectionSupplier selectionSupplier = Lo.qi(XSelectionSupplier.class, ctrl);
    selectionSupplier.select(tc);

    // Copy the selection (whole source page).
    final XTransferableSupplier transferableSupplier = Lo.qi(XTransferableSupplier.class, ctrl);
    final XTransferable xTransferable = transferableSupplier.getTransferable();

    // Now select the target page.
    tc.gotoStart(false);
    vc.gotoStart(false);
    pc.jumpToPage((short) target);
    tc.gotoRange(vc.getStart(), false);
    pc.jumpToEndOfPage();
    tc.gotoRange(vc.getStart(), true);
    selectionSupplier.select(tc);

    // Paste the source page into the target page. This will replace the
    // target page with the source page.
    transferableSupplier.insertTransferable(xTransferable);
  }

  private void selectTextPages(final XTextDocument doc) throws Exception {

    final XController ctrl = doc.getCurrentController();

    // Save the PageCount property of the document.
    final int pageCount = (Integer) Props.getProperty(ctrl, "PageCount");

    // Delete all the pages except the ones to select.
    int nextTargetPage = 1;
    for (final int page : pages) {
      // Ignore invalid page
      if (page > 0 && page <= pageCount) {
        copyPage(doc, page, nextTargetPage++);
      }
    }

    // Once done, we must delete the pages after that last copied page.
    final int lastPage = nextTargetPage - 1;

    // Get the text cursor for the document.
    final XTextCursor tc = doc.getText().createTextCursor();

    // Get the view cursor for the document. We also need a page cursor
    // on the view cursor to navigate through the document pages.
    final XTextViewCursor vc = Lo.qi(XTextViewCursorSupplier.class, ctrl).getViewCursor();
    final XPageCursor pc = Lo.qi(XPageCursor.class, vc);

    // Reset both cursors to the beginning of the document
    tc.gotoStart(false);
    vc.gotoStart(false);

    // Jump to the and of the last copied page and move the text cursor to
    // the beginning of this page, while selecting text in between.
    pc.jumpToPage((short) lastPage);
    // tc.gotoRange(vc.getStart(), true);
    pc.jumpToEndOfPage();
    tc.gotoRange(vc.getEnd(), true);
    // Select the pages.
    final XSelectionSupplier selectionSupplier = Lo.qi(XSelectionSupplier.class, ctrl);
    selectionSupplier.select(tc);

    // Copy the selection (pages).
    final XTransferableSupplier transSupplier = Lo.qi(XTransferableSupplier.class, ctrl);
    final XTransferable trans = transSupplier.getTransferable();

    // Now select the whole document.
    tc.gotoStart(false); // Go to the start
    tc.gotoEnd(true); // Go to the end, expanding the cursor's text range
    selectionSupplier.select(tc);

    // Paste the previously copied pages. This will replace the current selection,
    // which is the whole document, by the previously selected pages.
    transSupplier.insertTransferable(trans);
  }

  private void selectSheets(final XSpreadsheetDocument doc) throws Exception {

    final XSpreadsheets sheets = doc.getSheets();
    final XIndexAccess indexedSheets = Lo.qi(XIndexAccess.class, sheets);

    // Delete all the sheets except the ones to select.
    final int count = indexedSheets.getCount();
    for (int i = count - 1; i >= 0; i--) {
      final XSpreadsheet sheet = Lo.qi(XSpreadsheet.class, indexedSheets.getByIndex(i));
      final XNamed namedSheet = Lo.qi(XNamed.class, sheet);
      if (!pages.contains(i + 1)) {
        sheets.removeByName(namedSheet.getName());
      }
    }
  }

  private void selectDrawPages(final XDrawPagesSupplier supplier) throws Exception {

    final XDrawPages drawPages = supplier.getDrawPages();
    final int pageCount = drawPages.getCount();

    // Delete all the pages except the ones to select.
    for (int i = pageCount; i > 0; i--) {
      if (!pages.contains(i)) {
        drawPages.remove(Lo.qi(XDrawPage.class, drawPages.getByIndex(i - 1)));
      }
    }
  }
}
