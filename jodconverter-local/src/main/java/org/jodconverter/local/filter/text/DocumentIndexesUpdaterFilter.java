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

package org.jodconverter.local.filter.text;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XDocumentIndex;
import com.sun.star.text.XDocumentIndexesSupplier;
import com.sun.star.util.XRefreshable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Write;

/**
 * Updates the indexes of a text document before it is stored: the table of contents, the
 * alphabetical index, the table of figures, the bibliography... A document is saved with the
 * indexes as they were last updated, so an export without this filter shows the headings and the
 * page numbers of that time.
 *
 * <p>The update is done twice, with a refresh of the layout before each pass: the first pass
 * rebuilds the indexes, which can change the pagination (a table of contents that grows by a page),
 * and the second pass fixes the page numbers for the new layout.
 *
 * <p>The number of levels of the tables of contents can be changed at the same time. Other
 * documents than text documents are left untouched.
 */
public class DocumentIndexesUpdaterFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentIndexesUpdaterFilter.class);

  private static final String CONTENT_INDEX_SERVICE = "com.sun.star.text.ContentIndex";

  private final int level;

  /** Creates a new filter that updates the indexes. */
  public DocumentIndexesUpdaterFilter() {
    this(0);
  }

  /**
   * Creates a new filter that updates the indexes, and changes the number of levels of the tables
   * of contents.
   *
   * @param level The number of levels of the tables of contents; 0 keeps the levels of the
   *     document.
   */
  public DocumentIndexesUpdaterFilter(final int level) {
    super();
    this.level = level;
  }

  @Override
  public void doFilter(
      final @NonNull OfficeContext context,
      final @NonNull XComponent document,
      final @NonNull FilterChain chain)
      throws Exception {

    // This filter can only be used with text document
    if (Write.isText(document)) {
      LOGGER.debug("Applying the DocumentIndexesUpdaterFilter");
      updateIndexes(document);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private void updateIndexes(final XComponent document) throws Exception {

    final var refreshable = Lo.qiOptional(XRefreshable.class, document);
    final var indexes =
        Lo.qi(
            XIndexAccess.class,
            Lo.qi(XDocumentIndexesSupplier.class, document).getDocumentIndexes());

    // Two passes: the first one rebuilds the indexes, which can change the pagination; the second
    // one updates the page numbers for the new layout.
    for (var pass = 0; pass < 2; pass++) {
      refreshable.ifPresent(XRefreshable::refresh);
      for (var i = 0; i < indexes.getCount(); i++) {
        final var index = Lo.qi(XDocumentIndex.class, indexes.getByIndex(i));
        if (pass == 0 && level > 0 && index.getServiceName().contains(CONTENT_INDEX_SERVICE)) {
          Lo.qi(XPropertySet.class, index).setPropertyValue("Level", (short) level);
        }
        index.update();
      }
    }
  }
}
