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

import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XPageCursor;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;

import org.jodconverter.filter.Filter;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeContext;

/** This filter is used to count the number of pages of a document. */
public class PageCounterFilter implements Filter {

  private short pageCount;

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws Exception {

    // We need both page cursor in order to jump to the last page of a document.
    XTextViewCursor viewCursor =
        UnoRuntime.queryInterface(
                XTextViewCursorSupplier.class,
                UnoRuntime.queryInterface(XModel.class, document).getCurrentController())
            .getViewCursor();
    XPageCursor pageCursor = UnoRuntime.queryInterface(XPageCursor.class, viewCursor);

    // Jump to the last page.
    pageCursor.jumpToLastPage();

    // Save the number of the page within the document at
    // the page cursor position.
    pageCount = pageCursor.getPage();

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  /**
   * Gets the number of pages within the document when the filter has been invoked.
   *
   * @return The number of pages.
   */
  public short getPageCount() {

    return this.pageCount;
  }
}
