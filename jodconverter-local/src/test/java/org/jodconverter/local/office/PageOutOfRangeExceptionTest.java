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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeException;

/** Contains tests for the {@link PageOutOfRangeException} class. */
class PageOutOfRangeExceptionTest {

  @Test
  void whenCreated_ShouldGiveThePageAndThePageCount() {

    final var exception = new PageOutOfRangeException(4, 3);

    assertThat(exception.getPage()).isEqualTo(4);
    assertThat(exception.getPageCount()).isEqualTo(3);
  }

  @Test
  void whenCreated_ShouldSayWhichPageDoesNotExistAndHowManyThereAre() {

    // The message is the one that the export had before the exception had a type of its own.
    assertThat(new PageOutOfRangeException(4, 3))
        .isInstanceOf(OfficeException.class)
        .hasMessage("Page 4 does not exist: the document has 3 pages");
  }
}
