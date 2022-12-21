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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import com.sun.star.lang.XComponent;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;

/** Contains tests for the {@link DefaultFilterChain} class. */
class DefaultFilterChainTest {

  @Nested
  class New {

    @Test
    void shouldBeEditable() {

      final DefaultFilterChain chain = new DefaultFilterChain(false);
      assertThatCode(() -> chain.addFilter(new TestFilter())).doesNotThrowAnyException();
    }

    @Test
    void emptyCtor_ShouldEndsWithRefreshFilter() {

      final DefaultFilterChain chain = new DefaultFilterChain();
      assertThat(chain).hasFieldOrPropertyWithValue("endsWithRefreshFilter", true);
    }
  }

  @Nested
  class Copy {

    @Test
    void withFilters_ShouldCopyWithFilters() {

      final DefaultFilterChain chain =
          new DefaultFilterChain(
              false, new TestFilter(), new TestFilter(), new TestFilter(), new TestFilter());
      assertThat(chain.copy())
          .hasFieldOrPropertyWithValue("endsWithRefreshFilter", false)
          .extracting("filters")
          .asList()
          .hasSize(4);
    }

    @Test
    void withoutFilters_ShouldCopyWithoutFilters() {

      final DefaultFilterChain chain = new DefaultFilterChain(true);
      assertThat(chain.copy())
          .hasFieldOrPropertyWithValue("endsWithRefreshFilter", true)
          .extracting("filters")
          .asList()
          .isEmpty();
    }
  }

  @Nested
  class DoFilter {

    @Test
    void withoutFilterAndEndsWithRefreshFilterIsFalse_ShouldNotExecuteLastRefresh()
        throws OfficeException {

      final TestFilterChain chain = new TestFilterChain(false);
      chain.doFilter(mock(OfficeContext.class), mock(XComponent.class));

      assertThat(chain.lastRefreshFilterExecutedCount).isEqualTo(0);
    }

    @Test
    void withMultipleFiltersAndEndsWithRefreshFilterIsFalse_ShouldNotExecuteLastRefresh()
        throws OfficeException {

      final TestFilterChain chain =
          new TestFilterChain(
              false,
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter());
      chain.doFilter(mock(OfficeContext.class), mock(XComponent.class));

      assertThat(chain.lastRefreshFilterExecutedCount).isEqualTo(0);
    }

    @Test
    void withoutFilterAndEndsWithRefreshFilterIsTrue_ShouldExecuteLastRefresh()
        throws OfficeException {

      final TestFilterChain chain = new TestFilterChain(true);
      chain.doFilter(mock(OfficeContext.class), mock(XComponent.class));

      assertThat(chain.lastRefreshFilterExecutedCount).isEqualTo(1);
    }

    @Test
    void withMultipleFiltersAndEndsWithRefreshFilterIsTrue_ShouldExecuteLastRefresh()
        throws OfficeException {

      final TestFilterChain chain =
          new TestFilterChain(
              true,
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter());
      chain.doFilter(mock(OfficeContext.class), mock(XComponent.class));

      assertThat(chain.lastRefreshFilterExecutedCount).isEqualTo(1);
    }

    @Test
    void withRefreshFilterAndEndsWithRefreshFilterSetToTrue_ShouldNotExecuteLastRefresh()
        throws OfficeException {

      final TestFilterChain chain =
          new TestFilterChain(
              true,
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new TestFilter(),
              new RefreshFilter());
      chain.doFilter(mock(OfficeContext.class), mock(XComponent.class));

      assertThat(chain.lastRefreshFilterExecutedCount).isEqualTo(0);
    }
  }

  static class TestFilter implements Filter {

    int executeCount;

    @Override
    @SuppressWarnings("NullableProblems")
    public void doFilter(
        final OfficeContext context, final XComponent document, final FilterChain chain)
        throws OfficeException {

      executeCount++;
      chain.doFilter(context, document);
    }
  }

  static class TestFilterChain extends DefaultFilterChain {

    final boolean endsWithRefreshFilter;
    int lastRefreshFilterExecutedCount;

    TestFilterChain(final boolean endsWithRefreshFilter, final Filter... filters) {
      super(endsWithRefreshFilter, filters);

      this.endsWithRefreshFilter = endsWithRefreshFilter;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public FilterChain copy() {
      return new TestFilterChain(endsWithRefreshFilter, filters.toArray(new Filter[0]));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilter(
        final Filter filter, final OfficeContext context, final XComponent document)
        throws OfficeException {

      if (RefreshFilter.LAST_REFRESH.equals(filter)) {
        lastRefreshFilterExecutedCount++;
      }
      super.doFilter(filter, context, document);
    }
  }
}
