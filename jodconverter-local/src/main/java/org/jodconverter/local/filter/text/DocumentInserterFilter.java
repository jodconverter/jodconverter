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

import static org.jodconverter.local.office.LocalOfficeUtils.toUrl;

import java.io.File;

import com.sun.star.beans.PropertyValue;
import com.sun.star.document.XDocumentInsertable;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Write;

/** This filter is used to insert a document at the end of the document being converted. */
public class DocumentInserterFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentInserterFilter.class);

  private final File documentToInsert;

  /**
   * Creates a new filter that will insert the specified document.
   *
   * @param document The document to insert at the end of the current document.
   */
  public DocumentInserterFilter(final @NonNull File document) {
    super();

    this.documentToInsert = document;
  }

  @Override
  public void doFilter(
      final @NonNull OfficeContext context,
      final @NonNull XComponent document,
      final @NonNull FilterChain chain)
      throws Exception {

    // This filter can only be used with text document
    final XTextDocument docText = Write.getTextDoc(document);
    if (docText != null) {
      LOGGER.debug("Applying the DocumentInserterFilter");
      insertDocument(docText);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private void insertDocument(final XTextDocument document) throws Exception {

    // We need the text cursor in order to go to the end of the document.
    final XTextCursor textCursor = document.getText().createTextCursor();

    // Go to the end of the document
    textCursor.gotoEnd(false);

    // Insert the document to merge at the end of the current document.
    // TODO: Should we allow custom load properties ?
    final XDocumentInsertable insertable = Lo.qi(XDocumentInsertable.class, textCursor);
    insertable.insertDocumentFromURL(toUrl(documentToInsert), new PropertyValue[0]);
  }
}
