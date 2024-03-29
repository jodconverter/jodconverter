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

/** Contains tests for the {@link OfficeConnectionException} class. */
class OfficeConnectionExceptionTest {

  @Test
  void withConnectStringAndThrowable_ShouldCreateExceptionWithConnectStringAndThrowable() {

    final OfficeConnectionException ex = new OfficeConnectionException("message", "host=127.0.0.1");
    assertThat(ex.getConnectString()).isEqualTo("host=127.0.0.1");
    assertThat(ex).hasNoCause();
  }

  @Test
  void withConnectString_ShouldCreateExceptionWithConnectString() {

    final OfficeConnectionException ex =
        new OfficeConnectionException("message", "host=127.0.0.1", new RuntimeException());
    assertThat(ex.getConnectString()).isEqualTo("host=127.0.0.1");
    assertThat(ex).hasCauseExactlyInstanceOf(RuntimeException.class);
  }
}
