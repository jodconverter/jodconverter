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

package org.jodconverter.local.filter.text;

/**
 * The former name of {@link DocumentIndexesUpdaterFilter}, which updates every index of a text
 * document and not only its table of contents.
 *
 * @deprecated Use {@link DocumentIndexesUpdaterFilter}; this class will be removed in a later
 *     release.
 */
@Deprecated(since = "5.0", forRemoval = true)
public class TableOfContentUpdaterFilter extends DocumentIndexesUpdaterFilter {

  /** Creates a new filter that updates the indexes. */
  public TableOfContentUpdaterFilter() {
    super();
  }

  /**
   * Creates a new filter that updates the indexes, and changes the number of levels of the tables
   * of contents.
   *
   * @param level The number of levels of the tables of contents; 0 keeps the levels of the
   *     document.
   */
  public TableOfContentUpdaterFilter(final int level) {
    super(level);
  }
}
