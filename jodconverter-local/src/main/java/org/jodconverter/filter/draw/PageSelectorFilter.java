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

package org.jodconverter.filter.draw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.lang.XComponent;

import org.jodconverter.filter.Filter;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.utils.Draw;
import org.jodconverter.office.utils.Lo;

/**
 * This filter is used to select a specific page from a document in order to convert only the
 * selected page.
 */
public class PageSelectorFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageSelectorFilter.class);

  // This class has been inspired by these examples:
  // https://wiki.openoffice.org/wiki/API/Tutorials/PDF_export
  // https://blog.oio.de/2010/10/27/copy-and-paste-without-clipboard-using-openoffice-org-api

  private final int page;

  /**
   * Creates a new filter that will select the specified page while converting a document (only the
   * given page will be converted).
   *
   * @param page The page number to convert.
   */
  public PageSelectorFilter(final int page) {
    super();

    this.page = page;
  }

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws Exception {

    LOGGER.debug("Applying the PageSelectorFilter");

    // This filter can only be used with draw document
    if (Draw.isDraw(document)) {
      selectPage(document);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private void selectPage(final XComponent document) throws Exception {

    final XDrawPages drawPages = Lo.qi(XDrawPagesSupplier.class, document).getDrawPages();
    final int pageCount = drawPages.getCount();

    // Delete all the pages except the one to select.
    int seekIdx = Math.min(pageCount, Math.max(0, page - 1));
    for (int i = 0; i < pageCount; i++) {
      XDrawPage drawPage = null;
      if (i < seekIdx) {
        drawPage = Lo.qi(XDrawPage.class, drawPages.getByIndex(0));
      } else if (i > seekIdx) {
        drawPage = Lo.qi(XDrawPage.class, drawPages.getByIndex(1));
      }
      if (drawPage != null) {
        drawPages.remove(drawPage);
      }
    }
  }
}
