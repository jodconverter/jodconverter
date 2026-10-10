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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sun.star.beans.XPropertySet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.local.MockUnoRuntimeExtension;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.UnoRuntime;

/** Contains tests for the {@link PageOrientationFilter} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class PageOrientationFilterTest {

  private static final String WIDTH = "Width";
  private static final String HEIGHT = "Height";
  private static final String LANDSCAPE = "IsLandscape";

  // A4, in hundredths of a millimeter.
  private static final int SHORT = 21_000;
  private static final int LONG = 29_700;

  private static void size(final XPropertySet style, final int width, final int height)
      throws Exception {
    given(style.getPropertyValue(WIDTH)).willReturn(width);
    given(style.getPropertyValue(HEIGHT)).willReturn(height);
  }

  @Nested
  class DoFilter {

    @Test
    void withUnsupportedDocument_ShouldOnlyCallNextFilter(final UnoRuntime unoRuntime)
        throws Exception {

      final var context = mock(OfficeContext.class);
      final var document = PageStylesMocks.document(unoRuntime, Lo.DRAW_SERVICE);
      final var styles = PageStylesMocks.pageStyles(unoRuntime, document, "Default");
      final var next = mock(Filter.class);

      final var chain = new DefaultFilterChain(false, new PageOrientationFilter(), next);
      chain.doFilter(context, document);

      verifyNoInteractions(styles.get(0));
      verify(next).doFilter(context, document, chain);
    }

    @Test
    void withLandscapeAndPortraitPages_ShouldSwapWidthAndHeight(final UnoRuntime unoRuntime)
        throws Exception {

      final var context = mock(OfficeContext.class);
      final var document = PageStylesMocks.document(unoRuntime, Lo.CALC_SERVICE);
      final var styles = PageStylesMocks.pageStyles(unoRuntime, document, "Default", "Report");
      final var next = mock(Filter.class);
      for (final var style : styles) {
        size(style, SHORT, LONG);
      }

      final var chain = new DefaultFilterChain(false, new PageOrientationFilter(), next);
      chain.doFilter(context, document);

      for (final var style : styles) {
        verify(style).setPropertyValue(WIDTH, LONG);
        verify(style).setPropertyValue(HEIGHT, SHORT);
        verify(style).setPropertyValue(LANDSCAPE, true);
      }
      verify(next).doFilter(context, document, chain);
    }

    @Test
    void withLandscapeAndLandscapePages_ShouldKeepTheSize(final UnoRuntime unoRuntime)
        throws Exception {

      final var context = mock(OfficeContext.class);
      final var document = PageStylesMocks.document(unoRuntime, Lo.WRITER_SERVICE);
      final var style = PageStylesMocks.pageStyles(unoRuntime, document, "Landscape").get(0);
      size(style, LONG, SHORT);

      new DefaultFilterChain(false, new PageOrientationFilter(true)).doFilter(context, document);

      verify(style, never()).setPropertyValue(eq(WIDTH), any());
      verify(style, never()).setPropertyValue(eq(HEIGHT), any());
      verify(style).setPropertyValue(LANDSCAPE, true);
    }

    @Test
    void withPortraitAndLandscapePages_ShouldSwapWidthAndHeight(final UnoRuntime unoRuntime)
        throws Exception {

      final var context = mock(OfficeContext.class);
      final var document = PageStylesMocks.document(unoRuntime, Lo.WRITER_SERVICE);
      final var style = PageStylesMocks.pageStyles(unoRuntime, document, "Landscape").get(0);
      size(style, LONG, SHORT);

      new DefaultFilterChain(false, new PageOrientationFilter(false)).doFilter(context, document);

      verify(style).setPropertyValue(WIDTH, SHORT);
      verify(style).setPropertyValue(HEIGHT, LONG);
      verify(style).setPropertyValue(LANDSCAPE, false);
    }

    @Test
    void withPortraitAndPortraitPages_ShouldKeepTheSize(final UnoRuntime unoRuntime)
        throws Exception {

      final var context = mock(OfficeContext.class);
      final var document = PageStylesMocks.document(unoRuntime, Lo.WRITER_SERVICE);
      final var style = PageStylesMocks.pageStyles(unoRuntime, document, "Standard").get(0);
      size(style, SHORT, LONG);

      new DefaultFilterChain(false, new PageOrientationFilter(false)).doFilter(context, document);

      verify(style, never()).setPropertyValue(eq(WIDTH), any());
      verify(style, never()).setPropertyValue(eq(HEIGHT), any());
      verify(style).setPropertyValue(LANDSCAPE, false);
    }
  }
}
