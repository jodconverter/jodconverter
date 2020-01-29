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

import org.junit.jupiter.api.Test;

/** Contains tests for the {@link NoopFilter} class. */
public class NoopFilterTest {

  /** Tests that a NoopFilter.CHAIN is read only. */
  @Test
  public void chain_ShouldBeReadOnly() {

    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> NoopFilter.CHAIN.addFilter(NoopFilter.NOOP));
  }

  /** Tests that a NoopFilter#doFilter execute the next filter in the chain. */
  @Test
  public void doFilter_ShouldCallNextFilter() throws Exception {

    final Filter filter = mock(Filter.class);
    final DefaultFilterChain chain = new DefaultFilterChain(NoopFilter.NOOP, filter);
    chain.doFilter(null, null);

    // Verify that the filter is called.
    verify(filter, times(1)).doFilter(null, null, chain);
  }
}
