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

import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;

/**
 * This filter sets the orientation of the pages of the document being converted, landscape or
 * portrait, by changing its page styles: the width and the height of a page that has the other
 * orientation are swapped, so the paper format is kept.
 *
 * <p>The filter applies to spreadsheets and to text documents; other documents are left as they
 * are, since the page of a presentation or of a drawing is the slide itself.
 */
public class PageOrientationFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageOrientationFilter.class);

  private static final String WIDTH = "Width";
  private static final String HEIGHT = "Height";

  private final boolean landscape;

  /** Creates a new filter that will set the pages of the document to landscape. */
  public PageOrientationFilter() {
    this(true);
  }

  /**
   * Creates a new filter that will set the orientation of the pages of the document.
   *
   * @param landscape {@code true} for landscape pages, {@code false} for portrait pages.
   */
  public PageOrientationFilter(final boolean landscape) {
    super();

    this.landscape = landscape;
  }

  @Override
  public void doFilter(
      final @NonNull OfficeContext context,
      final @NonNull XComponent document,
      final @NonNull FilterChain chain)
      throws Exception {

    if (PageStyles.isSupported(document)) {
      LOGGER.debug("Applying the PageOrientationFilter: [landscape={}]", landscape);
      for (final var style : PageStyles.of(document)) {
        setOrientation(style);
      }
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private void setOrientation(final XPropertySet style) throws Exception {

    // The size tells the orientation; the IsLandscape property only records it, and does not
    // turn the page when it changes.
    final var width = ((Number) style.getPropertyValue(WIDTH)).intValue();
    final var height = ((Number) style.getPropertyValue(HEIGHT)).intValue();
    if (landscape ? width < height : width > height) {
      style.setPropertyValue(WIDTH, height);
      style.setPropertyValue(HEIGHT, width);
    }
    style.setPropertyValue("IsLandscape", landscape);
  }
}
