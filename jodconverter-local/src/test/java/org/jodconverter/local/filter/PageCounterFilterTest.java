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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.sheet.XSpreadsheets;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.local.MockUnoRuntimeExtension;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.UnoRuntime;

/** Contains tests for the {@link PageCounterFilter} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class PageCounterFilterTest {

  @Nested
  class DoFilter {

    @Test
    void withText_ShouldCallNextFilter(final UnoRuntime unoRuntime) throws Exception {

      final OfficeContext context = mock(OfficeContext.class);
      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      final XModel model = mock(XModel.class);
      final XController controller = mock(XController.class);
      final XPropertySet set = mock(XPropertySet.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.WRITER_SERVICE)).willReturn(true);
      given(unoRuntime.queryInterface(XModel.class, document)).willReturn(model);
      given(model.getCurrentController()).willReturn(controller);
      given(unoRuntime.queryInterface(XPropertySet.class, controller)).willReturn(set);
      given(set.getPropertyValue("PageCount")).willReturn(1);

      final PageCounterFilter filter = new PageCounterFilter();
      final Filter next = mock(Filter.class);

      final DefaultFilterChain chain = new DefaultFilterChain(false, filter, next);
      chain.doFilter(context, document);

      assertThat(filter.getPageCount()).isEqualTo(1);
      verify(next, times(1)).doFilter(context, document, chain);
    }

    @Test
    void withCalc_ShouldCallNextFilter(final UnoRuntime unoRuntime) throws Exception {

      final OfficeContext context = mock(OfficeContext.class);
      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      final XSpreadsheetDocument calcDocument = mock(XSpreadsheetDocument.class);
      final XSpreadsheets sheets = mock(XSpreadsheets.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.CALC_SERVICE)).willReturn(true);
      given(unoRuntime.queryInterface(XSpreadsheetDocument.class, document))
          .willReturn(calcDocument);
      given(calcDocument.getSheets()).willReturn(sheets);
      given(sheets.getElementNames()).willReturn(new String[] {"Page1"});

      final PageCounterFilter filter = new PageCounterFilter();
      final Filter next = mock(Filter.class);

      final DefaultFilterChain chain = new DefaultFilterChain(false, filter, next);
      chain.doFilter(context, document);

      assertThat(filter.getPageCount()).isEqualTo(1);
      verify(next, times(1)).doFilter(context, document, chain);
    }

    @Test
    void withImpress_ShouldCallNextFilter(final UnoRuntime unoRuntime) throws Exception {

      final OfficeContext context = mock(OfficeContext.class);
      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      final XDrawPagesSupplier supplier = mock(XDrawPagesSupplier.class);
      final XDrawPages drawPages = mock(XDrawPages.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.IMPRESS_SERVICE)).willReturn(true);
      given(unoRuntime.queryInterface(XDrawPagesSupplier.class, document)).willReturn(supplier);
      given(supplier.getDrawPages()).willReturn(drawPages);
      given(drawPages.getCount()).willReturn(1);

      final PageCounterFilter filter = new PageCounterFilter();
      final Filter next = mock(Filter.class);

      final DefaultFilterChain chain = new DefaultFilterChain(false, filter, next);
      chain.doFilter(context, document);

      assertThat(filter.getPageCount()).isEqualTo(1);
      verify(next, times(1)).doFilter(context, document, chain);
    }

    @Test
    void withDraw_ShouldCallNextFilter(final UnoRuntime unoRuntime) throws Exception {

      final OfficeContext context = mock(OfficeContext.class);
      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      final XDrawPagesSupplier supplier = mock(XDrawPagesSupplier.class);
      final XDrawPages drawPages = mock(XDrawPages.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(Lo.DRAW_SERVICE)).willReturn(true);
      given(unoRuntime.queryInterface(XDrawPagesSupplier.class, document)).willReturn(supplier);
      given(supplier.getDrawPages()).willReturn(drawPages);
      given(drawPages.getCount()).willReturn(1);

      final PageCounterFilter filter = new PageCounterFilter();
      final Filter next = mock(Filter.class);

      final DefaultFilterChain chain = new DefaultFilterChain(false, filter, next);
      chain.doFilter(context, document);

      assertThat(filter.getPageCount()).isEqualTo(1);
      verify(next, times(1)).doFilter(context, document, chain);
    }

    @Test
    void withUnsupportedDocument_ShouldCallNextFilterAndReturn0(final UnoRuntime unoRuntime)
        throws Exception {

      final OfficeContext context = mock(OfficeContext.class);
      final XComponent document = mock(XComponent.class);
      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(anyString())).willReturn(false);

      final PageCounterFilter filter = new PageCounterFilter();
      final Filter next = mock(Filter.class);

      final DefaultFilterChain chain = new DefaultFilterChain(false, filter, next);
      chain.doFilter(context, document);

      assertThat(filter.getPageCount()).isEqualTo(0);
      verify(next, times(1)).doFilter(context, document, chain);
    }
  }
}
