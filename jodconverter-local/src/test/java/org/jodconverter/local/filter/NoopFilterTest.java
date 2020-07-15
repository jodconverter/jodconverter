/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sun.star.lang.XComponent;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeContext;

/** Contains tests for the {@link NoopFilter} class. */
class NoopFilterTest {

  @Nested
  class NoopFilterChain {

    @Test
    void chain_ShouldBeReadOnly() {

      assertThatExceptionOfType(UnsupportedOperationException.class)
          .isThrownBy(() -> NoopFilter.CHAIN.addFilter(NoopFilter.NOOP));
    }
  }

  @Nested
  class DoFilter {

    @Test
    void shouldCallNextFilter() throws Exception {

      final Filter filter = mock(Filter.class);
      final OfficeContext context = mock(OfficeContext.class);
      final XComponent document = mock(XComponent.class);
      final DefaultFilterChain chain = new DefaultFilterChain(NoopFilter.NOOP, filter);
      chain.doFilter(context, document);

      // Verify that the filter is called.
      verify(filter, times(1)).doFilter(context, document, chain);
    }
  }
}
