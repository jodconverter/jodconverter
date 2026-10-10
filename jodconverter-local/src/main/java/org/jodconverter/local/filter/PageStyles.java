/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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
import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.XComponent;
import com.sun.star.style.XStyleFamiliesSupplier;

import org.jodconverter.local.office.utils.Info;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Write;

/** Gives access to the page styles of a document, for the filters that change them. */
final class PageStyles {

  private static final String FAMILY = "PageStyles";

  /**
   * Tells whether the pages of the given document are described by page styles, which is the case
   * of the text documents and of the spreadsheets. The pages of a presentation or of a drawing are
   * the slides themselves.
   *
   * @param document The document.
   * @return {@code true} if the document has page styles, {@code false} otherwise.
   */
  /* default */ static boolean isSupported(final XComponent document) {
    return Write.isText(document) || Info.isDocumentType(document, Lo.CALC_SERVICE);
  }

  /**
   * Gets the properties of every page style of the given document, the ones in use and the others:
   * a spreadsheet may have one page style per sheet, and a text document one per section.
   *
   * @param document The document, for which {@link #isSupported(XComponent)} is {@code true}.
   * @return The property sets of the page styles.
   * @throws Exception If the styles cannot be read.
   */
  /* default */ static List<XPropertySet> of(final XComponent document) throws Exception {

    final var supplier = Lo.qi(XStyleFamiliesSupplier.class, document);
    final var family = Lo.qi(XNameAccess.class, supplier.getStyleFamilies().getByName(FAMILY));
    final var styles = new ArrayList<XPropertySet>();
    for (final var name : family.getElementNames()) {
      styles.add(Lo.qi(XPropertySet.class, family.getByName(name)));
    }
    return styles;
  }

  private PageStyles() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
