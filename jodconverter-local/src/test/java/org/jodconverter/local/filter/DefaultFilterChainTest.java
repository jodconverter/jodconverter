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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import java.util.List;

import com.sun.star.lang.XComponent;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;

/** Contains tests for the {@link DefaultFilterChain} class. */
public class DefaultFilterChainTest {

  /** Tests that a DefaultFilterChain is created empty by default. */
  @Test
  public void create_WithoutFilters_ShouldBeEmpty() {

    final DefaultFilterChain chain = new DefaultFilterChain();
    final List<Filter> filters = Whitebox.getInternalState(chain, "filters");
    assertThat(filters).hasSize(0);
  }

  /** Tests that a DefaultFilterChain.addFilter works as expected. */
  @Test
  public void create_ShouldBeEditable() {

    final Filter filter = new RefreshFilter();
    final DefaultFilterChain chain = new DefaultFilterChain();
    chain.addFilter(filter);

    final List<Filter> filters = Whitebox.getInternalState(chain, "filters");
    assertThat(filters).hasSize(1);
    assertThat(filters).containsExactly(filter);
  }

  @Test
  public void doFilter_WithFilterThrowingException_ThrowsOfficeException() {

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(
            () ->
                new DefaultFilterChain(
                        (context, document, chain) -> {
                          throw new OfficeException("Unsupported Filter");
                        })
                    .doFilter(mock(OfficeContext.class), mock(XComponent.class)))
        .withNoCause()
        .withMessage("Unsupported Filter");

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(
            () ->
                new DefaultFilterChain(
                        (context, document, chain) -> {
                          throw new IndexOutOfBoundsException();
                        })
                    .doFilter(mock(OfficeContext.class), mock(XComponent.class)))
        .withCauseExactlyInstanceOf(IndexOutOfBoundsException.class);
  }
}
