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

package org.jodconverter.filter.text;

import com.sun.star.datatransfer.XTransferable;
import com.sun.star.datatransfer.XTransferableSupplier;
import com.sun.star.frame.XController;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XPageCursor;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.XSelectionSupplier;

import org.jodconverter.filter.Filter;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeContext;

/**
 * This filter is used to select a specific page from a document in order to convert only the
 * selected page.
 */
public class PageSelectorFilter implements Filter {

  // This class has been inspired by these examples:
  // https://wiki.openoffice.org/wiki/API/Tutorials/PDF_export
  // https://blog.oio.de/2010/10/27/copy-and-paste-without-clipboard-using-openoffice-org-api

  private final short page;

  /**
   * Creates a new filter that will select the specified page while converting a document (only the
   * given page will be converted).
   *
   * @param page The page number to convert.
   */
  public PageSelectorFilter(final short page) {
    super();

    this.page = page;
  }

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws Exception {

    // Querying for the interface XTextDocument (text interface) on the XComponent.
    final XTextDocument docText = UnoRuntime.queryInterface(XTextDocument.class, document);

    // We need both the text cursor and the view cursor in order
    // to select the whole content of the desired page.
    final XController xController = docText.getCurrentController();
    final XTextCursor xTextCursor = docText.getText().createTextCursor();
    final XTextViewCursor xTextViewCursor =
        UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController).getViewCursor();

    // Reset both cursors to the beginning of the document
    xTextCursor.gotoStart(false);
    xTextViewCursor.gotoStart(false);

    // Querying for the interface XPageCursor on the view cursor.
    final XPageCursor xPageCursor = UnoRuntime.queryInterface(XPageCursor.class, xTextViewCursor);

    // Jump to the page to select (first page is 1) and move the
    // text cursor to the beginning of this page.
    xPageCursor.jumpToPage(page);
    xTextCursor.gotoRange(xTextViewCursor.getStart(), false);

    // Jump to the end of the page and expand the text cursor
    // to the end of this page.
    xPageCursor.jumpToEndOfPage();
    xTextCursor.gotoRange(xTextViewCursor.getStart(), true);

    // Select the whole page.
    final XSelectionSupplier selectionSupplier =
        UnoRuntime.queryInterface(XSelectionSupplier.class, xController);
    selectionSupplier.select(xTextCursor);

    // Copy the selection (whole page).
    final XTransferableSupplier xTransferableSupplier =
        UnoRuntime.queryInterface(XTransferableSupplier.class, xController);
    final XTransferable xTransferable = xTransferableSupplier.getTransferable();

    // Now select the whole document.
    xTextCursor.gotoStart(false); // Go to the start
    xTextCursor.gotoEnd(true); // Go to the end, expanding the cursor's text range
    selectionSupplier.select(xTextCursor);

    // Paste the previously copied page. This will replace the
    // current selection (the whole document).
    xTransferableSupplier.insertTransferable(xTransferable);
  }
}
