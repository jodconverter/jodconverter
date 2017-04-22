/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.filter.text;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;

import org.artofsolving.jodconverter.filter.Filter;
import org.artofsolving.jodconverter.filter.FilterChain;
import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

/** This filter is used to replace text in a document. */
public class TextReplacerFilter implements Filter {

  // This class has been inspired by this example:
  // http://api.libreoffice.org/examples/java/Text/TextReplace.java

  private static final Logger logger = LoggerFactory.getLogger(TextReplacerFilter.class);

  private final String[] searchList;
  private final String[] replacementList;

  /**
   * Creates a new filter with the specified strings to replace.
   *
   * @param searchList the Strings to search for, no-op if null.
   * @param replacementList the Strings to replace them with, no-op if null.
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

    try {
      final XReplaceable replaceable = UnoRuntime.queryInterface(XReplaceable.class, document);

      // We need a descriptor to set properties for Replace
      final XReplaceDescriptor replaceDescr = replaceable.createReplaceDescriptor();

      logger.debug("Changing all occurrences of ...");
      for (int i = 0; i < searchList.length; i++) {
        logger.debug("{} -> {}", searchList[i], replacementList[i]);
        // Set the properties the replace method need
        replaceDescr.setSearchString(searchList[i]);
        replaceDescr.setReplaceString(replacementList[i]);

        // Replace all words
        replaceable.replaceAll(replaceDescr);
      }

    } catch (Exception e) {
      logger.error("Error", e);
    }

    chain.doFilter(context, document);
  }
}
