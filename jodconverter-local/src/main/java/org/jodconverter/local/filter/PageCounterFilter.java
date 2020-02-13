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

import java.util.Objects;

import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.sheet.XSpreadsheetDocument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.local.office.utils.Calc;
import org.jodconverter.local.office.utils.Draw;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Props;
import org.jodconverter.local.office.utils.Write;

/** This filter is used to count the number of pages of a document. */
public class PageCounterFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageCounterFilter.class);

  private int pageCount;

  @Override
  public void doFilter(
      @NonNull final OfficeContext context,
      @NonNull final XComponent document,
      @NonNull final FilterChain chain)
      throws Exception {

    if (Write.isText(document)) {
      LOGGER.debug("Applying the PageCounterFilter for a Text document");

      pageCount =
          (Integer)
              Props.getProperty(Lo.qi(XModel.class, document).getCurrentController(), "PageCount");

    } else if (Calc.isCalc(document)) {
      LOGGER.debug("Applying the PageCounterFilter for a Calc document");

      final XSpreadsheetDocument doc = Calc.getCalcDoc(document);
      pageCount = Objects.requireNonNull(doc).getSheets().getElementNames().length;

    } else if (Draw.isImpress(document)) {
      LOGGER.debug("Applying the PageCounterFilter for an Impress document");

      final XDrawPages xDrawPages = Lo.qi(XDrawPagesSupplier.class, document).getDrawPages();
      pageCount = xDrawPages.getCount();

    } else if (Draw.isDraw(document)) {
      LOGGER.debug("Applying the PageCounterFilter for a Draw document");

      final XDrawPages xDrawPages = Lo.qi(XDrawPagesSupplier.class, document).getDrawPages();
      pageCount = xDrawPages.getCount();
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
