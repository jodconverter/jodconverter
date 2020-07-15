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

import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.sheet.XSpreadsheetDocument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.local.office.LocalOfficeUtils;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Props;

/** This filter is used to count the number of pages of a document. */
public class PageCounterFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageCounterFilter.class);

  private int pageCount;

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
          LOGGER.debug("Applying the PageCounterFilter for a Text document");
          pageCount =
              (Integer)
                  Props.getProperty(
                      Lo.qi(XModel.class, document).getCurrentController(), "PageCount");
          break;
        case SPREADSHEET:
          LOGGER.debug("Applying the PageCounterFilter for a Calc document");
          pageCount =
              Lo.qi(XSpreadsheetDocument.class, document).getSheets().getElementNames().length;
          break;
        case PRESENTATION:
        case DRAWING:
          LOGGER.debug(
              "Applying the PageCounterFilter for a {} document",
              family == DocumentFamily.DRAWING ? "Draw" : "Impress");
          final XDrawPages xDrawPages = Lo.qi(XDrawPagesSupplier.class, document).getDrawPages();
          pageCount = xDrawPages.getCount();
          break;
      }
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  /**
   * Gets the number of pages within the document when the filter has been invoked.
   *
   * @return The number of pages.
   */
  public int getPageCount() {

    return this.pageCount;
  }
}
