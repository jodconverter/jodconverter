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

package org.jodconverter.local.office;

import java.io.Serial;

import org.jodconverter.core.office.OfficeException;

/**
 * Thrown when the pages asked for an export go beyond the last page of the document. The number of
 * pages is only known once the office has loaded the document, so the range given to the job could
 * not be checked before.
 *
 * <p>It is a mistake of the request, not a failure of the office: an application can answer it as
 * such, with the number of pages that the document has.
 */
public class PageOutOfRangeException extends OfficeException {
  @Serial private static final long serialVersionUID = 1024561283382499609L;

  private final int page;
  private final int pageCount;

  /**
   * Constructs a new exception for a page beyond the last one.
   *
   * @param page The page that was asked for: the last page of the range.
   * @param pageCount The number of pages of the document.
   */
  public PageOutOfRangeException(final int page, final int pageCount) {
    super("Page " + page + " does not exist: the document has " + pageCount + " pages");
    this.page = page;
    this.pageCount = pageCount;
  }

  /**
   * Gets the page that was asked for and does not exist: the last page of the range.
   *
   * @return The number of the page, from 1.
   */
  public int getPage() {
    return page;
  }

  /**
   * Gets the number of pages of the document.
   *
   * @return The number of pages.
   */
  public int getPageCount() {
    return pageCount;
  }
}
