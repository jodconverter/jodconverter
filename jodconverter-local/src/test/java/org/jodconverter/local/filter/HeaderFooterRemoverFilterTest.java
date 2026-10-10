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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.local.MockUnoRuntimeExtension;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.UnoRuntime;

/** Contains tests for the {@link HeaderFooterRemoverFilter} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class HeaderFooterRemoverFilterTest {

  private static final String HEADER = "HeaderIsOn";
  private static final String FOOTER = "FooterIsOn";

  @Nested
  class DoFilter {

    @Test
    void withUnsupportedDocument_ShouldOnlyCallNextFilter(final UnoRuntime unoRuntime)
        throws Exception {

      final var context = mock(OfficeContext.class);
      final var document = PageStylesMocks.document(unoRuntime, Lo.IMPRESS_SERVICE);
      final var styles = PageStylesMocks.pageStyles(unoRuntime, document, "Default");
      final var next = mock(Filter.class);

      final var chain = new DefaultFilterChain(false, new HeaderFooterRemoverFilter(), next);
      chain.doFilter(context, document);

      verifyNoInteractions(styles.get(0));
      verify(next).doFilter(context, document, chain);
    }

    @Test
    void withSpreadsheet_ShouldSwitchOffHeaderAndFooterOfEveryPageStyle(final UnoRuntime unoRuntime)
        throws Exception {

      final var context = mock(OfficeContext.class);
      final var document = PageStylesMocks.document(unoRuntime, Lo.CALC_SERVICE);
      final var styles = PageStylesMocks.pageStyles(unoRuntime, document, "Default", "Report");
      final var next = mock(Filter.class);

      final var chain = new DefaultFilterChain(false, new HeaderFooterRemoverFilter(), next);
      chain.doFilter(context, document);

      for (final var style : styles) {
        verify(style).setPropertyValue(HEADER, false);
        verify(style).setPropertyValue(FOOTER, false);
        verifyNoMoreInteractions(style);
      }
      verify(next).doFilter(context, document, chain);
    }

    @Test
    void withTextDocumentAndHeaderOnly_ShouldKeepTheFooter(final UnoRuntime unoRuntime)
        throws Exception {

      final var context = mock(OfficeContext.class);
      final var document = PageStylesMocks.document(unoRuntime, Lo.WRITER_SERVICE);
      final var styles = PageStylesMocks.pageStyles(unoRuntime, document, "Standard");

      new DefaultFilterChain(false, new HeaderFooterRemoverFilter(true, false))
          .doFilter(context, document);

      verify(styles.get(0)).setPropertyValue(HEADER, false);
      verify(styles.get(0), never()).setPropertyValue(FOOTER, false);
    }

    @Test
    void withFooterOnly_ShouldKeepTheHeader(final UnoRuntime unoRuntime) throws Exception {

      final var context = mock(OfficeContext.class);
      final var document = PageStylesMocks.document(unoRuntime, Lo.CALC_SERVICE);
      final var styles = PageStylesMocks.pageStyles(unoRuntime, document, "Default");

      new DefaultFilterChain(false, new HeaderFooterRemoverFilter(false, true))
          .doFilter(context, document);

      verify(styles.get(0)).setPropertyValue(FOOTER, false);
      verify(styles.get(0), never()).setPropertyValue(HEADER, false);
    }

    @Test
    void withNothingToRemove_ShouldOnlyCallNextFilter(final UnoRuntime unoRuntime)
        throws Exception {

      final var context = mock(OfficeContext.class);
      final var document = PageStylesMocks.document(unoRuntime, Lo.CALC_SERVICE);
      final var styles = PageStylesMocks.pageStyles(unoRuntime, document, "Default");
      final var next = mock(Filter.class);

      final var chain =
          new DefaultFilterChain(false, new HeaderFooterRemoverFilter(false, false), next);
      chain.doFilter(context, document);

      verify(styles.get(0), never()).setPropertyValue(anyString(), any());
      verify(next).doFilter(context, document, chain);
    }
  }
}
