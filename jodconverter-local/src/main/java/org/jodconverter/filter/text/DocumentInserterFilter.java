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

import static org.jodconverter.office.LocalOfficeUtils.toUrl;

import java.io.File;

import com.sun.star.beans.PropertyValue;
import com.sun.star.document.XDocumentInsertable;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;

import org.jodconverter.filter.Filter;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeContext;

/** This filter is used to insert a document at the end of the document being converted. */
public class DocumentInserterFilter implements Filter {

  private File documentToInsert;

  /**
   * Creates a new filter that will insert the specified document.
   *
   * @param document The document to insert at the end of the current document.
   */
  public DocumentInserterFilter(final File document) {
    super();

    this.documentToInsert = document;
  }

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws Exception {

    // Querying for the interface XTextDocument (text interface) on the XComponent.
    final XTextDocument docText = UnoRuntime.queryInterface(XTextDocument.class, document);

    // We need the text cursor in order to go to the end of the document.
    final XTextCursor textCursor = docText.getText().createTextCursor();

    // Go to the end of the document
    textCursor.gotoEnd(false);

    // Insert the document to merge at the end of the current document.
    // TODO: Should we allow custom load properties ?
    XDocumentInsertable insertable =
        UnoRuntime.queryInterface(XDocumentInsertable.class, textCursor);
    insertable.insertDocumentFromURL(toUrl(documentToInsert), new PropertyValue[0]);

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }
}
