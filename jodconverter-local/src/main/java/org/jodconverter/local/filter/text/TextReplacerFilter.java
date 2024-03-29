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

import java.util.Arrays;

import com.sun.star.lang.XComponent;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Write;

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
  public TextReplacerFilter(
      final @NonNull String[] searchList, final @NonNull String[] replacementList) {
    super();

    // Both arrays are required and cannot be empty
    AssertUtils.notEmpty(searchList, "searchList must not be null nor empty");
    AssertUtils.notEmpty(replacementList, "replacementList must not be null nor empty");

    // Make sure lengths are ok, these need to be equal
    final int searchLength = searchList.length;
    final int replacementLength = replacementList.length;
    AssertUtils.isTrue(
        searchLength == replacementLength,
        String.format(
            "search array length [%s] and replacement array length [%s] don't match",
            searchLength, replacementLength));

    // Everything is fine
    this.searchList = Arrays.copyOf(searchList, searchList.length);
    this.replacementList = Arrays.copyOf(replacementList, replacementList.length);
  }

  @Override
  public void doFilter(
      final @NonNull OfficeContext context,
      final @NonNull XComponent document,
      final @NonNull FilterChain chain)
      throws OfficeException {

    LOGGER.debug("Applying the TextReplacerFilter");

    // This filter can only be used with text document
    if (Write.isText(document)) {
      replaceText(document);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private void replaceText(final @NonNull XComponent document) {

    final XReplaceable replaceable = Lo.qi(XReplaceable.class, document);

    // We need a descriptor to set properties for Replace
    final XReplaceDescriptor replaceDesc = replaceable.createReplaceDescriptor();

    LOGGER.debug("Changing all occurrences of ...");
    for (int i = 0; i < searchList.length; i++) {
      LOGGER.debug("{} -> {}", searchList[i], replacementList[i]);
      // Set the properties the replace method need
      replaceDesc.setSearchString(searchList[i]);
      replaceDesc.setReplaceString(replacementList[i]);

      // Replace all words
      replaceable.replaceAll(replaceDesc);
    }
  }
}
