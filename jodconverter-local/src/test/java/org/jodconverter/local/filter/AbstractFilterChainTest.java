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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import com.sun.star.lang.XComponent;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;

/** Contains tests for the {@link DefaultFilterChain} class. */
class AbstractFilterChainTest {

  @Nested
  class New {

    @Test
    void whenReadOnlyIsFalse_ShouldBeEditable() {

      final TestFilterChain chain = new TestFilterChain(false);

      final Filter filter = new TestFilter();
      chain.addFilter(filter);

      assertThat(chain).extracting("filters").asList().hasSize(1).containsExactly(filter);
    }

    @Test
    void whenReadOnlyIsTrue_ShouldBeReadOnly() {

      final TestFilterChain chain = new TestFilterChain(true);

      assertThatExceptionOfType(UnsupportedOperationException.class)
          .isThrownBy(() -> chain.addFilter(new TestFilter()));
    }

    @Test
    void withoutFilters_ShouldBeEmpty() {

      final TestFilterChain chain = new TestFilterChain(false);
      assertThat(chain).extracting("filters").asList().hasSize(0);
    }
  }

  @Nested
  class DoFilter {

    @Test
    void withFilterThrowingException_ShouldThrowWrapperOfficeException() {

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(
              () ->
                  new DefaultFilterChain(
                          (context, document, chain) -> {
                            throw new IndexOutOfBoundsException();
                          })
                      .doFilter(mock(OfficeContext.class), mock(XComponent.class)))
          .withMessageStartingWith("Could not apply filter")
          .withCauseExactlyInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void withFilterThrowingOfficeException_ShouldThrowSameOfficeException() {

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(
              () ->
                  new TestFilterChain(
                          false,
                          (context, document, chain) -> {
                            throw new OfficeException("Unsupported Filter");
                          })
                      .doFilter(mock(OfficeContext.class), mock(XComponent.class)))
          .withCauseExactlyInstanceOf(OfficeException.class)
          .satisfies(
              e -> {
                assertThat(e.getCause()).hasMessage("Unsupported Filter");
              });
    }

    @Test
    void withFilters_ShouldExecuteAllFilters() throws OfficeException {

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

      assertThat(chain)
          .extracting("filters")
          .asList()
          .hasSize(10)
          .allSatisfy(
              o ->
                  assertThat(o)
                      .isInstanceOf(TestFilter.class)
                      .hasFieldOrPropertyWithValue("executeCount", 0));

      chain.doFilter(mock(OfficeContext.class), mock(XComponent.class));

      assertThat(chain)
          .extracting("filters")
          .asList()
          .hasSize(10)
          .allSatisfy(
              o ->
                  assertThat(o)
                      .isInstanceOf(TestFilter.class)
                      .hasFieldOrPropertyWithValue("executeCount", 1));
    }
  }

  @Nested
  class Reset {

    @Test
    void withFilters_ShouldExecuteAllAgainAfterReset() throws OfficeException {

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

      assertThat(chain)
          .extracting("filters")
          .asList()
          .hasSize(10)
          .allSatisfy(
              o ->
                  assertThat(o)
                      .isInstanceOf(TestFilter.class)
                      .hasFieldOrPropertyWithValue("executeCount", 0));

      chain.doFilter(mock(OfficeContext.class), mock(XComponent.class));

      assertThat(chain)
          .extracting("filters")
          .asList()
          .hasSize(10)
          .allSatisfy(
              o ->
                  assertThat(o)
                      .isInstanceOf(TestFilter.class)
                      .hasFieldOrPropertyWithValue("executeCount", 1));

      // Check that without a reset, nothing change.
      chain.doFilter(mock(OfficeContext.class), mock(XComponent.class));

      assertThat(chain)
          .extracting("filters")
          .asList()
          .hasSize(10)
          .allSatisfy(
              o ->
                  assertThat(o)
                      .isInstanceOf(TestFilter.class)
                      .hasFieldOrPropertyWithValue("executeCount", 1));

      // Now test after a reset.
      chain.reset();
      chain.doFilter(mock(OfficeContext.class), mock(XComponent.class));

      assertThat(chain)
          .extracting("filters")
          .asList()
          .hasSize(10)
          .allSatisfy(
              o ->
                  assertThat(o)
                      .isInstanceOf(TestFilter.class)
                      .hasFieldOrPropertyWithValue("executeCount", 2));
    }
  }

  static class TestFilter implements Filter {

    private int executeCount;

    @Override
    @SuppressWarnings("NullableProblems")
    public void doFilter(
        final OfficeContext context, final XComponent document, final FilterChain chain)
        throws OfficeException {

      executeCount++;
      chain.doFilter(context, document);
    }
  }

  static class TestFilterChain extends AbstractFilterChain {

    private final boolean readOnly;

    TestFilterChain(final boolean readOnly, final Filter... filters) {
      super(readOnly, filters);

      this.readOnly = readOnly;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public FilterChain copy() {
      return new TestFilterChain(readOnly, filters.toArray(new Filter[0]));
    }
  }
}
