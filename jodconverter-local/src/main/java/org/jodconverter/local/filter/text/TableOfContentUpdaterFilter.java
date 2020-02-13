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

package org.jodconverter.local.filter.text;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XDocumentIndex;
import com.sun.star.text.XDocumentIndexesSupplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Write;

/** This filter update all indexes in a document. */
public class TableOfContentUpdaterFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TableOfContentUpdaterFilter.class);

  // This class has been inspired by these examples:
  // https://forum.openoffice.org/en/forum/viewtopic.php?f=25&t=54982#

  private final int level;

  /** Creates a new filter that will update the table of content. */
  public TableOfContentUpdaterFilter() {
    this(0);
  }

  /**
   * Creates a new filter that will change the number of levels of the table of content and will
   * update it.
   *
   * @param level The desired number of levels in the table of content.
   */
  public TableOfContentUpdaterFilter(final int level) {
    super();

    this.level = level;
  }

  @Override
  public void doFilter(
      @NonNull final OfficeContext context,
      @NonNull final XComponent document,
      @NonNull final FilterChain chain)
      throws Exception {

    LOGGER.debug("Applying the TableOfContentUpdaterFilter");

    // This filter can only be used with text document
    if (Write.isText(document)) {
      updateToc(document);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private void updateToc(final XComponent document) throws Exception {

    // Get the DocumentIndexesSupplier interface of the document
    final XDocumentIndexesSupplier documentIndexesSupplier =
        Lo.qi(XDocumentIndexesSupplier.class, document);

    // Get an XIndexAccess of DocumentIndexes
    final XIndexAccess documentIndexes =
        Lo.qi(XIndexAccess.class, documentIndexesSupplier.getDocumentIndexes());

    for (int i = 0; i < documentIndexes.getCount(); i++) {

      // Update each index
      final XDocumentIndex docIndex = Lo.qi(XDocumentIndex.class, documentIndexes.getByIndex(i));

      // Update the level if required
      if (level > 0) {

        // Get the service interface of the ContentIndex
        final String indexType = docIndex.getServiceName();

        if (indexType.contains("com.sun.star.text.ContentIndex")) {

          final XPropertySet index = Lo.qi(XPropertySet.class, docIndex);

          // Set TOC levels
          index.setPropertyValue("Level", (short) level);
        }
      }

      docIndex.update();
    }
  }
}
