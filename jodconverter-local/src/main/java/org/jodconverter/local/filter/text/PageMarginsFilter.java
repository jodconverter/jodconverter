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
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.XComponent;
import com.sun.star.style.XStyle;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Write;

/** This filter is used to set the margins of the document being converted. */
public class PageMarginsFilter implements Filter {

  // This class has been inspired by these examples:
  // https://wiki.openoffice.org/wiki/Translating_Java_Code_in_cpp
  // https://github.com/bomm/thera-pi-2/blob/master/RehaBillEdit/src/Tools/OOTools.java#L328

  private static final Logger LOGGER = LoggerFactory.getLogger(PageMarginsFilter.class);

  private final Integer topMargin;
  private final Integer rightMargin;
  private final Integer bottomMargin;
  private final Integer leftMargin;

  /**
   * Creates a new filter that will set the margins of the document.
   *
   * @param leftMargin The left margin (millimeters), may be null. If null, the left margin does not
   *     change.
   * @param topMargin The top margin (millimeters), may be null. If null, the top margin does not
   *     change.
   * @param rightMargin The right margin (millimeters), may be null. If null, the right margin does
   *     not change.
   * @param bottomMargin The bottom margin (millimeters), may be null. If null, the bottom margin
   *     does not change.
   */
  public PageMarginsFilter(
      final @Nullable Integer leftMargin,
      final @Nullable Integer topMargin,
      final @Nullable Integer rightMargin,
      final @Nullable Integer bottomMargin) {
    super();

    this.leftMargin = leftMargin;
    this.topMargin = topMargin;
    this.rightMargin = rightMargin;
    this.bottomMargin = bottomMargin;
  }

  @Override
  public void doFilter(
      final @NonNull OfficeContext context,
      final @NonNull XComponent document,
      final @NonNull FilterChain chain)
      throws Exception {

    // This filter can only be used with text document
    if (Write.isText(document)) {
      LOGGER.debug("Applying the PageMarginsFilter");
      setMargins(Lo.qi(XTextDocument.class, document));
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private void setMargins(final XTextDocument document) throws Exception {

    // Create a text cursor from the cells XText interface
    final XTextCursor xTextCursor = document.getText().createTextCursor();

    // Get the property set of the cell's TextCursor
    final XPropertySet xTextCursorProps = Lo.qi(XPropertySet.class, xTextCursor);

    // Get the Page Style name at the cursor position
    final String pageStyleName = xTextCursorProps.getPropertyValue("PageStyleName").toString();

    // Get the StyleFamiliesSupplier interface of the document
    final XStyleFamiliesSupplier xSupplier = Lo.qi(XStyleFamiliesSupplier.class, document);

    // Use the StyleFamiliesSupplier interface to get the XNameAccess interface of the
    // actual style families
    final XNameAccess xFamilies = Lo.qi(XNameAccess.class, xSupplier.getStyleFamilies());

    // Access the 'PageStyles' Family
    final XNameContainer xFamily = Lo.qi(XNameContainer.class, xFamilies.getByName("PageStyles"));

    // Get the style of the current page from the PageStyles family
    final XStyle xStyle = Lo.qi(XStyle.class, xFamily.getByName(pageStyleName));

    //
    // We could also just bet that the Standard style is used. If this is what we want,
    // uncomment the following line, and remove the ones (used to get the pageStyleName)
    // that will no longer be required.
    //

    // Get the "Standard" style from the PageStyles family
    // XStyle xStyle = Lo.qi(XStyle.class, xFamily.getByName("Standard"));

    LOGGER.debug(
        "Changing margins using: [left={}, top={}, right={}, bottom={}]",
        leftMargin,
        topMargin,
        rightMargin,
        bottomMargin);

    // Get the property set of the style
    final XPropertySet xStyleProps = Lo.qi(XPropertySet.class, xStyle);

    // Change the margins (1 = 0.01 mm)
    if (leftMargin != null) {
      xStyleProps.setPropertyValue("LeftMargin", leftMargin * 100);
    }
    if (topMargin != null) {
      xStyleProps.setPropertyValue("TopMargin", topMargin * 100);
    }
    if (rightMargin != null) {
      xStyleProps.setPropertyValue("RightMargin", rightMargin * 100);
    }
    if (bottomMargin != null) {
      xStyleProps.setPropertyValue("BottomMargin", bottomMargin * 100);
    }
  }
}
