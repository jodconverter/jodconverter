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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;

import org.jodconverter.filter.Filter;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;

/** This filter is used to replace text in a document. */
public class TextReplacerFilter implements Filter {

  // This class has been inspired by this example:
  // http://api.libreoffice.org/examples/java/Text/TextReplace.java

  private static final Logger LOGGER = LoggerFactory.getLogger(TextReplacerFilter.class);

  private final String[] searchList;
  private final String[] replacementList;

  /**
   * Creates a new filter with the specified strings to replace.
   *
   * @param searchList The Strings to search for, no-op if null.
   * @param replacementList The Strings to replace them with, no-op if null.
   */
  public TextReplacerFilter(final String[] searchList, final String[] replacementList) {
    super();

    // Both arrays are required and cannot be empty
    Validate.notEmpty(searchList, "Search list is empty");
    Validate.notEmpty(replacementList, "Replacement list is empty");

    // Make sure lengths are ok, these need to be equal
    final int searchLength = searchList.length;
    final int replacementLength = replacementList.length;
    Validate.isTrue(
        searchList.length == replacementList.length,
        "Search and Replace array lengths don't match: "
            + searchLength
            + " vs "
            + replacementLength);

    // Everything is fine
    this.searchList = ArrayUtils.clone(searchList);
    this.replacementList = ArrayUtils.clone(replacementList);
  }

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws OfficeException {

    final XReplaceable replaceable = UnoRuntime.queryInterface(XReplaceable.class, document);

    // We need a descriptor to set properties for Replace
    final XReplaceDescriptor replaceDescr = replaceable.createReplaceDescriptor();

    LOGGER.debug("Changing all occurrences of ...");
    for (int i = 0; i < searchList.length; i++) {
      LOGGER.debug("{} -> {}", searchList[i], replacementList[i]);
      // Set the properties the replace method need
      replaceDescr.setSearchString(searchList[i]);
      replaceDescr.setReplaceString(replacementList[i]);

      // Replace all words
      replaceable.replaceAll(replaceDescr);
    }

    chain.doFilter(context, document);
  }
}
