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

package org.jodconverter.cli;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.local.filter.DefaultFilterChain;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.remote.ssl.SslConfig;

/**
 * The content of the configuration file given to the command line with {@code --config}: the SSL
 * options of a remote conversion, and the filters applied to the documents of a local conversion.
 *
 * @param ssl The SSL options, or null if the file has no {@code ssl} section.
 * @param filters The filters, in the order they are applied; empty if the file has no {@code
 *     filters} section.
 * @see CliConfigReader
 */
record CliConfig(@Nullable SslConfig ssl, List<Filter> filters) {

  /** A configuration without SSL options nor filters. */
  static final CliConfig EMPTY = new CliConfig(null, List.of());

  CliConfig {
    filters = List.copyOf(filters);
  }

  /**
   * Gets the chain of the filters.
   *
   * @return The filter chain, or null if there is no filter.
   */
  @Nullable FilterChain filterChain() {
    return filters.isEmpty() ? null : new DefaultFilterChain(filters.toArray(new Filter[0]));
  }
}
