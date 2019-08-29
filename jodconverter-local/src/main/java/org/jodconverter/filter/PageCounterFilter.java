/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2019 Simon Braconnier and contributors
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

package org.jodconverter.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;

import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.utils.Calc;
import org.jodconverter.office.utils.Draw;
import org.jodconverter.office.utils.Lo;
import org.jodconverter.office.utils.Props;
import org.jodconverter.office.utils.Write;

/** This filter is used to count the number of pages of a document. */
public class PageCounterFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageCounterFilter.class);

  private int pageCount;

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws Exception {

    if (Write.isText(document)) {

      // Save the PageCount property of the document.
      pageCount =
          (Integer)
              Props.getProperty(Lo.qi(XModel.class, document).getCurrentController(), "PageCount")
                  .orElse(0);
    } else if (Calc.isCalc(document)) {

      // Not supported
      throw new UnsupportedOperationException("SpreadsheetDocument not supported yet");

    } else if (Draw.isImpress(document)) {

      // Not Supported
      throw new UnsupportedOperationException("PresentationDocument not supported yet");

    } else if (Draw.isDraw(document)) {

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
