/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.filter;

import org.junit.Test;

import com.sun.star.lang.XComponent;

import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;

/**
 * Contains tests for the {@link org.jodconverter.filter.UnmodifiableFilterChain} class.
 *
 * @see org.jodconverter.filter.UnmodifiableFilterChain
 */
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class UnmodifiableFilterChainTest {

  /** Tests that a UnmodifiableFilterChain.addFilter throws an exception after creation. */
  @Test(expected = UnsupportedOperationException.class)
  public void create_ShouldBeReadOnly() {

    final UnmodifiableFilterChain chain = new UnmodifiableFilterChain(RefreshFilter.REFRESH);
    chain.addFilter(
        new Filter() {

          @Override
          public void doFilter(
              final OfficeContext context, final XComponent document, final FilterChain chain)
              throws OfficeException {
            // Do nothing
          }
        });
  }
}
