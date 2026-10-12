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

import com.sun.star.lang.XComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;

/**
 * This filter removes the page header, the page footer or both from the document being converted,
 * by switching them off in its page styles.
 *
 * <p>Its first use is the conversion of a CSV file, or of any spreadsheet that has no page setup of
 * its own: the office prints the name of the sheet (the name of the file, for a CSV file) at the
 * top of each page and the page number at the bottom, which are rarely wanted in a PDF.
 *
 * <p>The filter applies to spreadsheets and to text documents; other documents are left as they
 * are.
 */
public class HeaderFooterRemoverFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeaderFooterRemoverFilter.class);

  private final boolean header;
  private final boolean footer;

  /** Creates a new filter that will remove both the header and the footer of the pages. */
  public HeaderFooterRemoverFilter() {
    this(true, true);
  }

  /**
   * Creates a new filter that will remove the header, the footer or both.
   *
   * @param header {@code true} to remove the header of the pages, {@code false} to keep it.
   * @param footer {@code true} to remove the footer of the pages, {@code false} to keep it.
   */
  public HeaderFooterRemoverFilter(final boolean header, final boolean footer) {
    super();

    this.header = header;
    this.footer = footer;
  }

  @Override
  public void doFilter(
      final @NonNull OfficeContext context,
      final @NonNull XComponent document,
      final @NonNull FilterChain chain)
      throws Exception {

    if ((header || footer) && PageStyles.isSupported(document)) {
      LOGGER.debug(
          "Applying the HeaderFooterRemoverFilter: [header={}, footer={}]", header, footer);
      for (final var style : PageStyles.of(document)) {
        if (header) {
          style.setPropertyValue("HeaderIsOn", false);
        }
        if (footer) {
          style.setPropertyValue("FooterIsOn", false);
        }
      }
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }
}
