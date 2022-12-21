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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sun.star.lang.XComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;

/** Base class of a FilterChain. */
public abstract class AbstractFilterChain implements FilterChain {

  private final boolean readOnly;
  protected List<Filter> filters;
  protected int pos; // to maintain the current position in the filter chain.

  /** Creates a FilterChain. */
  public AbstractFilterChain() {
    this(false);
  }

  /**
   * Creates a FilterChain that will contain the specified filters.
   *
   * @param filters The filters to add to the chain.
   */
  public AbstractFilterChain(final @Nullable Filter... filters) {
    this(false, filters);
  }

  /**
   * Creates a FilterChain that will contain the specified filters.
   *
   * @param readOnly {@code true} If the chain must be read-only (which means that no other filter
   *     can be added to the chain), {@code false} otherwise.
   * @param filters The filters to initially add to the chain.
   */
  public AbstractFilterChain(final boolean readOnly, final @Nullable Filter... filters) {

    this.readOnly = readOnly;
    this.pos = 0;
    this.filters =
        Arrays.stream(Optional.of(filters).orElse(new Filter[0])).collect(Collectors.toList());

    if (readOnly) {
      this.filters = Collections.unmodifiableList(this.filters);
    }
  }

  @Override
  public void addFilter(final @NonNull Filter filter) {

    if (readOnly) {
      throw new UnsupportedOperationException();
    }
    filters.add(filter);
  }

  @Override
  public void doFilter(final @NonNull OfficeContext context, final @NonNull XComponent document)
      throws OfficeException {

    // Call the next filter if there is one
    if (pos < filters.size()) {
      final Filter filter = filters.get(pos++);
      doFilter(filter, context, document);
    }
  }

  /**
   * Causes the specified filter to be invoked.
   *
   * @param filter The filter to execute.
   * @param context The context in use to pass along the chain.
   * @param document The document being converted to pass along the chain.
   * @throws OfficeException If an error occurs processing the filter.
   */
  protected void doFilter(
      final @NonNull Filter filter,
      final @NonNull OfficeContext context,
      final @NonNull XComponent document)
      throws OfficeException {

    try {
      filter.doFilter(context, document, this);
    } catch (Exception ex) {
      throw new OfficeException("Could not apply filter " + filter.getClass().getName() + ".", ex);
    }
  }

  /** Resets the position in the filter chain to 0, making the chain reusable. */
  public void reset() {

    pos = 0;
  }
}
